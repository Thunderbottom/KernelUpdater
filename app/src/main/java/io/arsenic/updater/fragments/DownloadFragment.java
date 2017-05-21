package io.arsenic.updater.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.arsenic.updater.R;
import io.arsenic.updater.utils.KernelUpdater;
import io.arsenic.updater.views.DataAdapter;

import static android.os.Environment.getExternalStorageDirectory;


public class DownloadFragment extends Fragment{

    View downloadView;
    private Spinner downloadSpinner;
    DownloadTask downloadTask;
    ProgressDialog mProgressDialog;
    NotificationManager notificationManager;
    NotificationCompat.Builder notification;
    String filename;
    Unbinder unbinder;

    int TIMEOUT = 1000;

    @BindView(R.id.searchButton) Button searchButton;
    @BindView(R.id.dismiss) TextView dismiss;
    @BindView(R.id.never_show) TextView never_show;
    @BindView(R.id.help_card_view) CardView helpCard;

    public DownloadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        downloadView = inflater.inflate(R.layout.fragment_download, container, false);
        unbinder = ButterKnife.bind(this, downloadView);
        SharedPreferences sp = getContext().getSharedPreferences("view", Activity.MODE_PRIVATE);
        if (sp.getInt("helpCardVisible", 1) == 0) {
            dismiss();
        }
        downloadSpinner = (Spinner) downloadView.findViewById(R.id.spinner);
        try {
            getVersions();
        } catch (JSONException ignored) {
            Toast.makeText(getContext(), getString(R.string.kernel_version_fail), Toast.LENGTH_SHORT).show();
        }
        return downloadView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.searchButton)
    public void searchButton(){
        if (KernelUpdater.getStoragePermission(getContext(), getActivity())) {
            try {
                initViews(downloadSpinner.getSelectedItemPosition());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.dismiss)
    public void dismiss() {
        helpCard.setVisibility(View.GONE);
    }

    @OnClick(R.id.never_show)
    public void neverShow() {
        SharedPreferences settings = getActivity().getSharedPreferences("view", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("helpCardVisible", 0)
                .apply();
        dismiss();
    }

    /**
     * Initialize RecyclerView for download cards.
     * @throws JSONException for problems with JSON
     **/
    private void initViews(int position) throws JSONException {
        RecyclerView recyclerView = (RecyclerView) downloadView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        ArrayList arsenic_list = KernelUpdater.getKernelVersionList(position);
        RecyclerView.Adapter<DataAdapter.ViewHolder > adapter = new DataAdapter(arsenic_list, DownloadFragment.this);
        recyclerView.setAdapter(adapter);
    }


    /**
     * Get all available kernel version from remote JSON.
     * @throws JSONException for problems with JSON
     **/
    public void getVersions() throws JSONException {
        JSONArray versions = KernelUpdater.getJSON().getJSONArray("versions");
        List<String> version_list = new ArrayList<>();
        for(int i = 0; i < versions.length(); i ++){
            JSONObject kernel_versions = versions.getJSONObject(i);
            version_list.add(kernel_versions.get("kernel_version").toString());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, version_list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        downloadSpinner.setAdapter(spinnerAdapter);
    }

    public void downloadFile(String URL) {
        HomeFragment.updateDownloader.downloadUpdate(URL);
    }

    /**
     *  Checks whether the download directory exists.
     *  Tries to create the directory if it does not exist.
     **/
    public void checkDir(){
        File folder = new File(getExternalStorageDirectory() + getString(R.string.download_location));
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (!success) {
            downloadTask.cancel(true);
            mProgressDialog.dismiss();
            Toast.makeText(getContext(), getString(R.string.create_failed), Toast.LENGTH_SHORT).show();
            setNotification(getString(R.string.download_failed), R.drawable.ic_cancel);
        }
    }

    /**
     *  Downloads the file from the specified URL.
     *  Downloads to /sdcard/.arsenicupdater/downloads/ directory.
     **/
    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                // download the file
                checkDir();
                input = connection.getInputStream();
                filename = getExternalStorageDirectory() + getString(R.string.download_location) +
                        URLUtil.guessFileName(url.toString(), null,
                                MimeTypeMap.getFileExtensionFromUrl(url.toString()));
                output = new FileOutputStream(filename);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Notification
            notification.setProgress(100, 0, false);
            notificationManager.notify(1, notification.build());
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(TIMEOUT);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Notification
            notification.setProgress(100, progress[0], false);
            notificationManager.notify(1, notification.build());
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onCancelled(){
            setNotification(getString(R.string.download_failed), R.drawable.ic_cancel);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
                setNotification(getString(R.string.download_failed), R.drawable.ic_cancel);
            }
            else {
                Toast.makeText(context, getString(R.string.download_complete), Toast.LENGTH_SHORT).show();
                setNotification(getString(R.string.download_complete), R.drawable.ic_check);
                KernelUpdater.flashFile(getContext(), filename);
            }
        }
    }

    public void setNotification(String notification_text, int notification_icon){
        notification.setContentText(notification_text);
        notification.setSmallIcon(notification_icon);
        notification.setProgress(0, 0, false);
        notificationManager.notify(1, notification.build());
    }
}
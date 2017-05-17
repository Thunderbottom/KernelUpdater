package io.arsenic.updater.fragments;


import android.app.Fragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
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

import io.arsenic.updater.R;
import io.arsenic.updater.utils.ArsenicUpdater;
import io.arsenic.updater.views.DataAdapter;

import static android.os.Environment.getExternalStorageDirectory;


public class DownloadFragment extends Fragment{

    View downloadView;
    Button searchButton;
    private Spinner downloadSpinner;
    DownloadTask downloadTask;
    ProgressDialog mProgressDialog;
    NotificationManager notificationManager;
    NotificationCompat.Builder notification;
    String filename;

    public DownloadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        downloadView = inflater.inflate(R.layout.fragment_download, container, false);
        downloadSpinner = (Spinner) downloadView.findViewById(R.id.spinner);
        try {
            getVersions();
        } catch (JSONException ignored) {
            Toast.makeText(getContext(), getString(R.string.kernelVersionFail), Toast.LENGTH_SHORT).show();
        }
        searchButton = (Button) downloadView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initViews(downloadSpinner.getSelectedItemPosition());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return downloadView;
    }

    private void initViews(int position) throws JSONException {
        RecyclerView recyclerView = (RecyclerView) downloadView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<String> arsenic_list = ArsenicUpdater.getKernelVersionList(position);
        RecyclerView.Adapter<DataAdapter.ViewHolder> adapter = new DataAdapter(arsenic_list);
        recyclerView.setAdapter(adapter);
    }

    public void getVersions() throws JSONException {
        JSONArray versions = ArsenicUpdater.getJSON().getJSONArray("versions");
        List<String> version_list = new ArrayList<>();
        for(int i = 0; i < versions.length(); i ++){
            JSONObject kernel_versions = versions.getJSONObject(i);
            version_list.add(kernel_versions.get("kernel_version").toString());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, version_list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        downloadSpinner.setAdapter(spinnerAdapter);
    }

    public void downloadFile(String URL){
        filename = URLUtil.guessFileName(URL, null, MimeTypeMap.getFileExtensionFromUrl(URL));
        File alreadyExist = new File(getExternalStorageDirectory() + getString(R.string.downloadLocation), filename);
        if(!alreadyExist.exists()) {
            notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notification = new NotificationCompat.Builder(getActivity());
            notification.setContentTitle(getString(R.string.kernelDownloader))
                    .setContentText(getString(R.string.downloadingUpdate))
                    .setSmallIcon(R.drawable.app_icon)
                    .setColor(ContextCompat.getColor(getContext(), R.color.blue_500));
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.downloadingUpdate));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            downloadTask = new DownloadTask(getActivity());
            downloadTask.execute(URL);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    downloadTask.cancel(true);
                    notification.setContentText(getString(R.string.downloadCanceled));
                    notification.setProgress(0, 0, false);
                    notificationManager.notify(1, notification.build());
                }
            });
        }
        else {
            ArsenicUpdater.flashFile(getContext(), filename);
        }
    }

    public void checkDir(){
        File folder = new File(getExternalStorageDirectory() + getString(R.string.updateLocation));
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (!success) {
            downloadTask.cancel(true);
            Toast.makeText(getContext(), getString(R.string.createFailed), Toast.LENGTH_SHORT).show();
            notification.setContentText(getString(R.string.downloadFailed));
            notification.setProgress(0, 0, false);
            notificationManager.notify(1, notification.build());
        }
    }

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
                filename = getExternalStorageDirectory() + getString(R.string.downloadLocation) + URLUtil.guessFileName(url.toString(), null, MimeTypeMap.getFileExtensionFromUrl(url.toString()));
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
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Notification
            notification.setProgress(100, progress[0], false);
            notificationManager.notify(1, notification.build());
            super.onProgressUpdate(progress);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
                notification.setContentText(getString(R.string.downloadFailed));
            }
            else {
                Toast.makeText(context, getString(R.string.downloadComplete), Toast.LENGTH_SHORT).show();
                notification.setContentText(getString(R.string.downloadComplete));
                ArsenicUpdater.flashFile(getContext(), filename);
            }
            notification.setProgress(0, 0, false);
            notificationManager.notify(1, notification.build());
        }
    }
}

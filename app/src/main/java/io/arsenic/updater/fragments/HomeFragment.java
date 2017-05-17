package io.arsenic.updater.fragments;


import android.app.Fragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.arsenic.updater.R;
import io.arsenic.updater.utils.ArsenicUpdater;
import io.arsenic.updater.utils.RootUtils;

import static android.content.ContentValues.TAG;
import static android.os.Environment.getExternalStorageDirectory;

public class HomeFragment extends Fragment {

    TextView kernelVersion;
    String kVersion;
    RootUtils.SU su;
    DownloadTask downloadTask;
    ProgressDialog mProgressDialog;
    NotificationManager notificationManager;
    NotificationCompat.Builder notification;

    String filename;

    private Unbinder unbinder;

    @BindView(R.id.update_container) View updateContainer;
    @BindView(R.id.update_check_progress) ProgressBar updateProgressBar;
    @BindView(R.id.update_status) TextView updateStatus;
    @BindView (R.id.update_icon) ImageView updateIcon;
    @BindView(R.id.downloadUpdate_card_view) CardView updateCardView;

    @BindColor(R.color.red_500) int colorBad;
    @BindColor(R.color.green_500) int colorOK;
    @BindColor(R.color.yellow_500) int colorWarn;
    @BindColor(R.color.grey_500) int colorNeutral;
    @BindColor(R.color.blue_500) int colorInfo;
    @BindColor(android.R.color.transparent) int trans;

    @BindView(R.id.rebootBootloader_card_view) CardView rebootBootloader;
    @BindView(R.id.rebootRecovery_card_view) CardView rebootRecovery;

    public void checkForUpdates() {
        int updateImage, updateColor, updateText;
        updateProgressBar.setVisibility(View.VISIBLE);
        updateContainer.setBackgroundColor(trans);
        if(isNetworkAvailable()) {
            // Internet Connection Available
            if(ArsenicUpdater.getKernelVersion()
                    .toLowerCase()
                    .contains(getString(R.string.arsenic).toLowerCase())) {
                // Installed Kernel is Arsenic
                switch (ArsenicUpdater.getUpdateValue()) {
                    case 0:
                        updateColor = colorOK;
                        updateImage = R.drawable.ic_check;
                        updateText = R.string.latestVersion;
                        break;
                    case 1:
                        updateColor = colorInfo;
                        updateImage = R.drawable.ic_update;
                        updateText = R.string.newerVersion;
                        updateCardView.setVisibility(View.VISIBLE);
                        break;
                    case -2:
                        updateColor = colorNeutral;
                        updateImage = R.drawable.ic_help;
                        updateText  = R.string.failedUpdate;
                        break;
                    default:
                        updateColor = colorBad;
                        updateImage = R.drawable.ic_cancel;
                        updateText  = R.string.unknownVersion;
                        break;
                }
            }
            else {
                // Installed kernel is not Arsenic
                updateColor = colorBad;
                updateImage = R.drawable.ic_cancel;
                updateText = R.string.unknownKernel;
            }
        }
        else{
            // No Internet Connection
            updateColor = colorNeutral;
            updateImage = R.drawable.ic_help;
            updateText = R.string.cannotCheckUpdate;
        }
        updateContainer.setBackgroundColor(updateColor);
        updateIcon.setImageResource(updateImage);
        updateStatus.setText(updateText);
        updateStatus.setTextColor(updateColor);
        updateProgressBar.setVisibility(View.INVISIBLE);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, homeView);
        kernelVersion = (TextView) homeView.findViewById(R.id.kvTextView);
        kVersion = ArsenicUpdater.getKernelValue();
        kernelVersion.setText(kVersion);
        checkForUpdates();
        return homeView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.rebootRecovery_card_view)
    public void rebootRecovery(){
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.reboot_recovery_text))
                .setMessage(getString(R.string.rebootConfirm))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        su = RootUtils.getSU();
                        su.runCommand("reboot recovery");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancelled Reboot Bootloader");
                    }
                })
                .show();
    }

    @OnClick(R.id.rebootBootloader_card_view)
    public void rebootBootloader(){
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.reboot_bootloader_text))
                .setMessage(getString(R.string.rebootConfirm))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        su = RootUtils.getSU();
                        su.runCommand("reboot bootloader");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancelled Reboot Bootloader");
                    }
                })
                .show();
    }

    @OnClick(R.id.downloadUpdate_card_view)
    public void downloadUpdate(){
        String URL = ArsenicUpdater.getDownloadURL();
        filename = URLUtil.guessFileName(URL, null, MimeTypeMap.getFileExtensionFromUrl(URL));
        File alreadyExist = new File(getExternalStorageDirectory() + getString(R.string.updateLocation), filename);
        if (ArsenicUpdater.getStoragePermission(getContext())){
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
        else {
            Toast.makeText(getContext(), getString(R.string.downloadFailed), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  Checks whether there is an active internet connection
     *  @url http://stackoverflow.com/a/4239019
     *  @return Network Connectivity information
     **/
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
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
                filename = getExternalStorageDirectory() + getString(R.string.updateLocation) + URLUtil.guessFileName(url.toString(), null, MimeTypeMap.getFileExtensionFromUrl(url.toString()));
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
            // Continues download in the background
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

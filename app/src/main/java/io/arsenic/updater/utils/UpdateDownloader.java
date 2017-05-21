package io.arsenic.updater.utils;


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

public class UpdateDownloader {

    private Context mContext;
    private Activity mActivity;
    private DownloadTask mDownloadTask;
    private ProgressDialog mProgressDialog;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotification;
    private View mHomeView;

    public UpdateDownloader(Activity activity, Context context, View homeView) {
        mActivity = activity;
        mContext = context;
        mHomeView = homeView;
    }

    private Activity getActivity() {
        return mActivity;
    }

    private Context getContext() {
        return mContext;
    }

    public void downloadUpdate() {
        downloadUpdate(KernelUpdater.getDownloadURL());
    }

    public void downloadUpdate(String URL) {
        if (KernelUpdater.isNetworkAvailable(getActivity())) {
            final String filename = URLUtil.guessFileName(URL, null, MimeTypeMap.getFileExtensionFromUrl(URL));
            final File alreadyExist = new File(mContext.getExternalStorageDirectory() + 
                            mContext.getString(R.string.update_location),
                        filename);
            if (KernelUpdater.getStoragePermission(getContext(), getActivity())) {
                if (!alreadyExist.exists()) {
                    // Set downloading notification
                    mNotificationManager = (NotificationManager) getActivity()
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotification = new NotificationCompat.Builder(getActivity());
                    mNotification.setContentTitle(getString(R.string.kernelDownloader))
                            .setContentText(getContext().getString(R.string.downloading_update))
                            .setSmallIcon(R.drawable.ic_notification)
                            .setColor(ContextCompat.getColor(getContext(), R.color.blue_500));
                    // Initialize download progress dialog
                    mProgressDialog = new ProgressDialog(getActivity());
                    mProgressDialog.setMessage(getContext()
                                    .getString(R.string.downloading_update));
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDownloadTask.cancel(true);
                            if (alreadyExist.exists())
                                if (alreadyExist.delete())
                                    Toast.makeText(getActivity(), getContext()
                                        .getString(R.string.download_canceled),
                                                Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    });
                    // Start downloading in the background
                    mDownloadTask = new DownloadTask(getActivity());
                    mDownloadTask.execute(URL);
                } else {
                    KernelUpdater.flashFile(getContext(), alreadyExist.toString());
                }
            }
        } else {
            Snackbar.make(mHomeView, getString(R.string.no_internet), Snackbar.LENGTH_SHORT)
                    .show();
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
            Notification notification = mNotification;
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
            Notification notification = mNotification;
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
                Toast.makeText(mContext, "Download error: " + result, Toast.LENGTH_LONG).show();
                setNotification(getContext()getString(R.string.download_failed), R.drawable.ic_cancel);
            }
            else {
                Toast.makeText(mContext, getString(R.string.download_complete), Toast.LENGTH_SHORT).show();
                setNotification(getContext().getString(R.string.download_complete), R.drawable.ic_check);
                KernelUpdater.flashFile(getContext(), filename);
            }
        }
    }

    public void setNotification(String notification_text, int notification_icon) {
        Notification notification = mNotification;
        notification.setContentText(notification_text);
        notification.setSmallIcon(notification_icon);
        notification.setProgress(0, 0, false);
        notificationManager.notify(1, notification.build());
    }

    /**
     *  Checks whether the download directory exists.
     *  Tries to create the directory if it does not exist.
     **/
    public void checkDir(){
        File folder = new File(getExternalStorageDirectory() + getContext().getString(R.string.download_location));
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

}
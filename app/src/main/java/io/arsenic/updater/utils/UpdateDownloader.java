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

}
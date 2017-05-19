package io.arsenic.updater.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import io.arsenic.updater.R;

import static android.content.ContentValues.TAG;


public class KernelUpdater {

    private static ArrayList downloadList;
    private static String kernelVersion;
    private static String kernelValue;
    private static JSONObject JSON;
    private static int updateValue = 0;
    private static String downloadURL;

    private static final String SCRIPT_NAME = "openrecoveryscript";

    private static final File OPENRECOVERY_SCRIPT_FILE = new File("/cache/recovery", SCRIPT_NAME);


    /**
     *  Gets List of download for specified kernel.
     *  @param downloadList JSON String Object.
     **/
    private static void setDownloadList(ArrayList downloadList) {
        KernelUpdater.downloadList = downloadList;
    }

    public static ArrayList getDownloadList() {
        return downloadList;
    }


    /**
     *  Gets Current Kernel Version from KernelUtils.
     *  @param kernelVersion JSON String Object.
     **/
    static void setKernelValue(String kernelVersion) {
        KernelUpdater.kernelValue = kernelVersion;
    }

    public static String getKernelValue() {
        return kernelValue;
    }


    /**
     *  Gets Current Kernel Version from KernelUtils.
     *  @param kernelVersion JSON String Object.
     **/
     static void setKernelVersion(String kernelVersion) {
        KernelUpdater.kernelVersion = kernelVersion;
    }

    public static String getKernelVersion() {
        return kernelVersion;
    }


    /**
     *  Gets JSON Object from KernelUtils.
     *  @return JSON JSON Object.
     **/
    public static JSONObject getJSON() {
        return JSON;
    }

    public static void setJSON(JSONObject JSON) {
        KernelUpdater.JSON = JSON;
    }


    /**
     *  Gets Update code from KernelUtils.
     *  @param updateValue JSON String Object.
     **/

    /*
        0 - Latest version installed
        1 - Update Available
       -1 - Unknown version installed
       -2 - Cannot check for updates
     */
    public static void setUpdateValue(int updateValue) {
        KernelUpdater.updateValue = updateValue;
    }

    public static int getUpdateValue() {
        return updateValue;
    }


    /**
     *  Gets DownloadURL from KernelSplash.
     *  @param downloadURL String URL.
     **/
    public static void setDownloadURL(String downloadURL) {
        KernelUpdater.downloadURL = downloadURL;
    }

    public static String getDownloadURL() {
        return downloadURL;
    }

    /**
     *  Gets available kernels for a particular version.
     *  @param position Spinner position.
     *  @return kernel_list List of kernels, depending on the OS type.
     **/
    public static ArrayList getKernelVersionList(int position) throws JSONException {
        JSONArray versions = KernelUpdater.getJSON().getJSONArray("versions");
        ArrayList<String> kernel_list = new ArrayList<>();
        ArrayList<String> download_list = new ArrayList<>();
        JSONObject kernel_versions = versions.getJSONObject(position);
        JSONArray kernels = kernel_versions.getJSONArray("type");
        for(int i = 0; i < kernels.length(); i++) {
            JSONObject OSList = kernels.getJSONObject(i);
            kernel_list.add(OSList.get("OS").toString());
            download_list.add(OSList.get("URL").toString());
        }
        KernelUpdater.setDownloadList(download_list);
        return kernel_list;
    }

    /**
     *  Requests and checks whether storage permission is granted.
     *  @param context HomeFragment or DownloadFragment.
     *  @return boolean
     **/
    public static boolean getStoragePermission(final Context context, Activity activity){
        if (ActivityCompat.checkSelfPermission(context,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
            return true;
        } else {
            Log.v(TAG,"Permission is revoked");
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.permission_request))
                        .setMessage(context.getString(R.string.storage_permission))
                        .setPositiveButton(context.getString(R.string.continue_settings), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent
                                        (android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + context.getPackageName()));
                                context.startActivity(i);
                            }
                        })
                        .setNegativeButton(context.getString(R.string.not_now), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: Cancelled Permission from App Info");
                            }
                        })
                        .show();
            }
            else
                ActivityCompat.requestPermissions((Activity)context,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    /**
     *  Requests and checks whether storage permission is granted.
     *  @param context HomeFragment or DownloadFragment.
     *  @param filename kernel update file or downloaded file.
     **/
    public static void flashFile(Context context, final String filename) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.flash_kernel))
                .setMessage(context.getString(R.string.flash_confirm))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String command = "install " + filename;
                        RootUtils.SU su = RootUtils.getSU();
                        su.runCommand("echo " + command + " >> " + OPENRECOVERY_SCRIPT_FILE);
                        su.runCommand("reboot recovery");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Canceled Flash");
                    }
                })
                .show();
    }

    /**
     *  Checks whether there is an active internet connection
     *  Taken from http://stackoverflow.com/a/4239019
     *  @return Network Connectivity information
     **/
    // Checks for active internet connection.
    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean networkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        return networkAvailable && checkConnection();
    }

    // Check whether the internet connection actually works.
    private static boolean checkConnection() {
        boolean isConnected = false;
        try {
            Process process = Runtime.getRuntime()
                    .exec("ping -c 1 www.google.com");
            int returnVal = process.waitFor();
            isConnected = (returnVal == 0);
        } catch (Exception ignored) {}
        return isConnected;
    }

}

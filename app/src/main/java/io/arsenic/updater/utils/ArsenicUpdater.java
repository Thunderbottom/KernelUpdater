package io.arsenic.updater.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ArsenicUpdater {

    private static ArrayList downloadList;
    private static String kernelVersion;
    private static String kernelValue;
    private static JSONObject JSON;
    private static int updateValue = 0;


    /**
     *  Gets List of download for specified kernel.
     *  @param downloadList JSON String Object.
     **/
    private static void setDownloadList(ArrayList downloadList) {
        ArsenicUpdater.downloadList = downloadList;
    }

    public static ArrayList getDownloadList() {
        return downloadList;
    }


    /**
     *  Gets Current Kernel Version from ArsenicUtils.
     *  @param kernelVersion JSON String Object.
     **/
    static void setKernelValue(String kernelVersion) {
        ArsenicUpdater.kernelValue = kernelVersion;
    }

    public static String getKernelValue() {
        return kernelValue;
    }


    /**
     *  Gets Current Kernel Version from ArsenicUtils.
     *  @param kernelVersion JSON String Object.
     **/
     static void setKernelVersion(String kernelVersion) {
        ArsenicUpdater.kernelVersion = kernelVersion;
    }

    public static String getKernelVersion() {
        return kernelVersion;
    }


    /**
     *  Gets JSON Object from ArsenicUtils.
     *  @return JSON JSON Object.
     **/
    public static JSONObject getJSON() {
        return JSON;
    }

    public static void setJSON(JSONObject JSON) {
        ArsenicUpdater.JSON = JSON;
    }


    /**
     *  Gets Update code from ArsenicUtils.
     *  @param updateValue JSON String Object.
     **/

    /*
        0 - Latest version installed
        1 - Update Available
       -1 - Unknown version installed
       -2 - Cannot check for updates
     */
    public static void setUpdateValue(int updateValue) {
        ArsenicUpdater.updateValue = updateValue;
    }

    public static int getUpdateValue() {
        return updateValue;
    }


    /**
     *  Gets available kernels for a particular version.
     *  @param position Spinner position.
     *  @return kernel_list List of kernels, depending on the OS type.
     **/
    public static ArrayList getKernelVersionList(int position) throws JSONException {
        JSONArray versions = ArsenicUpdater.getJSON().getJSONArray("versions");
        ArrayList<String> kernel_list = new ArrayList<>();
        ArrayList<String> download_list = new ArrayList<>();
        JSONObject kernel_versions = versions.getJSONObject(position);
        JSONArray kernels = kernel_versions.getJSONArray("type");
        for(int i = 0; i < kernels.length(); i++) {
            JSONObject OSList = kernels.getJSONObject(i);
            kernel_list.add(OSList.get("OS").toString());
            download_list.add(OSList.get("URL").toString());
        }
        ArsenicUpdater.setDownloadList(download_list);
        return kernel_list;
    }
}

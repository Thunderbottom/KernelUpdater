package io.arsenic.updater.utils;

import org.json.JSONObject;


public class ArsenicUpdater {

    private static String kernelVersion;
    private static String kernelValue;
    private static JSONObject JSON;
    private static int updateValue = 0;

    /**
     *  Gets Current Kernel Version from ArsenicUtils
     *  @param kernelVersion JSON String Object
     **/
    static void setKernelValue(String kernelVersion) {
        ArsenicUpdater.kernelValue = kernelVersion;
    }

    public static String getKernelValue() {
        return kernelValue;
    }


    /**
     *  Gets Current Kernel Version from ArsenicUtils
     *  @param kernelVersion JSON String Object
     **/
     static void setKernelVersion(String kernelVersion) {
        ArsenicUpdater.kernelVersion = kernelVersion;
    }

    public static String getKernelVersion() {
        return kernelVersion;
    }

    /**
     *  Gets Update code from ArsenicUtils
     *  @param updateValue JSON String Object
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
}

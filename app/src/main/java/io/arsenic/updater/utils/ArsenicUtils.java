package io.arsenic.updater.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ArsenicUtils {
    public static String readKernelVersion(String val) {
        try {
            Process p = Runtime.getRuntime().exec("uname -a");
            InputStream is = null;
            if (p.waitFor() == 0) {
                is = p.getInputStream();
            } else {
                is = p.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            br.close();
            Log.i("TAG: LINUX", line);
            String [] str = line.split(val);
            Log.i("TAG: VAL", str[1].trim());
            return line;
        } catch (Exception ex) {
            return "ERROR: " + ex.getMessage();
        }
    }

}

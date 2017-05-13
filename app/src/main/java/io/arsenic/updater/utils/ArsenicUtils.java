package io.arsenic.updater.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArsenicUtils {

    private static String LOG_TAG = "ArsenicUtils";


    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename), 256)) {
            return reader.readLine();
        }
    }

    /**
     * Reads FILENAME_PROC_VERSION, formats it using @link formatKernelVersion and returns
     * @return String with the final kernel version
     * */
    public static void getFormattedKernelVersion() {
        try {
            String FILENAME_PROC_VERSION = "/proc/version";
            formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            Log.e(LOG_TAG,
                    "IO Exception when getting kernel version for Device Info screen",
                    e);
        }
    }

    /**
     * Takes raw kernel version and formats it using a regex to readable format
     * @param rawKernelVersion kernel version as read from /proc/version
     * @return String formatted kernel version
     * */
    private static void formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
                "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
                        "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
                        "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
                        "(#\\d+) " +              /* group 3: "#1" */
                        "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
                        "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            ArsenicUpdater.setKernelValue("Unavailable");
        } else if (m.groupCount() < 4) {
            Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            ArsenicUpdater.setKernelValue("Unavailable");
        }
        ArsenicUpdater.setKernelVersion(m.group(1));
        ArsenicUpdater.setKernelValue(m.group(1) + "\n"     // LinuxKernelVersion-KernelName
                + m.group(2) + " "                          // x@y.com #1
                + m.group(3) + "\n"                         // Day Month Date HH:MM:SS Timezone Year
                + m.group(4));
    }
}

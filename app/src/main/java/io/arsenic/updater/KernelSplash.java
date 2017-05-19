package io.arsenic.updater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import io.arsenic.updater.utils.JSONService;
import io.arsenic.updater.utils.KernelUpdater;
import io.arsenic.updater.utils.KernelUtils;
import io.arsenic.updater.utils.RootUtils;

public class KernelSplash extends Activity {

    private static int updateValue = -2;
    static String remoteKernelVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("theme", Activity.MODE_PRIVATE);
        int app_theme;
        if(sp.getInt("theme_id", 0) == 0)
            app_theme = R.style.SplashTheme;
        else
            app_theme = R.style.SplashThemeDark;
        setTheme(app_theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!RootUtils.rootAccess()) {
            Intent intent = new Intent(KernelSplash.this, DeniedRootActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            new UpdateTask().execute(KernelSplash.this);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(KernelSplash.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    KernelSplash.this.startActivity(intent);
                    KernelSplash.this.finish();
                }
            }, 2500);
        }
    }

    /**
     *  Gets JSON from the specified URL.
     *  Stores JSON in KernelUpdater.setJSON()
     **/
    private static class UpdateTask extends AsyncTask<Activity, Void, Void> {
        @Override
        protected Void doInBackground(Activity... activities) {
            KernelUtils.getFormattedKernelVersion();
            String currentKernelVersion = KernelUpdater
                    .getKernelVersion()
                    .replaceAll("\\D+", "");
            if(KernelUpdater.isNetworkAvailable(activities[0])) {
                String jsonStr = JSONService.request(activities[0].getResources().getString(R.string.update_url), JSONService.GET);
                try {
                    JSONObject json = new JSONObject(jsonStr);
                    KernelUpdater.setJSON(json);
                    if (KernelUpdater.getKernelVersion().contains("lineage")) {
                        JSONObject OS = json.getJSONObject("Lineage");
                        remoteKernelVersion = OS.getString("version");
                        KernelUpdater.setDownloadURL(OS.getString("link"));
                    } else if (KernelUpdater.getKernelVersion().contains("aosp") ||
                            currentKernelVersion.equals("34032")) {
                        JSONObject OS = json.getJSONObject("AOSP");
                        remoteKernelVersion = OS.getString("version");
                        KernelUpdater.setDownloadURL(OS.getString("link"));
                    } else if (Integer.parseInt(currentKernelVersion) < 34032) {
                        JSONObject OS = json.getJSONObject("OOS");
                        remoteKernelVersion = OS.getString("version");
                        KernelUpdater.setDownloadURL(OS.getString("link"));
                    } else {
                        KernelUpdater.setUpdateValue(-3);
                        return null;
                    }
                    if (Integer.parseInt(remoteKernelVersion) > Integer.parseInt(currentKernelVersion))
                        updateValue = 1;
                    else if (Integer.parseInt(remoteKernelVersion) < Integer.parseInt(currentKernelVersion))
                        updateValue = -1;
                    else if (remoteKernelVersion.equals(currentKernelVersion))
                        updateValue = 0;
                } catch (JSONException ignored) {
                }
            }
            else
                updateValue = -2;
                KernelUpdater.setUpdateValue(updateValue);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
package io.arsenic.updater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import io.arsenic.updater.utils.JSONService;
import io.arsenic.updater.utils.KernelUpdater;
import io.arsenic.updater.utils.KernelUtils;
import io.arsenic.updater.utils.RootUtils;

public class KernelSplash extends Activity {

    private static int updateValue = -2;
    static String remoteKernelVersion;
    int app_theme, icon_theme;

    ImageView splash_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("theme", Activity.MODE_PRIVATE);
        if(sp.getInt("theme_id", 0) == 0) {
            app_theme = R.style.SplashTheme;
            icon_theme = R.drawable.ic_icon;
        }
        else {
            app_theme = R.style.SplashThemeDark;
            icon_theme = R.drawable.ic_icon_dark;
        }
        setTheme(app_theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splash_image = (ImageView) findViewById(R.id.splash_image);
        splash_image.setImageResource(icon_theme);
        KernelUpdater.setIcon(icon_theme);
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
                    String OS_type;
                    if (KernelUpdater.getKernelVersion().contains("lineage")) {
                        OS_type = "Lineage";
                    } else if (KernelUpdater.getKernelVersion().contains("aosp") ||
                            currentKernelVersion.equals("34032")) {
                        OS_type = "AOSP";
                    } else if (Integer.parseInt(currentKernelVersion) < 34032) {
                        OS_type = "OOS";
                    } else {
                        KernelUpdater.setUpdateValue(-3);
                        return null;
                    }
                    JSONObject OS = json.getJSONObject(OS_type);
                    remoteKernelVersion = OS.getString("version");
                    KernelUpdater.setDownloadURL(OS.getString("link"));
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
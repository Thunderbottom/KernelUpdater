package io.arsenic.updater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import io.arsenic.updater.utils.ArsenicUpdater;
import io.arsenic.updater.utils.ArsenicUtils;
import io.arsenic.updater.utils.JSONService;
import io.arsenic.updater.utils.RootUtils;

public class ArsenicSplash extends Activity {

    private static int updateValue = -2;
    private int app_theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("theme", Activity.MODE_PRIVATE);
        if(sp.getInt("theme_id", 0) == 0) {
            app_theme = R.style.SplashTheme;
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }
        else {
            app_theme = R.style.SplashThemeDark;
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        }
        setTheme(app_theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arsenic_splash);
        new UpdateTask().execute();
        new Handler().postDelayed(new Runnable(){
            public void run() {
                Intent intent = new Intent(ArsenicSplash.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                ArsenicSplash.this.startActivity(intent);
                ArsenicSplash.this.finish();
            }
        }, 2500);
    }


    private class UpdateTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ArsenicUtils.getFormattedKernelVersion();
            String currentKernelVersion = ArsenicUpdater
                    .getKernelVersion()
                    .replaceAll("\\D+", "");
            String jsonStr = JSONService.request(getResources().getString(R.string.update_url), JSONService.GET);
            try {
                JSONObject json = new JSONObject(jsonStr);
                JSONObject kernel = json.getJSONObject("kernel");
                String remoteKernelVersion = kernel.getString("version");
                if (Integer.parseInt(remoteKernelVersion) > Integer.parseInt(currentKernelVersion))
                    updateValue = 1;
                else if (Integer.parseInt(remoteKernelVersion) < Integer.parseInt(currentKernelVersion))
                    updateValue = -1;
                else if (remoteKernelVersion.equals(currentKernelVersion))
                    updateValue = 0;
                ArsenicUpdater.setUpdateValue(updateValue);
            } catch (JSONException ignored) { }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!RootUtils.rootAccess()) {
                //TODO: Add a blank activity showing no root access.
                ArsenicSplash.this.finish();
            }
            super.onPostExecute(aVoid);
        }
    }
}
package io.arsenic.updater;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import io.arsenic.updater.utils.ArsenicUpdater;
import io.arsenic.updater.utils.ArsenicUtils;
import io.arsenic.updater.utils.JSONService;
import io.arsenic.updater.utils.RootUtils;

public class ArsenicSplash extends Activity {

    private static String UPDATE_JSON = "";
    private static int updateValue = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arsenic_splash);
        UPDATE_JSON = getResources().getString(R.string.update_url); // Add URL -> R.string.update_url
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
            String jsonStr = JSONService.request(UPDATE_JSON, JSONService.GET);
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
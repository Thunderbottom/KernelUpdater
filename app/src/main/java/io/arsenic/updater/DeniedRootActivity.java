package io.arsenic.updater;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;

public class DeniedRootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("theme", Activity.MODE_PRIVATE);
        int app_theme;
        if(sp.getInt("theme_id", 0) == 0) {
            app_theme = R.style.RootDeniedTheme;
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }
        else {
            app_theme = R.style.RootDeniedDarkTheme;
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        }
        setTheme(app_theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denied_root);
    }
}

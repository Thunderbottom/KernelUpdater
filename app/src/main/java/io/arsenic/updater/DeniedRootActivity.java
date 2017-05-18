package io.arsenic.updater;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DeniedRootActivity extends AppCompatActivity {

    Unbinder unbinder;
    @BindView(R.id.stupidButton) Button stupidButton;

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
        unbinder = ButterKnife.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.stupidButton)
    public void stupidButton(){
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}

package io.arsenic.updater;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;

import io.arsenic.updater.fragments.AboutFragment;
import io.arsenic.updater.fragments.DownloadFragment;
import io.arsenic.updater.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {

    int app_theme;
    int bg_color;
    int accent_color;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("theme", Activity.MODE_PRIVATE);
        if(sp.getInt("theme_id", 0) == 0) {
            app_theme = R.style.AppTheme;
            bg_color = ContextCompat.getColor(this, R.color.colorPrimary);
            accent_color = ContextCompat.getColor(this, R.color.dark_colorPrimary);
        }
        else {
            app_theme = R.style.DarkAppTheme;
            bg_color = ContextCompat.getColor(this, R.color.dark_colorPrimary);
            accent_color = ContextCompat.getColor(this, R.color.colorPrimary);
        }
        setTheme(app_theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pushFragment(new HomeFragment());
        AHBottomNavigation navigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        navigation.setDefaultBackgroundColor(bg_color);
        navigation.setAccentColor(accent_color);
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.navigation);
        navigationAdapter.setupWithBottomNavigation(navigation);
        navigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                selectFragment(position);
                return true;
            }

        });
    }

    protected void selectFragment(int item) {
        switch (item) {
            case 0:
                pushFragment(new HomeFragment());
                break;
            case 1:
                pushFragment(new DownloadFragment());
                break;
            case 2:
                pushFragment(new AboutFragment());
                break;
        }
    }

    protected void pushFragment(Fragment fragment) {
        if (fragment == null)
            return;
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (ft != null) {
                ft.replace(R.id.content, fragment);
                ft.commit();
            }
        }
    }
}

package io.arsenic.updater;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import io.arsenic.updater.fragments.AboutFragment;
import io.arsenic.updater.fragments.DownloadFragment;
import io.arsenic.updater.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {

    int app_theme;
    int bg_color;
    int accent_color;
    Fragment fragment = null;

    private AccountHeader headerResult = null;
    private Drawer result = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("theme", Activity.MODE_PRIVATE);
        if(sp.getInt("theme_id", 0) == 0) {
            app_theme = R.style.AppTheme;
            bg_color = R.color.colorPrimary;
            accent_color = ContextCompat.getColor(this, R.color.dark_colorPrimary);
        }
        else {
            app_theme = R.style.DarkAppTheme;
            bg_color = R.color.dark_colorPrimary;
            accent_color = ContextCompat.getColor(this, R.color.colorPrimary);
        }

        setTheme(app_theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        buildHeader(savedInstanceState);
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.title_home).withIcon(CommunityMaterial.Icon.cmd_home),
                        new PrimaryDrawerItem().withName(R.string.title_download).withIcon(CommunityMaterial.Icon.cmd_download),
                        new PrimaryDrawerItem().withName(R.string.title_about).withIcon(CommunityMaterial.Icon.cmd_information),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.settings).withIcon(CommunityMaterial.Icon.cmd_account_settings)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            switch (position) {
                                case 1:
                                    fragment = new HomeFragment();
                                    break;
                                case 2:
                                    fragment = new DownloadFragment();
                                    break;
                                case 3:
                                    fragment = new AboutFragment();
                                    break;
                            }
                            pushFragment(fragment);
                        }

                        return false;
                    }
                }).build();
        pushFragment(new HomeFragment());
    }



    @Override
    public void onBackPressed() {
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void buildHeader(Bundle savedInstanceState) {
            headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(bg_color)
                    .withCompactStyle(false)
                    .withSavedInstance(savedInstanceState)
                    .build();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }
}

package io.arsenic.updater.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.arsenic.updater.BuildConfig;
import io.arsenic.updater.R;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


public class AboutFragment extends Fragment {


    int click = 0;
    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Element versionElement = new Element();
        versionElement
                .setTitle("v" + BuildConfig.VERSION_NAME)
                .setGravity(Gravity.CENTER);
        versionElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click +=1;
                if (click % 5 == 0){
                    Toast.makeText(getContext(), getString(R.string.easter_egg), Toast.LENGTH_SHORT).show();
                    SharedPreferences settings = getActivity().getSharedPreferences("theme", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("theme_id", 1-getThemeId())
                            .apply();
                    Intent i = getActivity().getBaseContext().getPackageManager().
                            getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    getActivity().finish();
                }
            }
        });

        return new AboutPage(getActivity())
                .setDescription(getString(R.string.about_desc))
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .addGroup("Connect with us")
                .addEmail("nimitmehta95@gmail.com")
                .addWebsite("http://checkyourscreen.me")
                .addTwitter("Th3_0bserver")
                .addGitHub("thunderbottom/ArsenicUpdater")
                .addItem(versionElement)
                .create();
    }


    int getThemeId() {
        TypedValue outValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.theme_name, outValue, true);
        if ("dark".equals(outValue.string)) return 1;
        else return 0;
    }
}

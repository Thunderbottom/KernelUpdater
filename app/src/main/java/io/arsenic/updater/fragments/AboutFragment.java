package io.arsenic.updater.fragments;


import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.arsenic.updater.BuildConfig;
import io.arsenic.updater.R;
import io.arsenic.updater.utils.KernelUpdater;
import io.arsenic.updater.views.LibraryAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    Unbinder unbinder;
    View aboutView;
    ArrayList<String> library_names;
    ArrayList<String> library_versions;
    ArrayList<String> library_descriptions;
    ArrayList<String> library_urls;
    CustomTabsIntent.Builder builder;
    CustomTabsIntent customTabIntent;
    TextView appVersion;
    int click = 0;

    final String GITHUB_URL = "https://github.com/";
    final String TWITTER_URL = "https://twitter.com/";

    @BindView(R.id.about_card_view) CardView aboutCard;
    @BindView(R.id.contact_card_view) CardView contactCard;
    @BindView(R.id.github_card_view) CardView githubCard;
    @BindView(R.id.imageView) ImageView about_image;
    @BindView(R.id.website_card_view) CardView websiteCard;
    @BindView(R.id.twitter_card_view) CardView twitterCard;

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        aboutView = inflater.inflate(R.layout.fragment_about, container, false);
        unbinder = ButterKnife.bind(this, aboutView);
        about_image.setImageResource(KernelUpdater.getIcon());
        RecyclerView libraryRV = (RecyclerView) aboutView.findViewById(R.id.libraryRecyclerView);
        libraryRV.setHasFixedSize(true);
        initViews();
        appVersion = (TextView) aboutView.findViewById(R.id.appVersion);
        appVersion.setText(String.valueOf(BuildConfig.VERSION_NAME));
        builder = new CustomTabsIntent.Builder();
        return aboutView;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.about_card_view)
    public void aboutCard(){
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


    @OnClick(R.id.contact_card_view)
    public void contactCard(){
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{ getString(R.string.email)});
        email.setType("message/rfc822");
        try {
            startActivity(Intent.createChooser(email, "Choose an Email client :"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.github_card_view)
    public void githubCard(){
        builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.github));
        customTabIntent = builder.build();
        customTabIntent.launchUrl(getContext(), Uri.parse(GITHUB_URL + getString(R.string.github)));
    }

    @OnClick(R.id.twitter_card_view)
    public void twitterCard(){
        builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.twitter));
        customTabIntent = builder.build();
        customTabIntent.launchUrl(getContext(), Uri.parse(TWITTER_URL + getString(R.string.twitter)));
    }

    @OnClick(R.id.website_card_view)
    public void websiteCard(){
        builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.dark_colorPrimary));
        customTabIntent = builder.build();
        customTabIntent.launchUrl(getContext(), Uri.parse(getString(R.string.website)));
    }

    private void initViews(){
        RecyclerView recyclerView = (RecyclerView) aboutView.findViewById(R.id.libraryRecyclerView);
        library_names        = new ArrayList<>(Arrays.asList(getContext().getResources().getStringArray(R.array.library_name)));
        library_versions     = new ArrayList<>(Arrays.asList(getContext().getResources().getStringArray(R.array.library_version)));
        library_descriptions = new ArrayList<>(Arrays.asList(getContext().getResources().getStringArray(R.array.library_description)));
        library_urls         = new ArrayList<>(Arrays.asList(getContext().getResources().getStringArray(R.array.library_urls)));
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.Adapter adapter = new LibraryAdapter(library_names, library_versions, library_descriptions);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getActivity().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if(child != null && gestureDetector.onTouchEvent(e)) {
                    int position = rv.getChildAdapterPosition(child);
                    builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.github));
                    customTabIntent = builder.build();
                    customTabIntent.launchUrl(getContext(), Uri.parse(library_urls.get(position)));
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    int getThemeId() {
        TypedValue outValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.theme_name, outValue, true);
        if ("dark".equals(outValue.string)) return 1;
        else return 0;
    }

}

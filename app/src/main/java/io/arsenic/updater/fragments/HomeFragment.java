package io.arsenic.updater.fragments;


import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.arsenic.updater.R;
import io.arsenic.updater.utils.ArsenicUpdater;

public class HomeFragment extends Fragment {

    TextView kernelVersion;
    String kVersion;

    private Unbinder unbinder;

    @BindView(R.id.update_container) View updateContainer;
    @BindView(R.id.update_check_progress) ProgressBar updateProgressBar;
    @BindView(R.id.update_status) TextView updateStatus;
    @BindView (R.id.update_icon) ImageView updateIcon;
    @BindView(R.id.downloadUpdate_card_view) CardView updateCardView;

    @BindColor(R.color.red_500) int colorBad;
    @BindColor(R.color.green_500) int colorOK;
    @BindColor(R.color.yellow_500) int colorWarn;
    @BindColor(R.color.grey_500) int colorNeutral;
    @BindColor(R.color.blue_500) int colorInfo;
    @BindColor(android.R.color.transparent) int trans;

    public void checkForUpdates() {
        int updateImage, updateColor, updateText;
        updateProgressBar.setVisibility(View.VISIBLE);
        updateContainer.setBackgroundColor(trans);
        if(isNetworkAvailable()) {
            // Internet Connection Available
            if(ArsenicUpdater.getKernelVersion()
                    .toLowerCase()
                    .contains(getString(R.string.arsenic).toLowerCase())) {
                // Installed Kernel is Arsenic
                switch (ArsenicUpdater.getUpdateValue()) {
                    case 0:
                        updateColor = colorOK;
                        updateImage = R.drawable.ic_check;
                        updateText = R.string.latestVersion;
                        break;
                    case 1:
                        updateColor = colorInfo;
                        updateImage = R.drawable.ic_update;
                        updateText = R.string.newerVersion;
                        updateCardView.setVisibility(View.VISIBLE);
                        break;
                    case -2:
                        updateColor = colorNeutral;
                        updateImage = R.drawable.ic_help;
                        updateText  = R.string.failedUpdate;
                        break;
                    default:
                        updateColor = colorBad;
                        updateImage = R.drawable.ic_cancel;
                        updateText  = R.string.unknownVersion;
                        break;
                }
            }
            else {
                // Installed kernel is not Arsenic
                updateColor = colorBad;
                updateImage = R.drawable.ic_cancel;
                updateText = R.string.unknownKernel;
            }
        }
        else{
            // No Internet Connection
            updateColor = colorNeutral;
            updateImage = R.drawable.ic_help;
            updateText = R.string.cannotCheckUpdate;
        }
        updateContainer.setBackgroundColor(updateColor);
        updateIcon.setImageResource(updateImage);
        updateStatus.setText(updateText);
        updateStatus.setTextColor(updateColor);
        updateProgressBar.setVisibility(View.INVISIBLE);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, homeView);
        kernelVersion = (TextView) homeView.findViewById(R.id.kvTextView);
        kVersion = ArsenicUpdater.getKernelValue();
        kernelVersion.setText(kVersion);
        checkForUpdates();
        return homeView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    /**
     *  Checks whether there is an active internet connection
     *  @url http://stackoverflow.com/a/4239019
     *  @return Network Connectivity information
     **/
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}

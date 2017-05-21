package io.arsenic.updater.fragments;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.arsenic.updater.R;
import io.arsenic.updater.utils.KernelUpdater;
import io.arsenic.updater.utils.RootUtils;
import io.arsenic.updater.utils.UpdateDownloader;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static UpdateDownloader updateDownloader;

    RootUtils.SU su;
    View homeView;

    private Unbinder unbinder;

    @BindView(R.id.update_container) View updateContainer;
    @BindView(R.id.update_status) TextView updateStatus;
    @BindView (R.id.update_icon) ImageView updateIcon;
    @BindView(R.id.downloadUpdate_card_view) CardView updateCardView;

    @BindView(R.id.kvTextView) TextView kvTextView;

    @BindColor(R.color.red_500) int colorBad;
    @BindColor(R.color.green_500) int colorOK;
    @BindColor(R.color.grey_500) int colorNeutral;
    @BindColor(R.color.blue_500) int colorInfo;
    @BindColor(android.R.color.transparent) int trans;

    @BindView(R.id.rebootBootloader_card_view) CardView rebootBootloader;
    @BindView(R.id.rebootRecovery_card_view) CardView rebootRecovery;

    public void checkForUpdates() {
        int updateImage, updateColor, updateText;
        updateContainer.setBackgroundColor(trans);
        if(KernelUpdater.getKernelVersion()
                .toLowerCase()
                .contains(getString(R.string.arsenic).toLowerCase())) {
            // Installed Kernel is Arsenic
            switch (KernelUpdater.getUpdateValue()) {
                case 0:
                    updateColor = colorOK;
                    updateImage = R.drawable.ic_check;
                    updateText = R.string.latest_version;
                    break;
                case 1:
                    updateColor = colorInfo;
                    updateImage = R.drawable.ic_update;
                    updateText = R.string.newer_version;
                    updateCardView.setVisibility(View.VISIBLE);
                    break;
                case -2:
                    updateColor = colorNeutral;
                    updateImage = R.drawable.ic_help;
                    updateText  = R.string.failed_update_check;
                    break;
                default:
                    updateColor = colorBad;
                    updateImage = R.drawable.ic_cancel;
                    updateText  = R.string.unknown_version;
                    break;
            }
        }
        else {
            // Installed kernel is not Arsenic
            updateColor = colorBad;
            updateImage = R.drawable.ic_cancel;
            updateText = R.string.unknown_kernel;
        }
        updateContainer.setBackgroundColor(updateColor);
        updateIcon.setImageResource(updateImage);
        updateStatus.setText(updateText);
        updateStatus.setTextColor(updateColor);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        homeView = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, homeView);
        kvTextView.setText(KernelUpdater.getKernelValue());
        updateDownloader = new UpdateDownloader(
                            getActivity(), getContext(), homeView);
        checkForUpdates();
        return homeView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.rebootRecovery_card_view)
    public void rebootRecovery(){
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.reboot_recovery_text))
                .setMessage(getString(R.string.reboot_confirm))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        su = RootUtils.getSU();
                        su.runCommand("reboot recovery");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancelled Reboot Bootloader");
                    }
                })
                .show();
    }

    @OnClick(R.id.rebootBootloader_card_view)
    public void rebootBootloader(){
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.reboot_bootloader_text))
                .setMessage(getString(R.string.reboot_confirm))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        su = RootUtils.getSU();
                        su.runCommand("reboot bootloader");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancelled Reboot Bootloader");
                    }
                })
                .show();
    }

    @OnClick(R.id.downloadUpdate_card_view)
    public void downloadUpdate() {
        updateDownloader.downloadUpdate();
    }

}
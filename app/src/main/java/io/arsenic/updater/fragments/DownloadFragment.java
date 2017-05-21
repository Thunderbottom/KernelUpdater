package io.arsenic.updater.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.arsenic.updater.R;
import io.arsenic.updater.utils.KernelUpdater;
import io.arsenic.updater.views.DataAdapter;


public class DownloadFragment extends Fragment {

    public static final int TIMEOUT = 1000;

    View downloadView;
    private Spinner downloadSpinner;
    ProgressDialog mProgressDialog;
    NotificationManager notificationManager;
    NotificationCompat.Builder notification;
    String filename;
    Unbinder unbinder;

    @BindView(R.id.searchButton) Button searchButton;
    @BindView(R.id.dismiss) TextView dismiss;
    @BindView(R.id.never_show) TextView never_show;
    @BindView(R.id.help_card_view) CardView helpCard;

    public DownloadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        downloadView = inflater.inflate(R.layout.fragment_download, container, false);
        unbinder = ButterKnife.bind(this, downloadView);
        SharedPreferences sp = getContext().getSharedPreferences("view", Activity.MODE_PRIVATE);
        if (sp.getInt("helpCardVisible", 1) == 0) {
            dismiss();
        }
        downloadSpinner = (Spinner) downloadView.findViewById(R.id.spinner);
        try {
            getVersions();
        } catch (JSONException ignored) {
            Toast.makeText(getContext(), getString(R.string.kernel_version_fail), Toast.LENGTH_SHORT).show();
        }
        return downloadView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.searchButton)
    public void searchButton(){
        if (KernelUpdater.getStoragePermission(getContext(), getActivity())) {
            try {
                initViews(downloadSpinner.getSelectedItemPosition());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.dismiss)
    public void dismiss() {
        helpCard.setVisibility(View.GONE);
    }

    @OnClick(R.id.never_show)
    public void neverShow() {
        SharedPreferences settings = getActivity().getSharedPreferences("view", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("helpCardVisible", 0)
                .apply();
        dismiss();
    }

    /**
     * Initialize RecyclerView for download cards.
     * @throws JSONException for problems with JSON
     **/
    private void initViews(int position) throws JSONException {
        RecyclerView recyclerView = (RecyclerView) downloadView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        ArrayList arsenic_list = KernelUpdater.getKernelVersionList(position);
        RecyclerView.Adapter<DataAdapter.ViewHolder > adapter = new DataAdapter(arsenic_list, DownloadFragment.this);
        recyclerView.setAdapter(adapter);
    }


    /**
     * Get all available kernel version from remote JSON.
     * @throws JSONException for problems with JSON
     **/
    public void getVersions() throws JSONException {
        JSONArray versions = KernelUpdater.getJSON().getJSONArray("versions");
        List<String> version_list = new ArrayList<>();
        for(int i = 0; i < versions.length(); i ++){
            JSONObject kernel_versions = versions.getJSONObject(i);
            version_list.add(kernel_versions.get("kernel_version").toString());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, version_list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        downloadSpinner.setAdapter(spinnerAdapter);
    }

    public void downloadFile(String URL) {
        HomeFragment.updateDownloader.downloadUpdate(URL);
    }

}
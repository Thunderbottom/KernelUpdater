package io.arsenic.updater.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.arsenic.updater.R;
import io.arsenic.updater.utils.ArsenicUpdater;
import io.arsenic.updater.views.DataAdapter;


public class DownloadFragment extends Fragment{

    View downloadView;
    Button searchButton;
    private Spinner downloadSpinner;

    public DownloadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        downloadView = inflater.inflate(R.layout.fragment_download, container, false);
        downloadSpinner = (Spinner) downloadView.findViewById(R.id.spinner);
        try {
            getVersions();
        } catch (JSONException ignored) {
            Toast.makeText(getContext(), "Failed to get kernel versions", Toast.LENGTH_SHORT).show();
        }
        searchButton = (Button) downloadView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initViews(downloadSpinner.getSelectedItemPosition());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return downloadView;
    }

    private void initViews(int position) throws JSONException {
        RecyclerView recyclerView = (RecyclerView) downloadView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<String> arsenic_list = ArsenicUpdater.getKernelVersionList(position);
        RecyclerView.Adapter<DataAdapter.ViewHolder> adapter = new DataAdapter(arsenic_list);
        recyclerView.setAdapter(adapter);
    }

    public void getVersions() throws JSONException {
        JSONArray versions = ArsenicUpdater.getJSON().getJSONArray("versions");
        List<String> version_list = new ArrayList<>();
        for(int i = 0; i < versions.length(); i ++){
            JSONObject kernel_versions = versions.getJSONObject(i);
            version_list.add(kernel_versions.get("kernel_version").toString());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, version_list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        downloadSpinner.setAdapter(spinnerAdapter);
    }
}

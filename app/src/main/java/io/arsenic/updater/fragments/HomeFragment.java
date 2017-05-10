package io.arsenic.updater.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import io.arsenic.updater.R;
import io.arsenic.updater.utils.ArsenicUtils;


public class HomeFragment extends Fragment {

    TextView kernelVersion;
    String kVersion;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ScrollView homeView = (ScrollView) inflater.inflate(R.layout.fragment_home, container, false);
        kernelVersion = (TextView) homeView.findViewById(R.id.kvTextView);
        kVersion = ArsenicUtils.getFormattedKernelVersion();
        kernelVersion.setText(kVersion);
        return homeView;
    }
}

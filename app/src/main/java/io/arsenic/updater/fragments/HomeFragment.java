package io.arsenic.updater.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.arsenic.updater.R;


public class HomeFragment extends Fragment {

    TextView kernelVersion;
    String kVersion;
    String val = "Linux localhost 3.4.0-Arsenic.Kernel-onyx.";

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ScrollView homeView = (ScrollView) inflater.inflate(R.layout.fragment_home, container, false);
        kernelVersion = (TextView) homeView.findViewById(R.id.kvTextView);
        kVersion = readKernelVersion();
        kernelVersion.setText(kVersion);
        return homeView;
    }

    public String readKernelVersion() {
        try {
            Process p = Runtime.getRuntime().exec("uname -a");
            InputStream is = null;
            if (p.waitFor() == 0) {
                is = p.getInputStream();
            } else {
                is = p.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            br.close();
            Log.i("TAG: LINUX", line);
            String [] str = line.split(val);
            Log.i("TAG: VAL", str[1].trim());
            return line;
        } catch (Exception ex) {
            return "ERROR: " + ex.getMessage();
        }
    }

}

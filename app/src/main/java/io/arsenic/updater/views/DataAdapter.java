package io.arsenic.updater.views;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.arsenic.updater.R;
import io.arsenic.updater.fragments.DownloadFragment;
import io.arsenic.updater.utils.ArsenicUpdater;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private ArrayList<String> arsenic_version;
    private ArrayList<String> downloadList;
    private Button downloadButton;
    private View view;

    public DataAdapter(ArrayList<String> countries) {
        this.arsenic_version = countries;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataAdapter.ViewHolder viewHolder, int i) {
        viewHolder.arsenic_text.setText(arsenic_version.get(i));
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ArsenicUpdater.getStoragePermission(view.getContext())) {
                    downloadList = ArsenicUpdater.getDownloadList();
                    DownloadFragment df = new DownloadFragment();
                    df.downloadFile(downloadList.get(viewHolder.getAdapterPosition()));
                }
                else
                    Toast.makeText(view.getContext(), v.getContext().getString(R.string.downloadFailed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return arsenic_version.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView arsenic_text;
        ViewHolder(View view) {
            super(view);
            downloadButton = (Button)view.findViewById(R.id.downloadButton);
            arsenic_text = (TextView)view.findViewById(R.id.arsenic_version_text);
        }
    }

}
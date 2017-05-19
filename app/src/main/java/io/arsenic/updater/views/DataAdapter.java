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
import io.arsenic.updater.utils.KernelUpdater;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private final DownloadFragment fragment;
    private ArrayList arsenic_version;
    private ArrayList downloadList;
    private Button downloadButton;
    private View view;

    public DataAdapter(ArrayList version, DownloadFragment fragment) {
        this.fragment = fragment;
        this.arsenic_version = version;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataAdapter.ViewHolder viewHolder, int i) {
        viewHolder.arsenic_text.setText((String) arsenic_version.get(i));
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KernelUpdater.getStoragePermission(view.getContext(), fragment.getActivity())) {
                    downloadList = KernelUpdater.getDownloadList();
                    fragment.downloadFile((String) downloadList.get(viewHolder.getAdapterPosition()));
                }
                else
                    Toast.makeText(view.getContext(),
                            v.getContext().getString(R.string.download_failed),
                            Toast.LENGTH_SHORT).show();
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
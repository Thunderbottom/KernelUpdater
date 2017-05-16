package io.arsenic.updater.views;

/**
 * Created by chinmaypai on 5/17/17.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.arsenic.updater.R;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private ArrayList<String> arsenic_version;

    public DataAdapter(ArrayList<String> countries) {
        this.arsenic_version = countries;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {

        viewHolder.arsenic_text.setText(arsenic_version.get(i));
    }

    @Override
    public int getItemCount() {
        return arsenic_version.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView arsenic_text;
        public ViewHolder(View view) {
            super(view);

            arsenic_text = (TextView)view.findViewById(R.id.arsenic_version_text);
        }
    }

}
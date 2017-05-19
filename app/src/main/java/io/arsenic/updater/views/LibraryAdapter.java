package io.arsenic.updater.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.arsenic.updater.R;


public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {
    private ArrayList<String> libraries;
    private ArrayList<String> librariesVersion;
    private ArrayList<String> librariesDescription;

    public LibraryAdapter(ArrayList<String> libraries, ArrayList<String> librariesVersion, ArrayList<String> librariesDescription) {
        this.libraries = libraries;
        this.librariesVersion = librariesVersion;
        this.librariesDescription = librariesDescription;
    }

    @Override
    public LibraryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.library_card_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LibraryAdapter.ViewHolder viewHolder, int i) {

        viewHolder.library_textView.setText(libraries.get(i));
        viewHolder.version_textView.setText(librariesVersion.get(i));
        viewHolder.desc_textView.setText(librariesDescription.get(i));
    }

    @Override
    public int getItemCount() {
        return libraries.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder {
        private TextView library_textView;
        private TextView version_textView;
        private TextView desc_textView;

         ViewHolder(View view) {
            super(view);

            library_textView = (TextView) view.findViewById(R.id.libraryTV);
            version_textView = (TextView) view.findViewById(R.id.versionTV);
            desc_textView    = (TextView) view.findViewById(R.id.descTV);
        }
    }
}
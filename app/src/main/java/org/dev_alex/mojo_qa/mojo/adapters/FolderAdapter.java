package org.dev_alex.mojo_qa.mojo.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.DocumentsFragment;
import org.dev_alex.mojo_qa.mojo.models.File;

import java.util.ArrayList;


public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private DocumentsFragment parentFragment;
    private ArrayList<File> files;
    private boolean isGrid;

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageView moreBtn;

        FolderViewHolder(View itemView) {
            super(itemView);
            folderName = (TextView) itemView.findViewById(R.id.folder_name);
            moreBtn = (ImageView) itemView.findViewById(R.id.more_btn);
        }
    }


    public FolderAdapter(DocumentsFragment parentFragment, ArrayList<File> files, boolean isGrid) {
        this.parentFragment = parentFragment;
        this.files = files;
        this.isGrid = isGrid;
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v;
        if (isGrid)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_folder_grid, viewGroup, false);
        else
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_folder, viewGroup, false);

        return new FolderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FolderViewHolder viewHolder, int i) {
        final File file = files.get(i);
        viewHolder.folderName.setText(file.name);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.new GetFilesTask(file.id,file.name).execute();
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void swapType() {
        isGrid = !isGrid;
    }
}


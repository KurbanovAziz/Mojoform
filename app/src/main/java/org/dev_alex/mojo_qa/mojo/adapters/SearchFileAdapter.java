package org.dev_alex.mojo_qa.mojo.adapters;


import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.SearchFragment;
import org.dev_alex.mojo_qa.mojo.models.File;

import java.util.ArrayList;


public class SearchFileAdapter extends RecyclerView.Adapter<SearchFileAdapter.FileViewHolder> {
    private SearchFragment parentFragment;
    private ArrayList<File> files;

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView fileIcon;
        View separator;

        FileViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileIcon = (ImageView) itemView.findViewById(R.id.file_icon);
            separator = itemView.findViewById(R.id.separator);
        }
    }


    public SearchFileAdapter(SearchFragment parentFragment, ArrayList<File> files) {
        this.parentFragment = parentFragment;
        this.files = files;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_file_search, viewGroup, false);
        return new FileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FileViewHolder viewHolder, int i) {
        final File file = files.get(i);
        viewHolder.fileName.setText(file.name);

        if (parentFragment.bitmapCacheService.hasThumbnailInMemCache(file.id)) {
            Bitmap bitmap = parentFragment.bitmapCacheService.getThumbnailFromMemCache(file.id);
            viewHolder.fileIcon.setImageBitmap(bitmap);
        } else
            viewHolder.fileIcon.setImageResource(R.drawable.file_icon);

        viewHolder.separator.setVisibility(i == files.size() - 1 ? View.GONE : View.VISIBLE);

        if (file.nodeType.equals("cm:content"))
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFragment.new OpenFileTask(file).execute();
                }
            });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}


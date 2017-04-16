package org.dev_alex.mojo_qa.mojo.adapters;


import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.DocumentsFragment;
import org.dev_alex.mojo_qa.mojo.models.File;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private DocumentsFragment parentFragment;
    private ArrayList<File> files;
    private boolean isGrid;

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        TextView fileDate;
        ImageView moreBtn;
        ImageView fileIcon;
        ImageView filePreview;

        FileViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileDate = (TextView) itemView.findViewById(R.id.file_date);
            moreBtn = (ImageView) itemView.findViewById(R.id.more_btn);
            fileIcon = (ImageView) itemView.findViewById(R.id.file_icon);
            filePreview = (ImageView) itemView.findViewById(R.id.file_preview);
        }
    }


    public FileAdapter(DocumentsFragment parentFragment, ArrayList<File> files, boolean isGrid) {
        this.parentFragment = parentFragment;
        this.files = files;
        this.isGrid = isGrid;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v;
        if (isGrid)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_file_grid, viewGroup, false);
        else
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_file, viewGroup, false);

        return new FileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FileViewHolder viewHolder, int i) {
        final File file = files.get(i);
        viewHolder.fileName.setText(file.name);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH.mm", Locale.getDefault());

        if (isGrid) {
            FrameLayout.LayoutParams imageLayoutParams = (FrameLayout.LayoutParams) viewHolder.filePreview.getLayoutParams();
            if (parentFragment.bitmapCacheService.hasPreviewInMemCache(file.id)) {
                imageLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

                Bitmap bitmap = parentFragment.bitmapCacheService.getPreviewFromMemCache(file.id);
                viewHolder.filePreview.setImageBitmap(bitmap);
            } else {
                viewHolder.filePreview.setImageResource(R.drawable.no_image);
                imageLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                        parentFragment.getResources().getDisplayMetrics());
            }
            viewHolder.filePreview.setLayoutParams(imageLayoutParams);
        } else
            viewHolder.fileDate.setText(sdf.format(file.createdAt));

        if (parentFragment.bitmapCacheService.hasThumbnailInMemCache(file.id)) {
            Bitmap bitmap = parentFragment.bitmapCacheService.getThumbnailFromMemCache(file.id);
            viewHolder.fileIcon.setImageBitmap(bitmap);
        } else
            viewHolder.fileIcon.setImageResource(R.drawable.file_icon);

        viewHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.showPopUpWindow(file);
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


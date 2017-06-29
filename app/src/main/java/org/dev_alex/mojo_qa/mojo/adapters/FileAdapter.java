package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.res.Resources;
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
import org.dev_alex.mojo_qa.mojo.services.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private DocumentsFragment parentFragment;
    private ArrayList<File> files;
    private boolean isGrid;
    private boolean selectionModeEnabled;
    private ArrayList<String> selectedIds;

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        TextView fileDate;
        ImageView moreBtn;
        ImageView fileIcon;
        ImageView filePreview;
        ImageView selectedTick;
        View card;

        FileViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileDate = (TextView) itemView.findViewById(R.id.file_date);
            moreBtn = (ImageView) itemView.findViewById(R.id.more_btn);
            fileIcon = (ImageView) itemView.findViewById(R.id.file_icon);
            filePreview = (ImageView) itemView.findViewById(R.id.file_preview);
            card = itemView.findViewById(R.id.card);
            selectedTick = (ImageView) itemView.findViewById(R.id.selected_tick);
        }
    }


    public FileAdapter(DocumentsFragment parentFragment, ArrayList<File> files, boolean isGrid) {
        this.parentFragment = parentFragment;
        this.files = files;
        this.isGrid = isGrid;
        selectionModeEnabled = false;
    }

    public FileAdapter(DocumentsFragment parentFragment, ArrayList<File> files, boolean isGrid, ArrayList<String> selectedIds) {
        this.parentFragment = parentFragment;
        this.files = files;
        this.isGrid = isGrid;

        this.selectedIds = selectedIds;
        selectionModeEnabled = true;
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
            if (selectionModeEnabled)
                viewHolder.card.setBackgroundResource(selectedIds.contains(file.id) ?
                        R.drawable.file_card_grid_selection_checked : R.drawable.file_card_grid_selection_unchecked);
            else
                viewHolder.card.setBackgroundResource(R.drawable.file_card_grid_background);

            FrameLayout.LayoutParams imageLayoutParams = (FrameLayout.LayoutParams) viewHolder.filePreview.getLayoutParams();
            if (parentFragment.bitmapCacheService.hasThumbnailInMemCache(file.id)) {
                imageLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

                Bitmap bitmap = parentFragment.bitmapCacheService.getThumbnailFromMemCache(file.id);
                viewHolder.filePreview.setImageBitmap(bitmap);
            } else {
                viewHolder.filePreview.setImageResource(R.drawable.no_image);
                imageLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                        parentFragment.getResources().getDisplayMetrics());
            }
            viewHolder.filePreview.setLayoutParams(imageLayoutParams);
        } else {
            viewHolder.fileDate.setText(sdf.format(file.createdAt));
            if (selectionModeEnabled)
                viewHolder.card.setBackgroundResource(selectedIds.contains(file.id) ?
                        R.drawable.file_card_selection_checked : R.drawable.file_card_selection_unchecked);
            else
                viewHolder.card.setBackgroundResource(R.drawable.task_card_background);
        }

        String mimeType = file.nodeType;
        switch (mimeType) {
            case "cm:content":
                String fileExt = Utils.getFileExtension(file.name);
                if (!fileExt.isEmpty()) {
                    Resources resources = parentFragment.getResources();
                    final int resourceId = resources.getIdentifier("_" + fileExt, "drawable",
                            parentFragment.getContext().getPackageName());
                    if (resourceId != 0)
                        viewHolder.fileIcon.setImageResource(resourceId);
                    else
                        viewHolder.fileIcon.setImageResource(R.drawable.unknown);
                } else
                    viewHolder.fileIcon.setImageResource(R.drawable.unknown);
                break;
            case "mojo:template":
                viewHolder.fileIcon.setImageResource(R.drawable.icon_template);
                break;
            case "mojo:document":
                viewHolder.fileIcon.setImageResource(R.drawable.icon_filled_doc);
                break;
            case "mojo:analytic":
                viewHolder.fileIcon.setImageResource(R.drawable.icon_analytics);
                break;
            default:
                viewHolder.fileIcon.setImageResource(R.drawable.icon_mojo_file);
                break;
        }

        viewHolder.selectedTick.setVisibility(selectionModeEnabled && selectedIds.contains(file.id) ? View.VISIBLE : View.GONE);

        if (selectionModeEnabled) {
            viewHolder.moreBtn.setOnClickListener(null);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedIds.contains(file.id))
                        selectedIds.remove(file.id);
                    else
                        selectedIds.add(file.id);

                    notifyDataSetChanged();
                    parentFragment.checkIfSelectionModeFinished();
                }
            });
        } else {
            viewHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFragment.showPopUpWindow(file);
                }
            });

            if (file.nodeType.equals("cm:content"))
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parentFragment.new OpenFileTask(file).execute();
                    }
                });
        }

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!selectionModeEnabled) {
                    parentFragment.startSelectionMode();
                    selectedIds.add(file.id);
                    parentFragment.checkIfSelectionModeFinished();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void startSelectionMode() {
        selectionModeEnabled = true;
        selectedIds = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void stopSelectionMode() {
        selectionModeEnabled = false;
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedIds() {
        return selectedIds;
    }

    public ArrayList<File> getSelectedFiles() {
        ArrayList<File> selectedFiles = new ArrayList<>();
        for (File file : files)
            if (selectedIds.contains(file.id))
                selectedFiles.add(file);

        return selectedFiles;
    }
}


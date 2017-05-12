package org.dev_alex.mojo_qa.mojo.adapters;


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


    public SearchFileAdapter(SearchFragment parentFragment, ArrayList<File> files) {
        this.parentFragment = parentFragment;
        this.files = files;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_file, viewGroup, false);
        return new FileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FileViewHolder viewHolder, int i) {
       /* final File file = files.get(i);
        viewHolder.fileName.setText(file.name);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH.mm", Locale.getDefault());

        if (isGrid) {
            if (selectionModeEnabled)
                viewHolder.card.setBackgroundResource(selectedIds.contains(file.id) ?
                        R.drawable.file_card_grid_selection_checked : R.drawable.file_card_grid_selection_unchecked);
            else
                viewHolder.card.setBackgroundResource(R.drawable.file_card_grid_background);

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
        } else {
            viewHolder.fileDate.setText(sdf.format(file.createdAt));
            if (selectionModeEnabled)
                viewHolder.card.setBackgroundResource(selectedIds.contains(file.id) ?
                        R.drawable.file_card_selection_checked : R.drawable.file_card_selection_unchecked);
            else
                viewHolder.card.setBackgroundResource(R.drawable.task_card_background);
        }

        if (parentFragment.bitmapCacheService.hasThumbnailInMemCache(file.id)) {
            Bitmap bitmap = parentFragment.bitmapCacheService.getThumbnailFromMemCache(file.id);
            viewHolder.fileIcon.setImageBitmap(bitmap);
        } else
            viewHolder.fileIcon.setImageResource(R.drawable.file_icon);

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
        } else
            viewHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFragment.showPopUpWindow(file);
                }
            });

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
        });*/
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}


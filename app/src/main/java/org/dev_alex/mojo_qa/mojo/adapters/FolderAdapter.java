package org.dev_alex.mojo_qa.mojo.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.DocumentsFragment;
import org.dev_alex.mojo_qa.mojo.models.File;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;

import java.util.ArrayList;


public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private static final int VIEW_TYPE_ORG = 0;
    private static final int VIEW_TYPE_FOLDER = 1;

    private DocumentsFragment parentFragment;
    private ArrayList<File> folders;
    private boolean isGrid;
    private boolean selectionModeEnabled;
    private ArrayList<String> selectedIds;

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageView moreBtn;
        ImageView folderIcon;
        ImageView selectedTick;
        View card;

        FolderViewHolder(View itemView) {
            super(itemView);
            folderName = (TextView) itemView.findViewById(R.id.folder_name);
            moreBtn = (ImageView) itemView.findViewById(R.id.more_btn);
            folderIcon = (ImageView) itemView.findViewById(R.id.folder_icon_image);
            card = itemView.findViewById(R.id.card);
            selectedTick = (ImageView) itemView.findViewById(R.id.selected_tick);
        }
    }


    public FolderAdapter(DocumentsFragment parentFragment, ArrayList<File> files, boolean isGrid) {
        this.parentFragment = parentFragment;
        this.folders = files;
        this.isGrid = isGrid;
        selectionModeEnabled = false;
    }

    public FolderAdapter(DocumentsFragment parentFragment, ArrayList<File> folders, boolean isGrid, ArrayList<String> selectedIds) {
        this.parentFragment = parentFragment;
        this.folders = folders;
        this.isGrid = isGrid;

        this.selectedIds = selectedIds;
        selectionModeEnabled = true;
    }

    @Override
    public int getItemViewType(int position) {
        return folders.get(position).nodeType.equals("cm:org") ? 0 : 1;
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = null;
//        if (isGrid)
//            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_folder_grid, viewGroup, false);
//        else
//            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_folder, viewGroup, false);
        //это вроде бесполезная хрень, но на всякий оставлю. Хотя до этого вроде 2 одинаковых xml было

        switch (viewType) {
            case 0:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_folder, viewGroup, false);
                break;
            case 1:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_folder_updated, viewGroup, false);
                break;
        }

        return new FolderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FolderViewHolder viewHolder, int i) {
        final File folder = folders.get(i);
        viewHolder.folderName.setText(folder.name);

//        if (isGrid)
//            if (selectionModeEnabled)
//                viewHolder.card.setBackgroundResource(selectedIds.contains(folder.id) ?
//                        R.drawable.folder_card_grid_selection_checked : R.drawable.folder_card_grid_selection_unchecked);
//            else
//                viewHolder.card.setBackgroundResource(R.drawable.folder_card_grid_background);
//        else {
//            if (selectionModeEnabled)
//                viewHolder.card.setBackgroundResource(selectedIds.contains(folder.id) ?
//                        R.drawable.folder_card_selection_checked : R.drawable.folder_card_selection_unchecked);
//            else
//                viewHolder.card.setBackgroundResource(R.drawable.folder_card_background);
//        }


        viewHolder.selectedTick.setVisibility(selectionModeEnabled && selectedIds.contains(folder.id) ? View.VISIBLE : View.GONE);

        if (selectionModeEnabled) {
            viewHolder.moreBtn.setOnClickListener(null);
            viewHolder.itemView.setOnClickListener(v -> {
                if (selectedIds.contains(folder.id))
                    selectedIds.remove(folder.id);
                else
                    selectedIds.add(folder.id);

                notifyDataSetChanged();
                parentFragment.checkIfSelectionModeFinished();
            });
        } else {
            viewHolder.moreBtn.setOnClickListener(v -> {
                parentFragment.showPopUpWindow(folder);
            });
            viewHolder.itemView.setOnClickListener(v -> {
                parentFragment.new GetFilesTask(folder.id, folder.name, folder.orgId).execute();
            });
        }

        if (!folder.nodeType.equals("cm:org"))
            viewHolder.itemView.setOnLongClickListener(v -> {
                if (!selectionModeEnabled) {
                    parentFragment.startSelectionMode();
                    selectedIds.add(folder.id);
                    parentFragment.checkIfSelectionModeFinished();
                }
                return true;
            });


        viewHolder.folderIcon.setImageResource(folder.nodeType.equals("cm:org") ? R.drawable.organization_icon : R.drawable.category_icon);

        viewHolder.moreBtn.setVisibility(folder.nodeType.equals("cm:org") ? View.INVISIBLE : View.VISIBLE);

        viewHolder.moreBtn.setVisibility(View.VISIBLE);
        if ((LoginHistoryService.getCurrentUser().is_manager == null || !LoginHistoryService.getCurrentUser().is_manager)
                && (LoginHistoryService.getCurrentUser().is_orgowner == null || !LoginHistoryService.getCurrentUser().is_orgowner)) {
            viewHolder.moreBtn.setVisibility(View.INVISIBLE);
        }

        if (folder.nodeType.equals("cm:org")) {
            viewHolder.moreBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return folders.size();
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

    public ArrayList<File> getSelectedFolders() {
        ArrayList<File> selectedFolders = new ArrayList<>();
        for (File folder : folders)
            if (selectedIds.contains(folder.id))
                selectedFolders.add(folder);

        return selectedFolders;
    }
}
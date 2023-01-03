package org.dev_alex.mojo_qa.mojo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dev_alex.mojo_qa.mojo.R;

public class MediaFilesAdapter extends RecyclerView.Adapter<MediaFilesAdapter.MediaFileViewHolder> {

    @NonNull
    @Override
    public MediaFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_layout_recycler_view_list_item, parent, false);
        return new MediaFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaFileViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MediaFileViewHolder extends RecyclerView.ViewHolder {

        protected ImageView iconIv;
        protected ImageView deleteIv;

        public MediaFileViewHolder(@NonNull View itemView) {
            super(itemView);

            iconIv = itemView.findViewById(R.id.media_list_item_iv_icon);
            deleteIv = itemView.findViewById(R.id.media_list_item_iv_delete);
        }

        protected void bind() {

        }
    }
}

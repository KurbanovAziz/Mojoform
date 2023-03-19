package org.dev_alex.mojo_qa.mojo.adapters;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.ImageFullScreenActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MediaFilesAdapter extends RecyclerView.Adapter<MediaFilesAdapter.MediaFileViewHolder> {

    private List<Uri> mediaUriList = new ArrayList<>();

    public interface OnItemRemoveEventListener {
        void onItemRemoved(Uri uri);
    }

    private OnItemRemoveEventListener onItemRemoveEventListener = null;

    @NonNull
    @Override
    public MediaFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_layout_recycler_view_list_item, parent, false);
        return new MediaFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaFileViewHolder holder, int position) {
        Uri uri = mediaUriList.get(position);
        holder.bind(uri, this);
    }

    @Override
    public int getItemCount() {
        return mediaUriList.size();
    }

    public void add(Uri uri) {
        if (uri != null) {
            mediaUriList.add(uri);
            notifyItemChanged(mediaUriList.size() - 1);
        }
    }

    public void addAll(List<Uri> uris) {
        if (uris != null) {
            mediaUriList.clear();
            mediaUriList.addAll(uris);
            notifyDataSetChanged();
        }
    }

    protected void remove(Uri uri) {
        if (uri != null) {
            int position = mediaUriList.indexOf(uri);
            mediaUriList.remove(uri);
            notifyItemRemoved(position);
        }
    }

    protected void notifyMediaRemoved(Uri uri) {
        if (onItemRemoveEventListener != null)
            onItemRemoveEventListener.onItemRemoved(uri);
    }

    public void addOnElementRemoveListener(OnItemRemoveEventListener listener) {
        onItemRemoveEventListener = listener;
    }

    public class MediaFileViewHolder extends RecyclerView.ViewHolder {

        private static final String TYPE_IMAGE = "image";
        private static final String TYPE_DOCUMENT = "application";
        private static final String TYPE_AUDIO = "audio";
        private static final String EXT_3GPP = "3gpp";

        protected ShapeableImageView iconIv;
        protected ImageView deleteIv;

        private final ContentResolver contentResolver = itemView.getContext().getContentResolver();

        public MediaFileViewHolder(@NonNull View itemView) {
            super(itemView);

            iconIv = itemView.findViewById(R.id.media_list_item_iv_icon);
            deleteIv = itemView.findViewById(R.id.media_list_item_iv_delete);

        }

        protected void bind(Uri uri, MediaFilesAdapter adapter) {
            String mimeType = contentResolver.getType(uri);
            if (mimeType != null) {
                String type = mimeType.split("/")[0];
                String ext = mimeType.split("/")[1];
                if (ext.equalsIgnoreCase(EXT_3GPP)) {
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(itemView.getContext(), uri);
                    String hasVideoKey = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                    if (hasVideoKey == null) type = TYPE_AUDIO;
                }
                switch (type) {
                    case TYPE_IMAGE:
                        iconIv.setStrokeWidth(0);
                        Glide.with(itemView.getContext()).load(uri).centerCrop().into(iconIv);
                        iconIv.setOnClickListener(v -> viewImageFullScreen(uri));
                        break;
                    case TYPE_DOCUMENT:
                        iconIv.setImageResource(R.drawable.ic_list_item_media_file);
                        break;
                    case TYPE_AUDIO:
                        iconIv.setImageResource(R.drawable.ic_list_item_media_audio);
                        break;
                }
            }
            deleteIv.setOnClickListener(v -> removeItem(uri, adapter));
        }

        private void viewImageFullScreen(Uri uri) {
            startImageViewActivity(uri);
        }

        private void startImageViewActivity(Uri uri) {
            Intent intent = new Intent(itemView.getContext(), ImageFullScreenActivity.class);
            intent.putExtra(ImageFullScreenActivity.INTENT_KEY_EXTRAS, uri);
            itemView.getContext().startActivity(intent);
        }

        private byte[] getByteArrayFromImageView(ImageView imageView) {
            BitmapDrawable drawable = ((BitmapDrawable) imageView.getDrawable());
            Bitmap bitmap = drawable.getBitmap();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] arr = bos.toByteArray();
            return arr;
        }

        private void removeItem(Uri uri, MediaFilesAdapter adapter) {
            if (adapter != null) {
                adapter.remove(uri);
                adapter.notifyMediaRemoved(uri);
            }
        }
    }
}

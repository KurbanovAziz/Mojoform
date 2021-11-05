package com.lalongooo.videocompressor.video;

import android.media.MediaMetadataRetriever;

import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT;
import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH;

public class GetSizeVideo {

    private final String filePath;

    public GetSizeVideo(String filePath) {
        this.filePath = filePath;
    }

    public int width() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        return
                Integer.valueOf(
                        retriever.extractMetadata(
                                METADATA_KEY_VIDEO_WIDTH));
    }

    public int height() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        return
                Integer.valueOf(
                        retriever.extractMetadata(
                                METADATA_KEY_VIDEO_HEIGHT));
    }




}
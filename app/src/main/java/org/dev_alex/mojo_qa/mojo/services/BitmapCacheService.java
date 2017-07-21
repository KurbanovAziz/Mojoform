package org.dev_alex.mojo_qa.mojo.services;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapCacheService {
    private LruCache<String, Bitmap> mMemoryCache;

    public BitmapCacheService() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 10;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addPreviewToMemoryCache(String fileId, Bitmap bitmap) {
        if (getBitmapFromMemCache("preview_" + fileId) == null)
            mMemoryCache.put("preview_" + fileId, bitmap);
    }

    public void addThumbnailToMemoryCache(String fileId, Bitmap bitmap) {
        if (getBitmapFromMemCache("thumb_" + fileId) == null)
            mMemoryCache.put("thumb_" + fileId, bitmap);
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public Bitmap getPreviewFromMemCache(String fileId) {
        return mMemoryCache.get("preview_" + fileId);
    }

    public Bitmap getThumbnailFromMemCache(String fileId) {
        return mMemoryCache.get("thumb_" + fileId);
    }

    public boolean hasPreviewInMemCache(String fileId) {
        return mMemoryCache.get("preview_" + fileId) != null;
    }

    public boolean hasThumbnailInMemCache(String fileId) {
        return mMemoryCache.get("thumb_" + fileId) != null;
    }
}

package org.dev_alex.mojo_qa.mojo.services;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.util.Base64;
import android.util.TypedValue;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BitmapService {
    public static Pair<Intent, File> getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");


        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        File cameraFile = new File(context.getExternalCacheDir(), String.valueOf(System.currentTimeMillis()) + ".jpg");

        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", cameraFile);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);


        intentList = addIntentsToList(context, intentList, galleryIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
            chooserIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            chooserIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }


        return new Pair<>(chooserIntent, cameraFile);
    }

    public static Pair<Intent, File> getPickVideoIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("video/*");


        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra("return-data", false);
        File cameraFile = new File(context.getExternalCacheDir(), String.valueOf(System.currentTimeMillis()) + ".mp4");
        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", cameraFile);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);


        intentList = addIntentsToList(context, intentList, galleryIntent);
        intentList = addIntentsToList(context, intentList, takeVideoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
            chooserIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            chooserIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }


        return new Pair<>(chooserIntent, cameraFile);
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        if (reqHeight == 0 || reqWidth == 0) {
            reqHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130,
                    App.getContext().getResources().getDisplayMetrics()));
            reqWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70,
                    App.getContext().getResources().getDisplayMetrics()));
        }

        Bitmap resizedBitmap;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if (originalHeight > originalWidth) {
            newHeight = reqHeight;
            multFactor = (float) originalWidth / (float) originalHeight;
            newWidth = (int) (newHeight * multFactor);
        } else if (originalWidth > originalHeight) {
            newWidth = reqWidth;
            multFactor = (float) originalHeight / (float) originalWidth;
            newHeight = (int) (newWidth * multFactor);
        } else if (originalHeight == originalWidth) {
            newHeight = reqHeight;
            newWidth = reqWidth;
        }

        resizedBitmap = null;
        try {
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        } catch (Exception exc) {
        }

        return resizedBitmap;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int reqSize) {
        if (reqSize == 0) {
            reqSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130,
                    App.getContext().getResources().getDisplayMetrics()));

        }
        Bitmap resizedBitmap;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if (originalHeight > originalWidth) {
            newHeight = reqSize;
            multFactor = (float) originalWidth / (float) originalHeight;
            newWidth = (int) (newHeight * multFactor);
        } else if (originalWidth > originalHeight) {
            newWidth = reqSize;
            multFactor = (float) originalHeight / (float) originalWidth;
            newHeight = (int) (newWidth * multFactor);
        } else if (originalHeight == originalWidth) {
            newHeight = reqSize;
            newWidth = reqSize;
        }

        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    private static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int maxSize) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > maxSize || width > maxSize)
            if (height >= width)
                inSampleSize = height / maxSize;
            else
                inSampleSize = width / maxSize;

        return inSampleSize;
    }

    public static String saveBitmapToCache(Bitmap picture) {
        File cacheDir = new File(App.getContext().getCacheDir().getAbsolutePath() + "/point_tmp/");
        if (!cacheDir.exists() && !cacheDir.mkdirs())
            return null;
        File f = new File(cacheDir, UUID.randomUUID().toString() + ".jpg");

        try {
            FileOutputStream out = new FileOutputStream(f);
            picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            return null;
        }
        return f.getAbsolutePath();
    }

    public static String getBitmapBytesEncodedBase64(Bitmap bitmap) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, blob);
        byte[] b = blob.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static File saveBitmapToFile(Context context, Bitmap bitmap) throws Exception {
        File f = new File(context.getExternalCacheDir(), String.valueOf(System.currentTimeMillis()) + ".png");
        if (f.createNewFile()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] bitmapData = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            return f;
        } else
            throw new Exception("Не удалось создать файл");
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}

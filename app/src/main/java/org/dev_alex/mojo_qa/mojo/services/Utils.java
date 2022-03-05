package org.dev_alex.mojo_qa.mojo.services;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utils {

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public static void setupCloseKeyboardUI(final Activity activity, View rootView) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(rootView instanceof EditText)) {

            rootView.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (rootView instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) rootView).getChildCount(); i++) {
                View innerView = ((ViewGroup) rootView).getChildAt(i);
                setupCloseKeyboardUI(activity, innerView);
            }
        }
    }

    public static String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        return "";
    }

    public static float trimFloatAfterPointValue(float value, int afterPointCt) {
        BigDecimal bd = new BigDecimal(value);
        BigDecimal res = bd.setScale(afterPointCt, RoundingMode.HALF_UP);
        return res.floatValue();
    }

    public static JSONArray addAllItemsToJson(JSONArray toJsonArray, JSONArray fromJsonArray) {
        try {
            for (int j = 0; j < fromJsonArray.length(); j++)
                toJsonArray.put(fromJsonArray.get(j));
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return toJsonArray;
    }

    public static JSONArray removeItemAt(JSONArray jsonArray, int i) {
        JSONArray resultJsonArray = new JSONArray();
        try {
            for (int j = 0; j < jsonArray.length(); j++)
                if (j != i)
                    resultJsonArray.put(jsonArray.get(j));
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return resultJsonArray;
    }

    public static JSONArray removeItemWithValue(JSONArray jsonArray, String value) {
        JSONArray resultJsonArray = new JSONArray();
        try {
            for (int j = 0; j < jsonArray.length(); j++)
                if (!jsonArray.getString(j).equals(value))
                    resultJsonArray.put(jsonArray.get(j));
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return resultJsonArray;
    }

    public static boolean containsValue(JSONArray jsonArray, String value) {
        try {
            for (int j = 0; j < jsonArray.length(); j++)
                if (jsonArray.getString(j).equals(value))
                    return true;
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return false;
    }

    public static boolean containsAllValues(JSONArray jsonArray, JSONArray checkValues) {
        try {
            for (int j = 0; j < checkValues.length(); j++)
                if (!containsValue(jsonArray, checkValues.getString(j)))
                    return false;
            return true;
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String formatHtmlContentSize(String html) {
        String imgTag = "<img";
        String videoTag = "<iframe";
        String video2Tag = "<video";

        html = formatHtmlBlockSize(html, imgTag);
        html = formatHtmlBlockSize(html, videoTag);
        html = formatHtmlBlockSize(html, video2Tag);
        return html;
    }

    private static String formatHtmlBlockSize(String html, String tag) {
        int index = 0;
        String widthStr = "width=\"";
        String widthStr2 = "width:";
        String heightStr = "height=\"";
        String heightStr2 = "height:";
        String endOfTag = ">";
        String closeTag = "</" + tag.substring(1);

        while ((index = html.indexOf(tag, index)) != -1) {
            try {
                int endTagI = html.indexOf(endOfTag, index + 1);
                int closeTagI = html.indexOf(closeTag, index + 1);
                int heightI = html.indexOf(heightStr, index);
                int widthI = html.indexOf(widthStr, index);


                if (endTagI != -1 || closeTagI != -1) {
                    if (heightI != -1 && widthI != -1) {
                        html = html.substring(0, widthI + widthStr.length()) + "100%" + html.substring(html.indexOf("\"", widthI + widthStr.length() + 1));
                        heightI = html.indexOf(heightStr, index);
                        html = html.substring(0, heightI + heightStr.length()) + "auto" + html.substring(html.indexOf("\"", heightI + heightStr.length() + 1));
                    } else {
                        if (endTagI == -1)
                            endTagI = closeTagI;
                        html = html.substring(0, endTagI) + String.valueOf(" width=\"100%\" height=\"auto\" ") + html.substring(endTagI);
                    }
                }
                index++;
            } catch (Exception exc) {
                index++;
            }
        }

        index = 0;
        while ((index = html.indexOf(tag, index)) != -1) {
            try {
                int endTagI = html.indexOf(endOfTag, index + 1);
                int closeTagI = html.indexOf(closeTag, index + 1);
                int heightI = html.indexOf(heightStr2, index);
                int widthI = html.indexOf(widthStr2, index);


                if (endTagI != -1 || closeTagI != -1) {
                    if (heightI != -1 && widthI != -1) {
                        html = html.substring(0, widthI + widthStr2.length()) + "100%" + html.substring(html.indexOf("px", widthI + widthStr2.length() + 1));
                        heightI = html.indexOf(heightStr2, index);
                        html = html.substring(0, heightI + heightStr2.length()) + "auto" + html.substring(html.indexOf("px", heightI + heightStr2.length() + 1));
                        html = html.replace("px", "");
                    } else {
                        if (endTagI == -1)
                            endTagI = closeTagI;
                        html = html.substring(0, endTagI) + String.valueOf(" width:100%; height:auto; ") + html.substring(endTagI);
                    }
                }
                index++;
            } catch (Exception exc) {
                index++;
            }
        }
        return html;
    }

    public static boolean isImage(String path) {
        return path.endsWith(".jpg") || path.endsWith(".jpeg") ||
                path.endsWith(".bmp") || path.endsWith(".png");
    }

    public static String getMimeType(String path) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (type == null)
            type = "*/*";
        return type;
    }

    public static String getRealPathFromIntentData(Context context, Uri selectedFile) {
        if ("file".equalsIgnoreCase(selectedFile.getScheme())) {
            return selectedFile.getPath();
        }

        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.Files.FileColumns.DATA};
        Cursor cursor = context.getContentResolver().query(selectedFile, filePathColumn, null, null, null);

        if (cursor == null) {
            Log.e("UTILS", "can't get real path " + selectedFile);
            return null;
        }

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String realPath = cursor.getString(columnIndex);
        cursor.close();

        return realPath;
    }

    public static String getPathFromUri(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type))
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                else
                    return "/storage/" + type + "/" + split[1];
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}

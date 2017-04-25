package org.dev_alex.mojo_qa.mojo.services;


import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

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

    public static boolean isImage(String path) {
        return path.endsWith(".jpg") || path.endsWith(".jpeg") ||
                path.endsWith(".bmp") || path.endsWith(".png");
    }
}

package org.dev_alex.mojo_qa.mojo.services;


import org.json.JSONArray;

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
}

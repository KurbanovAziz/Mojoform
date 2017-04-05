package org.dev_alex.mojo_qa.mojo.services;

import android.content.Context;
import android.content.SharedPreferences;

import org.dev_alex.mojo_qa.mojo.App;


public class TokenService {
    public final static String TOKEN_PREFERENCES = "user_token";
    public final static String TOKEN = "token";

    public static String getToken() {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);

        String tokenStr = mSettings.getString(TOKEN, "");
        if (tokenStr.isEmpty())
            return null;
        else
            return tokenStr;
    }

    public static void deleteToken() {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        editor.remove(TOKEN);
        editor.apply();
    }

    public static void updateToken(String tokenStr) {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        editor.putString(TOKEN, tokenStr);
        editor.apply();
    }

    public static boolean isTokenExists() {
        return (getToken() != null);
    }
}

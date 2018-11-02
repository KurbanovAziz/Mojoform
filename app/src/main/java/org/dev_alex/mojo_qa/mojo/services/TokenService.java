package org.dev_alex.mojo_qa.mojo.services;

import android.content.Context;
import android.content.SharedPreferences;

import org.dev_alex.mojo_qa.mojo.App;

public class TokenService {
    private final static String TOKEN_PREFERENCES = "user_token";
    private final static String TOKEN = "token";
    private final static String FIREBASE_TOKEN = "firebase_token";
    private final static String USERNAME = "username";

    public static String getToken() {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);

        String tokenStr = mSettings.getString(TOKEN, "");
        if (tokenStr.isEmpty())
            return null;
        else
            return tokenStr;
    }

    public static String getUsername() {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);

        return mSettings.getString(USERNAME, "");
    }

    public static void deleteToken() {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        editor.remove(TOKEN);
        editor.apply();
    }

    public static void updateToken(String tokenStr, String userName) {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        editor.putString(TOKEN, tokenStr);
        editor.putString(USERNAME, userName);
        editor.apply();
    }

    public static String getFirebaseToken() {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);

        String tokenStr = mSettings.getString(FIREBASE_TOKEN, "");
        if (tokenStr.isEmpty())
            return null;
        else
            return tokenStr;
    }

    public static void updateFirebaseToken(String tokenStr) {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        editor.putString(FIREBASE_TOKEN, tokenStr);
        editor.apply();
    }

    public static boolean isTokenExists() {
        return (getToken() != null);
    }
}

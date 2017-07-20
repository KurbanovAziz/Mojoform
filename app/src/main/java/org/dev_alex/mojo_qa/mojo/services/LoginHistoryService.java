package org.dev_alex.mojo_qa.mojo.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.models.User;

import java.util.ArrayList;


public class LoginHistoryService {
    private final static String LOGIN_PREFERENCES = "user_history";
    private final static String USERS = "users";
    private final static String CURRENT_USER = "current_user";

    public static ArrayList<User> getLastLoggedUsers() {
        try {
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);

            String usersJson = mSettings.getString(USERS, "");
            if (usersJson.isEmpty())
                return null;
            else
                return new ObjectMapper().readValue(usersJson, new TypeReference<ArrayList<User>>() {
                });
        } catch (Exception exc) {
            return null;
        }
    }

    public static void addUser(User user) {
        try {
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSettings.edit();

            ArrayList<User> users = null;
            if (lastLoginUsersExists())
                users = getLastLoggedUsers();

            if (users == null)
                users = new ArrayList<>();

            for (User tmpUser : users)
                if (tmpUser.userName.equals(user.userName))
                    return;

            users.add(user);
            editor.putString(USERS, new ObjectMapper().writeValueAsString(users));
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    public static void addAvatar(String username, Bitmap bitmap) {
        try {
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSettings.edit();

            editor.putString(username, BitmapService.getBitmapBytesEncodedBase64(bitmap));
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    public static Bitmap getAvatar(String username) {
        Bitmap avatar = null;
        try {
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);

            String imageEncoded = mSettings.getString(username, "");
            if (imageEncoded.isEmpty())
                return null;

            byte[] decodedByte = Base64.decode(imageEncoded, 0);
            avatar = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        } catch (Exception ignored) {
        }
        return avatar;
    }

    public static User getCurrentUser() {
        try {
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);

            String userJson = mSettings.getString(CURRENT_USER, "");
            if (userJson.isEmpty())
                return new User();
            else
                return new ObjectMapper().readValue(userJson, User.class);

        } catch (Exception exc) {
            return new User();
        }
    }

    public static void setCurrentUser(User user) {
        try {
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSettings.edit();

            editor.putString(CURRENT_USER, new ObjectMapper().writeValueAsString(user));
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    public static boolean lastLoginUsersExists() {
        return (getLastLoggedUsers() != null);
    }
}

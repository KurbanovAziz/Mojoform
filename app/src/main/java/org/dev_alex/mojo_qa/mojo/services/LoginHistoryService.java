package org.dev_alex.mojo_qa.mojo.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.models.User;

import java.util.ArrayList;


public class LoginHistoryService {
    private final static String LOGIN_PREFERENCES = "user_history";
    private final static String USERS = "users";

    public static ArrayList<User> getLastLoginedUsers() {
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
                users = getLastLoginedUsers();

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

    public static boolean lastLoginUsersExists() {
        return (getLastLoginedUsers() != null);
    }
}

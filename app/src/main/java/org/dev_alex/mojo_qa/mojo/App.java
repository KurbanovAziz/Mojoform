package org.dev_alex.mojo_qa.mojo;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import java.lang.ref.WeakReference;

public class App extends Application {
    private static WeakReference<Context> mContext;
    public static DisplayMetrics displayMetrics;

    private static String host;
    private static String task_host;


    public static DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<Context>(this);
        displayMetrics = getResources().getDisplayMetrics();

        switch (BuildConfig.FLAVOR) {
            case "release_flavor":
                host = "https://system.mojoform.com";
                task_host = "https://tasks.mojo.mojoform.com/activiti-rest/service";
                break;

            case "debug_flavor":
                host = "https://mojo-qa.dev-alex.org";
                task_host = "https://activiti.dev-alex.org/activiti-rest/service";
                break;

            default:
                host = "https://system.mojoform.com";
                task_host = "https://tasks.mojo.mojoform.com/activiti-rest/service";
                break;
        }
    }

    public static Context getContext() {
        return mContext.get();
    }

    public static String getHost() {
        return host;
    }

    public static String getTask_host() {
        return task_host;
    }
}

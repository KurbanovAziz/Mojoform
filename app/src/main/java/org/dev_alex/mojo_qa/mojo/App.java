package org.dev_alex.mojo_qa.mojo;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import java.lang.ref.WeakReference;

public class App extends Application {
    private static WeakReference<Context> mContext;
    public static DisplayMetrics displayMetrics;

    public static DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<Context>(this);
        displayMetrics = getResources().getDisplayMetrics();
    }

    public static Context getContext() {
        return mContext.get();
    }
}

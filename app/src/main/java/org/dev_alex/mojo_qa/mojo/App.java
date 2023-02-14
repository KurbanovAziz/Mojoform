package org.dev_alex.mojo_qa.mojo;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import androidx.multidex.MultiDex;

import java.lang.ref.WeakReference;

import io.reactivex.plugins.RxJavaPlugins;

public class App extends Application {
    private static WeakReference<Context> mContext;
    public static DisplayMetrics displayMetrics;

    private static String host;


    public static DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = new WeakReference<>(this);
        displayMetrics = getResources().getDisplayMetrics();


        switch (BuildConfig.FLAVOR) {
            case "release_flavor":
                host = "https://system.mojoform.com";
                host = "https://system.mojoform.com";
                host = "https://system.mojo2.ru";

                break;

            case "debug_flavor":
                host = "https://mojo-qa.dev-alex.org";
                host = "https://system.mojoform.com";
                host = "https://system.mojo2.ru";


                break;

            case "demo_flavor":
                host = "https://demo.mojoform.com";
                host = "https://system.mojoform.com";
                host = "https://system.mojo2.ru";


                break;

            default:
                host = "https://system.mojoform.com";
                host = "https://system.mojo2.ru";

                break;
        }

        RxJavaPlugins.setErrorHandler(Throwable::printStackTrace);
        io.reactivex.rxjava3.plugins.RxJavaPlugins.setErrorHandler(Throwable::printStackTrace);
    }

    public static Context getContext() {
        return mContext.get();
    }

    public static String getHost() {
        return host;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

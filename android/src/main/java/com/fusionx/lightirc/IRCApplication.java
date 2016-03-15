package com.fusionx.lightirc;

import android.app.Application;

import com.fusionx.lightirc.misc.AppPreferences;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class IRCApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppPreferences.setupAppPreferences(this);
    }
}

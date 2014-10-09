package com.fusionx.lightirc;

import android.app.Application;

import com.fusionx.lightirc.misc.AppPreferences;

public class IRCApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppPreferences.setupAppPreferences(this);
    }
}

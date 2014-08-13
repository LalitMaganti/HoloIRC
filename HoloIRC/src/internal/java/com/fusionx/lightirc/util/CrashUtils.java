package com.fusionx.lightirc.util;

import com.crashlytics.android.Crashlytics;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.relay.Server;

import android.content.Context;

public class CrashUtils {

    public static void startCrashlyticsIfAppropriate(final Context context) {
        // Make sure app preferences are initialized
        AppPreferences.setupAppPreferences(context);

        final AppPreferences appPreferences = AppPreferences.getAppPreferences();
        if (appPreferences.isBugReportingEnabled()) {
            Crashlytics.start(context);
        }
    }

    public static void logMissingData(final Server server) {
        final AppPreferences appPreferences = AppPreferences.getAppPreferences();
        if (appPreferences.isBugReportingEnabled()) {
            Crashlytics.log("Missing data on server " + server.getConfiguration().getUrl());
        }
    }
}
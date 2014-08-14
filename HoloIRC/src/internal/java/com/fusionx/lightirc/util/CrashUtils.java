package com.fusionx.lightirc.util;

import com.crashlytics.android.Crashlytics;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.relay.Server;

import android.content.Context;

public class CrashUtils {

    public static void startCrashlyticsIfAppropriate(final Context context) {
        // Make sure app preferences are initialized
        AppPreferences.setupAppPreferences(context);

        callbackIfReportingEnabled(() -> Crashlytics.start(context));
    }

    public static void logMissingData(final Server server) {
        callbackIfReportingEnabled(() -> Crashlytics.log("Missing data on server "
                + server.getConfiguration().getUrl()));
    }

    public static void logIssue(final String data) {
        callbackIfReportingEnabled(() -> Crashlytics.log(data));
    }

    private static void callbackIfReportingEnabled(final Runnable runnable) {
        final AppPreferences appPreferences = AppPreferences.getAppPreferences();
        if (appPreferences.isBugReportingEnabled()) {
            runnable.run();
        }
    }
}
package com.fusionx.lightirc.util;

import com.crashlytics.android.Crashlytics;
import com.fusionx.lightirc.misc.AppPreferences;

import android.content.Context;

public class CrashUtils {

    public static void startCrashlyticsIfAppropriate(final Context context) {
        // Make sure app preferences are initialized
        AppPreferences.setupAppPreferences(context);

        callbackIfReportingEnabled(() -> Crashlytics.start(context));
    }

    public static void logIssue(final String data) {
        callbackIfReportingEnabled(() -> {
            Crashlytics.log(data);
            Crashlytics.logException(new IRCException(data));
        });
    }

    private static void callbackIfReportingEnabled(final Runnable runnable) {
        final AppPreferences appPreferences = AppPreferences.getAppPreferences();
        if (appPreferences.isBugReportingEnabled()) {
            runnable.run();
        }
    }

    private static class IRCException extends Exception {

        public IRCException(final String data) {
            super(data);
        }
    }
}
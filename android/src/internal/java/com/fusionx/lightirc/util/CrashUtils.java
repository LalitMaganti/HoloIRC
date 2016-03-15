package com.fusionx.lightirc.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.fusionx.lightirc.misc.AppPreferences;

import co.fusionx.relay.base.Server;
import io.fabric.sdk.android.Fabric;

public class CrashUtils {

    public static void startCrashlyticsIfAppropriate(final Context context) {
        callbackIfReportingEnabled(() -> Fabric.with(context, new Crashlytics()));
    }

    public static void logMissingData(final Server server) {
        callbackIfReportingEnabled(() -> {
            Crashlytics.log(server.getConfiguration().getUrl());
            Crashlytics.logException(new IRCException("Missing data on server"));
        });
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
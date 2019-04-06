package app.holoirc.util;

import co.fusionx.relay.base.Server;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import app.holoirc.misc.AppPreferences;
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

    public static void handleException(Exception ex) {
        callbackIfReportingEnabled(() -> Crashlytics.logException(ex));
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
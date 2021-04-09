package app.holoirc.util;

import co.fusionx.relay.base.Server;
import android.content.Context;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import app.holoirc.misc.AppPreferences;

public class CrashUtils {
    public static String TAG = "CrashUtils";

    public static void startCrashlyticsIfAppropriate(final Context context) {
        callbackIfReportingEnabled(() -> FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true));
    }

    public static void logMissingData(final Server server) {
        callbackIfReportingEnabled(() -> {
            FirebaseCrashlytics.getInstance().log(server.getConfiguration().getUrl());
            FirebaseCrashlytics.getInstance().recordException(new IRCException("Missing data on server"));
        });
    }

    public static void logIssue(final String data) {
        callbackIfReportingEnabled(() -> {
            FirebaseCrashlytics.getInstance().log(data);
            FirebaseCrashlytics.getInstance().recordException(new IRCException(data));
        });
    }

    public static void handleException(Exception ex) {
        callbackIfReportingEnabled(() -> FirebaseCrashlytics.getInstance().recordException(ex));
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
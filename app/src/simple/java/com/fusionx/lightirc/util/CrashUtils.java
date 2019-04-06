package com.fusionx.lightirc.util;

import co.fusionx.relay.base.Server;

import android.content.Context;

public class CrashUtils {

    public static void startCrashlyticsIfAppropriate(final Context context) {
        // Don't do anything since we are building simple flavour
    }

    public static void logMissingData(final Server server) {
        // Don't do anything since we are building simple flavour
    }

    public static void logIssue(final String data) {
        // Don't do anything since we are building simple flavour
    }

    public static void handleException(Exception ex) {
        // Simply throw the exception to warn the user.
        throw new RuntimeException(ex);
    }
}
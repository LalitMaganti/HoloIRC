package com.fusionx.lightirc.util;

import com.crashlytics.android.Crashlytics;

import android.content.Context;

public class CrashUtils {

    public static void startCrashlyticsIfAppropriate(final Context context) {
        Crashlytics.start(context);
    }
}
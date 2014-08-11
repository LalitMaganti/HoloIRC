package com.fusionx.lightirc.util;

import com.crashlytics.android.Crashlytics;
import com.fusionx.lightirc.R;

import android.content.Context;
import android.text.TextUtils;

public class CrashUtils {

    public static void startCrashlyticsIfAppropriate(final Context context) {
        if (TextUtils.isEmpty(context.getString(R.string.crashlytics_key))) {
            return;
        }
        Crashlytics.start(context);
    }
}
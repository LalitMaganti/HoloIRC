package com.fusionx.lightirc.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import static com.fusionx.lightirc.misc.AppPreferences.getAppPreferences;

public class StorageUtils {

    public static File getLoggingDir(final Context context) {
        final File file = new File(Environment.getExternalStorageDirectory() + "/" +
                getAppPreferences(context).getRelativeLoggingPath());
        if (!file.exists() && !file.mkdirs()) {
            throw new IllegalArgumentException();
        }
        return file;
    }
}
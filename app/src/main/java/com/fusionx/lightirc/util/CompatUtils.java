package com.fusionx.lightirc.util;

import android.os.Build;

public class CompatUtils {

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
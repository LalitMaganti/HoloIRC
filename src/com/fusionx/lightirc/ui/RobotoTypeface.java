package com.fusionx.lightirc.ui;

import android.content.Context;
import android.graphics.Typeface;

public class RobotoTypeface {
    private static Typeface robotoTypeface = null;

    public static Typeface getTypeface(final Context context) {
        if (robotoTypeface == null) {
            robotoTypeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }
        return robotoTypeface;
    }
}

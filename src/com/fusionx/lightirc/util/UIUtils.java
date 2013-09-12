package com.fusionx.lightirc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.PreferenceConstants;

public class UIUtils {
    private static Typeface mRobotoLightTypeface = null;
    private static Typeface mRobotoThinTypeface = null;

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return hasHoneycomb() && isTablet(context);
    }

    public static Typeface getRobotoLight(final Context context) {
        if (mRobotoLightTypeface == null) {
            mRobotoLightTypeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }
        return mRobotoLightTypeface;
    }

    public static void setRobotoLight(final Context context, final TextView textView) {
        final Typeface font = getRobotoLight(context);
        textView.setTypeface(font);
    }

    private static Typeface getRobotoThin(final Context context) {
        if (mRobotoLightTypeface == null) {
            mRobotoThinTypeface = Typeface.createFromAsset(context.getAssets(),
                    "Roboto-Thin.ttf");
        }
        return mRobotoThinTypeface;
    }

    public static void setRobotoThin(final Context context, final TextView textView) {
        final Typeface font = getRobotoThin(context);
        textView.setTypeface(font);
    }

    public static int getThemeInt(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final int theme = Integer.parseInt(preferences.getString(PreferenceConstants.Theme, "1"));
        return theme != 0 ? R.style.Light : R.style.Dark;
    }

    public static int getThemedTextColor(final Context context) {
        return isThemeLight(context) ? context.getResources().getColor(android.R.color.black) :
                context.getResources().getColor(android.R.color.white);
    }

    public static boolean isThemeLight(final Context context) {
        return getThemeInt(context) == R.style.Light;
    }

    public static void updateLineColourfulness(final SharedPreferences preferences) {
        ColourParserUtils.highlightLine = preferences.getBoolean(PreferenceConstants.LineColourful, true);
    }
}
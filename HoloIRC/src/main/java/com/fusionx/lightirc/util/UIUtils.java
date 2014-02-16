package com.fusionx.lightirc.util;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.ui.IRCActivity;
import com.fusionx.lightirc.ui.phone.IRCPhoneActivity;
import com.fusionx.relay.constants.Theme;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

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

    private static boolean isTablet(final Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration
                .SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(final Context context) {
        return hasHoneycomb() && isTablet(context);
    }

    public static boolean isLandscape(final Context context) {
        return context.getResources().getConfiguration().orientation == Configuration
                .ORIENTATION_LANDSCAPE;
    }

    public static Typeface getRobotoLight(final Context context) {
        if (mRobotoLightTypeface == null) {
            mRobotoLightTypeface = Typeface.createFromAsset(context.getAssets(),
                    "Roboto-Light.ttf");
        }
        return mRobotoLightTypeface;
    }

    public static Class<? extends IRCActivity> getIRCActivity(final Context context) {
        // TODO - fix this
        return IRCPhoneActivity.class;
        //return UIUtils.isHoneycombTablet(context) ? IRCTabletActivity.class : IRCPhoneActivity
        //        .class;
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

    public static int getThemeInt() {
        return AppPreferences.theme != Theme.DARK ? R.style.Light : R.style.Dark;
    }

    public static int getThemedTextColor(final Context context) {
        return isThemeLight() ? context.getResources().getColor(android.R.color.black)
                : context.getResources().getColor(android.R.color.white);
    }

    public static boolean isThemeLight() {
        return getThemeInt() == R.style.Light;
    }

    public static void setWindowBackgroundOnView(final Context context, final View view) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(new int[]
                {android.R.attr.windowBackground});
        final int background = a.getResourceId(0, 0);
        view.setBackgroundResource(background);
        a.recycle();
    }
}
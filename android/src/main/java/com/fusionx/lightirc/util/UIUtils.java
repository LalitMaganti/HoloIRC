package com.fusionx.lightirc.util;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.Theme;
import com.fusionx.lightirc.model.MessagePriority;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UIUtils {

    private static Typeface sRobotoLightTypeface = null;

    private static Typeface sRobotoThinTypeface = null;

    public static boolean isLandscape(final Context context) {
        return context.getResources().getConfiguration().orientation == Configuration
                .ORIENTATION_LANDSCAPE;
    }

    public static void setRobotoLight(final Context context, final TextView textView) {
        final Typeface font = getRobotoLight(context);
        textView.setTypeface(font);
    }

    public static int getThemeInt() {
        return AppPreferences.getAppPreferences().getTheme() == Theme.DARK
                ? R.style.Dark
                : R.style.Light;
    }

    public static void toggleSlidingPane(final SlidingPaneLayout slidingPaneLayout) {
        if (slidingPaneLayout.isOpen()) {
            slidingPaneLayout.closePane();
        } else {
            slidingPaneLayout.openPane();
        }
    }

    public static void toggleDrawerLayout(final DrawerLayout drawerLayout, final View drawer) {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
        } else {
            drawerLayout.openDrawer(drawer);
        }
    }

    public static List<Integer> getCheckedPositions(final AbsListView listView) {
        if (listView == null) {
            return new ArrayList<>();
        }

        final List<Integer> list = new ArrayList<>();
        final SparseBooleanArray checkedPositionsBool = listView.getCheckedItemPositions();
        for (int i = 0; i < checkedPositionsBool.size(); i++) {
            if (checkedPositionsBool.valueAt(i)) {
                final Integer index = checkedPositionsBool.keyAt(i);
                list.add(index);
            }
        }
        return list;
    }

    // TODO - fix this horribleness
    public static CharacterStyle getSpanFromPriority(final MessagePriority
            priority) {
        final int color;
        switch (priority) {
            case LOW:
                color = Color.parseColor("#00994C");
                break;
            case MEDIUM:
                if (AppPreferences.getAppPreferences().getTheme() == Theme.DARK) {
                    color = Color.parseColor("#4C81B7");
                } else {
                    color = Color.parseColor("#004C99");
                }
                break;
            case HIGH:
                if (AppPreferences.getAppPreferences().getTheme() == Theme.DARK) {
                    color = Color.parseColor("#DA4646");
                } else {
                    color = Color.parseColor("#CC0000");
                }
                break;
            default:
                color = 0;
                break;
        }
        return new ForegroundColorSpan(color);
    }

    public static <T extends View> T findById(final View view, final int id) {
        return (T) view.findViewById(id);
    }

    public static <T extends View> T findById(final Activity activity, final int id) {
        return (T) activity.findViewById(id);
    }

    public static int resolveResourceIdFromAttr(final Context context, final int attrResource) {
        final TypedValue typedvalueattr = new TypedValue();
        context.getTheme().resolveAttribute(attrResource, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    public static int getColourFromResource(final Context context, final int resourceId) {
        return context.getResources().getColor(resourceId);
    }

    public static boolean isAppFromRecentApps(final int flags) {
        return (flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
    }

    private static boolean isTablet(final Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration
                .SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static Typeface getRobotoLight(final Context context) {
        if (sRobotoLightTypeface == null) {
            sRobotoLightTypeface = Typeface.createFromAsset(context.getAssets(),
                    "Roboto-Light.ttf");
        }
        return sRobotoLightTypeface;
    }
}
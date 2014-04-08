package com.fusionx.lightirc.util;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.model.MessagePriority;
import com.fusionx.relay.constants.Theme;

import android.app.Activity;
import android.content.Context;
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

    private static Typeface mRobotoLightTypeface = null;

    private static Typeface mRobotoThinTypeface = null;

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

    public static void setRobotoLight(final Context context, final TextView textView) {
        final Typeface font = getRobotoLight(context);
        textView.setTypeface(font);
    }

    public static void setRobotoThin(final Context context, final TextView textView) {
        final Typeface font = getRobotoThin(context);
        textView.setTypeface(font);
    }

    public static int getThemeInt() {
        return AppPreferences.theme != Theme.DARK ? R.style.Light : R.style.Dark;
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
        List<Integer> checkedSessionPositions = new ArrayList<>();
        if (listView == null) {
            return checkedSessionPositions;
        }

        SparseBooleanArray checkedPositionsBool = listView.getCheckedItemPositions();
        for (int i = 0; i < checkedPositionsBool.size(); i++) {
            if (checkedPositionsBool.valueAt(i)) {
                checkedSessionPositions.add(checkedPositionsBool.keyAt(i));
            }
        }

        return checkedSessionPositions;
    }

    public static CharacterStyle getSpanFromPriority(final Context context, final MessagePriority
            priority) {
        if (priority == null) {
            final TypedValue typedvalueattr = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.expandable_list_text, typedvalueattr, true);
            return new ForegroundColorSpan(context.getResources().getColor(typedvalueattr
                    .resourceId));
        }
        final int color;
        switch (priority) {
            case LOW:
                color = Color.parseColor("#00994C");
                break;
            case MEDIUM:
                color = Color.parseColor("#004C99");
                break;
            case HIGH:
                color = Color.parseColor("#CC0000");
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

    public static int getResourceId(final Context context, final int attrResource) {
        final TypedValue typedvalueattr = new TypedValue();
        context.getTheme().resolveAttribute(attrResource, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    private static boolean isTablet(final Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration
                .SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static Typeface getRobotoThin(final Context context) {
        if (mRobotoLightTypeface == null) {
            mRobotoThinTypeface = Typeface.createFromAsset(context.getAssets(),
                    "Roboto-Thin.ttf");
        }
        return mRobotoThinTypeface;
    }
}
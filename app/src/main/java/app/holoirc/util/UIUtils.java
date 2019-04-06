package app.holoirc.util;

import app.holoirc.R;
import app.holoirc.misc.AppPreferences;
import app.holoirc.misc.Theme;
import app.holoirc.model.MessagePriority;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.widget.DrawerLayout;
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

    public static void setRobotoLight(final Context context, final TextView textView) {
        final Typeface font = getRobotoLight(context);
        textView.setTypeface(font);
    }

    public static int getThemeInt() {
        return AppPreferences.getAppPreferences().getTheme() == Theme.DARK
                ? R.style.Dark
                : R.style.Light;
    }

    public static boolean toggleDrawerLayout(final DrawerLayout drawerLayout, final View drawer) {
        boolean isOpen = drawerLayout.isDrawerOpen(drawer);
        if (isOpen) {
            drawerLayout.closeDrawer(drawer);
        } else {
            drawerLayout.openDrawer(drawer);
        }
        return !isOpen;
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

    public static int resolveResourceIdFromAttr(final Context context, final int attrResource) {
        final TypedValue typedvalueattr = new TypedValue();
        context.getTheme().resolveAttribute(attrResource, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    public static boolean isAppFromRecentApps(final int flags) {
        return (flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
    }

    private static Typeface getRobotoLight(final Context context) {
        if (sRobotoLightTypeface == null) {
            sRobotoLightTypeface = Typeface.createFromAsset(context.getAssets(),
                    "Roboto-Light.ttf");
        }
        return sRobotoLightTypeface;
    }
}
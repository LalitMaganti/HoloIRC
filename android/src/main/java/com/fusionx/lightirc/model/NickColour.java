package com.fusionx.lightirc.model;

import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.Theme;

import android.graphics.Color;

import co.fusionx.relay.core.Nick;

public class NickColour {

    private final Nick mNick;

    private final int mColour;

    public NickColour(final Nick nick) {
        mNick = nick;
        mColour = getColorFromNick();
    }

    public int getColour() {
        return mColour;
    }

    private int getColorFromNick() {
        final Theme theme = AppPreferences.getAppPreferences().getTheme();
        final int colorOffset = theme.getTextColourOffset();
        final int hash = mNick.hashCode();

        int red = (hash) & 0xFF;
        int green = (hash >> 16) & 0xFF;
        int blue = (hash >> 8) & 0xFF;

        // mix the color
        red = normaliseColourInt((red + colorOffset) / 2);
        green = normaliseColourInt((green + colorOffset) / 2);
        blue = normaliseColourInt((blue + colorOffset) / 2);

        return Color.rgb(red, green, blue);
    }

    private int normaliseColourInt(final int colour) {
        if (colour > 255) {
            return 255;
        } else if (colour < 0) {
            return 0;
        }
        return colour;
    }
}
package com.fusionx.lightirc.misc;

import android.text.style.CharacterStyle;

public class FormattedString {
    private final String mString;

    private final CharacterStyle mCharacterStyle;

    public FormattedString(final String string) {
        mString = string;
        mCharacterStyle = null;
    }

    public FormattedString(final String string, final CharacterStyle characterStyle) {
        mString = string;
        mCharacterStyle = characterStyle;
    }

    public String getString() {
        return mString;
    }

    public CharacterStyle getCharacterStyle() {
        return mCharacterStyle;
    }
}
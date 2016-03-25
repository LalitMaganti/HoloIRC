package com.fusionx.lightirc.misc;

import android.text.style.CharacterStyle;

import java.util.ArrayList;
import java.util.List;

public class FormattedString {

    private final String mString;

    public static class Span {
        int start;
        int end;
        CharacterStyle style;
        private Span(int start, int end, CharacterStyle style) {
            this.start = start;
            this.end = end;
            this.style = style;
        }
    }

    private final ArrayList<Span> mSpans = new ArrayList<>();

    public FormattedString(final String string) {
        mString = string;
    }

    public FormattedString(final String string, final CharacterStyle characterStyle) {
        mString = string;
        mSpans.add(new Span(0, string.length(), characterStyle));
    }

    public void addSpan(final CharacterStyle style, int start, int end) {
        mSpans.add(new Span(start, end, style));
    }

    public String getString() {
        return mString;
    }

    public List<Span> getSpans() {
        return mSpans;
    }

    @Override
    public String toString() {
        return mString;
    }
}
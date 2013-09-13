package com.fusionx.lightirc.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.google.common.base.CharMatcher;

import static com.fusionx.lightirc.misc.AppPreferences.highlightLine;

public class ColourParserUtils {
    public static Spanned parseHtml(final String input) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String remainingText = CharMatcher.JAVA_ISO_CONTROL.removeFrom(input);
        while (containsValidTag(remainingText)) {
            final int indexOfFirstOpen = remainingText.indexOf("<");
            final String start = remainingText.substring(0, indexOfFirstOpen);
            builder.append(start);
            final int indexOfFirstClose = remainingText.indexOf(">");
            String tag = remainingText.substring(indexOfFirstOpen + 1, indexOfFirstClose);
            final String textAfterTag = remainingText.substring(indexOfFirstClose + 1);
            CharacterStyle characterStyle;
            if (tag.startsWith("color")) {
                characterStyle = new ForegroundColorSpan(Integer.parseInt(tag.substring(6)));
                tag = "color";
            } else if (tag.equals("b")) {
                characterStyle = new StyleSpan(Typeface.BOLD);
            } else {
                final int indexOfLastOpen = textAfterTag.indexOf("</" + tag + ">");
                builder.append(tag);
                remainingText = textAfterTag.substring(indexOfLastOpen + 3 + tag.length());
                continue;
            }
            final int indexOfLastOpen = textAfterTag.indexOf("</" + tag + ">");
            final String text = textAfterTag.substring(0, indexOfLastOpen);
            final int len = builder.length();
            int length;
            if (!containsValidTag(text)) {
                length = text.length();
                builder.append(text);
            } else {
                final Spanned spanned = parseHtml(text);
                length = spanned.length();
                builder.append(spanned);
            }
            if (highlightLine) {
                builder.setSpan(characterStyle, 0, length + len, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                builder.setSpan(characterStyle, len, length + len,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            remainingText = textAfterTag.substring(indexOfLastOpen + 3 + tag.length());
        }
        return builder.append(remainingText);
    }

    public static boolean containsValidTag(final String text) {
        return text.contains("<color=") && text.contains("</color>") || text.contains("<b>") &&
                text.contains("</b>");
    }
}
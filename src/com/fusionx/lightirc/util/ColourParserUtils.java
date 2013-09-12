package com.fusionx.lightirc.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import com.fusionx.lightirc.constants.Constants;
import com.google.common.base.CharMatcher;

public class ColourParserUtils {
    public static boolean highlightLine = true;

    public static Spanned parseHtml(final String input) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        String trimmedText = CharMatcher.JAVA_ISO_CONTROL.removeFrom(input);

        int processed = 0, bracketIndex;
        while ((bracketIndex = trimmedText.indexOf('<', processed)) >= 0) {
            builder.append(trimmedText.substring(processed, bracketIndex));
            processed = bracketIndex + 1;

            final String tag = trimmedText.substring(processed);
            String closingTag;
            CharacterStyle characterStyle;
            final int indexOfOpenClosing = trimmedText.indexOf(">", processed);
            if (tag.startsWith("color=")) {
                characterStyle = new ForegroundColorSpan(Integer.parseInt(trimmedText.substring
                        (processed + 6, indexOfOpenClosing)));
                closingTag = "</color>";
            } else if (tag.startsWith("b>")) {
                characterStyle = new StyleSpan(Typeface.BOLD);
                closingTag = "</b>";
            } else {
                if(Constants.DEBUG) {
                    throw new UnsupportedOperationException();
                } else {
                    return builder;
                }
            }
            processed = indexOfOpenClosing + 1;
            final int indexOfClosingOpen = trimmedText.indexOf(closingTag, processed);
            final String textToStyle = trimmedText.substring(processed, indexOfClosingOpen);
            int length;
            if (containsValidTag(textToStyle)) {
                final Spanned spanned = parseHtml(textToStyle);
                length = spanned.length();
                builder.append(spanned);
            } else {
                length = textToStyle.length();
                builder.append(textToStyle);
            }
            if (highlightLine) {
                try {
                builder.setSpan(characterStyle, 0, length + bracketIndex,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } catch (IndexOutOfBoundsException ex) {
                    Log.e("HoloIRC", input);
                }
            } else {
                try {
                    builder.setSpan(characterStyle, bracketIndex, length + bracketIndex,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IndexOutOfBoundsException ex) {
                    Log.e("HoloIRC", input);
                }
            }
            processed = indexOfClosingOpen + closingTag.length();
        }
        builder.append(trimmedText.substring(processed));

        return builder;
    }

    public static boolean containsValidTag(final String text) {
        return text.contains("<color=") && text.contains("</color>") || text.contains("<b>") &&
                text.contains("</b>");
    }
}
package com.fusionx.lightirc.util;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ColourParserUtilsTest {
    @Test
    public void parseTest() {
        final int color = MiscUtils.generateRandomColor(0);
        ColourParserUtils.highlightLine = true;
        final Spanned test = ColourParserUtils.parseHtml("<color=" + color +
                "><222222></color>: gsjiknsknfkdjngjdkngdfkknjfkdnf");
        final SpannableStringBuilder expected = new SpannableStringBuilder();
        expected.append("<222222>: gsjiknsknfkdjngjdkngdfkknjfkdnf");
        expected.setSpan(new ForegroundColorSpan(222222), 1, 7,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        assertEquals(expected, test);

        final long time = System.nanoTime();
        for(int i = 0; i < 10000; i++) {
            Html.fromHtml("<font color=\"" + color +
                    "\">222222</font>: gsjiknsknfkdjngjdkngdfkknjfkdnf");
        }
        final long totla = System.nanoTime() - time;

        System.out.println(totla);

        final long time3 = System.nanoTime();
        for(int i = 0; i < 10000; i++) {
            ColourParserUtils.parseHtml("<color=" + color +
                    ">222222</color>: gsjiknsknfkdjngjdkngdfkknjfkdnf");
        }
        final long totla3 = System.nanoTime() - time3;

        System.out.println(totla3);
    }
}
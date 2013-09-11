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
public class HtmlUtilsTest {
    @Test
    public void parseTest() {
        final int color = MiscUtils.generateRandomColor(0);
        HtmlUtils.highlightLine = true;
        final Spanned test = HtmlUtils.parseHtml("<color=" + color +
                "><222222></color>: gsjiknsknfkdjngjdkngdfkknjfkdnf");
        final SpannableStringBuilder expected = new SpannableStringBuilder();
        expected.append("<222222>: gsjiknsknfkdjngjdkngdfkknjfkdnf");
        expected.setSpan(new ForegroundColorSpan(222222), 1, 7,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        assertEquals(expected, test);

        final long time = System.nanoTime();
        for(int i = 0; i < 10000; i++) {
            Html.fromHtml("<color=" + color +
                    "><222222></color>: gsjiknsknfkdjngjdkngdfkknjfkdnf");
        }
        final long totla = System.nanoTime() - time;

        System.out.println(totla);

        final long time2 = System.nanoTime();
        for(int i = 0; i < 10000; i++) {
            HtmlUtils.parseHtml("<color=" + color +
                    ">222222</color>: gsjiknsknfkdjngjdkngdfkknjfkdnf");
        }
        final long totla2 = System.nanoTime() - time2;

        System.out.println(totla2);
    }
}
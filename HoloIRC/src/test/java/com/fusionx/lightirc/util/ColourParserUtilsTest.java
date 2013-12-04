package com.fusionx.lightirc.util;

import com.fusionx.androidirclibrary.util.ColourParserUtils;
import com.fusionx.lightirc.RobolectricGradleTestRunner;
import com.fusionx.lightirc.misc.AppPreferences;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
public class ColourParserUtilsTest {

    @Test
    public void parseTest() {
        AppPreferences.highlightLine = true;
        final SpannableStringBuilder expected = new SpannableStringBuilder();
        expected.append("Welcome to Arch Linux World Domination, Inc. <+> Remember: Arch Linux " +
                "does not like to " +
                "be neglected. <+> Latest news:  https://www.archlinux.org/news/ <+> Adopt AUR " +
                "orphans https://bbs.archlinux.org/viewtopic.php?id=50869");
        final Spanned test = ColourParserUtils.parseMarkup(expected.toString());
        assertEquals(expected, test);

        final Spanned test2 = ColourParserUtils.parseMarkup("You " +
                "(<color=-14876040>tilal6991</color>) are now known as " +
                "<color=-14876040>tilal</color>");
        final SpannableStringBuilder expected2 = new SpannableStringBuilder();
        expected2.append("You (tilal6991) are now known as tilal");
        assertEquals(expected2, test2);
    }
}
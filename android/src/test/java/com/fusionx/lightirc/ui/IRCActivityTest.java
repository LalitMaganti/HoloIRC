package com.fusionx.lightirc.ui;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class IRCActivityTest {

    @Test
    public void onUserActionMenuClicked() {
        final MainActivity activity = Robolectric.buildActivity(MainActivity.class).create().get();
        assertNotNull(activity);
    }
}
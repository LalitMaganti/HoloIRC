package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenuItem;

import android.view.MenuItem;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class IRCActivityTest {

    @Test
    public void onUserActionMenuClicked() {
        final IRCActivity activity = Robolectric.buildActivity(IRCActivity.class).create().get();
        assertNotNull(activity);

        final MenuItem item = new TestMenuItem() {
            @Override
            public int getItemId() {
                return R.id.activity_server_channel_ab_users;
            }
        };
        activity.onOptionsItemSelected(item);
        assertTrue(activity.mUserSlidingMenu.isMenuShowing());
    }
}
package com.fusionx.lightirc.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.ircfragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ircfragments.UserFragment;

public class IRCViewPager extends ViewPager {
    public IRCViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public IRCPagerAdapter getAdapter() {
        return (IRCPagerAdapter) super.getAdapter();
    }

    public int onNewChannelJoined(final String channelName, final boolean mentioned) {
        final ChannelFragment channel = new ChannelFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("title", channelName);

        channel.setArguments(bundle);

        final int position = getAdapter().addFragment(channel);

        if (mentioned) {
            setCurrentItem(position, true);
        }

        return position;
    }

    public int onNewPrivateMessage(final String userNick) {
        final UserFragment userFragment = new UserFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("title", userNick);
        userFragment.setArguments(bundle);

        final int position = getAdapter().addFragment(userFragment);

        setCurrentItem(position, true);

        return position;
    }

    public void disconnect() {
        getAdapter().removeAllButServer();
        getAdapter().disableAllEditTexts();
    }
}

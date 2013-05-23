/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ServerFragment;
import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.irc.LightBuilder;
import com.fusionx.lightirc.irc.LightChannel;
import com.fusionx.lightirc.listeners.ActivityListener;
import com.fusionx.lightirc.parser.MessageParser;
import com.fusionx.lightirc.services.IRCService;
import org.pircbotx.Channel;

import java.lang.reflect.Field;

public class ServerChannelActivity extends FragmentActivity implements TabListener, OnPageChangeListener {
    private IRCPagerAdapter mIRCPagerAdapter;
    private ViewPager mViewPager;
    private LightBuilder builder;
    private ActivityListener listener;
    private IRCService service;
    private final MessageParser parser = new MessageParser();

    private void addTab(final int i) {
        final ActionBar actionBar = getActionBar();
        actionBar.addTab(actionBar.newTab()
                .setText(mIRCPagerAdapter.getPageTitle(i))
                .setTabListener(this));
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className,
                                       final IBinder binder) {
            service = ((IRCService.IRCBinder) binder).getService();
            parser.setService(service);
            final ServerFragment d = new ServerFragment();
            Bundle b = new Bundle();
            b.putString("title", builder.getTitle());

            if (service.getBot(builder.getTitle()) != null) {
                LightBot bot = service.getBot(builder.getTitle());
                bot.getConfiguration().getListenerManager().addListener(listener);
                b.putString("buffer", bot.getBuffer());
                d.setArguments(b);
                mIRCPagerAdapter.addView(d);
                addTab(0);

                for (final Channel channelName : bot.getUserBot().getChannels()) {
                    onNewChannelJoined(channelName.getName(), bot.getNick(), ((LightChannel) channelName).getBuffer());
                }
            } else {
                builder.getListenerManager().addListener(listener);
                service.connectToServer(builder);
                d.setArguments(b);
                mIRCPagerAdapter.addView(d);
                addTab(0);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            // This should never happen
        }
    };

    @Override
    public void onDestroy() {
        unbindService(mConnection);
        if(service.getBot(builder.getTitle()) != null) {
            service.getBot(builder.getTitle()).getConfiguration().getListenerManager().removeListener(listener);
        }
        super.onDestroy();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_server_channel);

        mIRCPagerAdapter = new IRCPagerAdapter(getSupportFragmentManager());
        listener = new ActivityListener(this, mIRCPagerAdapter);

        builder = getIntent().getExtras().getParcelable("server");

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mIRCPagerAdapter);
        mViewPager.setOnPageChangeListener(this);

        final Intent service = new Intent(this, IRCService.class);
        service.putExtra("server", true);
        service.putExtra("stop", false);
        startService(service);
        bindService(service, mConnection, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server_channel_ab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Intent intent = new Intent(this, MainServerListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(intent);
                return true;
            case R.id.activity_server_channel_ab_part:
                partFromChannel();
                return true;
            case R.id.activity_server_channel_ab_disconnect:
                disconnect();
                startActivity(intent);
                return true;
            case R.id.activity_server_channel_ab_users:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(final int arg0) {
    }

    @Override
    public void onPageSelected(final int position) {
        invalidateOptionsMenu();
        getActionBar().setSelectedNavigationItem(position);
        // Hack for http://code.google.com/p/android/issues/detail?id=38500
        setSpinnerSelectedNavigationItem(position);
    }

    @Override
    public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
    }

    private void partFromChannel() {
        int index = mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(index - 1);
        service.partFromChannel(builder.getTitle(), ((ChannelFragment) mIRCPagerAdapter.getItem(index)).getTitle());
        removeTab(index);
        mIRCPagerAdapter.removeView(index);
    }

    private void removeTab(final int i) {
        final ActionBar actionBar = getActionBar();
        actionBar.removeTabAt(i);
    }

    // Hack for http://code.google.com/p/android/issues/detail?id=38500
    private void setSpinnerSelectedNavigationItem(int position) {
        try {
            int id = getResources()
                    .getIdentifier("action_bar", "id", "android");
            View actionBarView = findViewById(id);

            Class<?> actionBarViewClass = actionBarView.getClass();
            Field mTabScrollViewField = actionBarViewClass
                    .getDeclaredField("mTabScrollView");
            mTabScrollViewField.setAccessible(true);

            Object mTabScrollView = mTabScrollViewField.get(actionBarView);
            if (mTabScrollView == null) {
                return;
            }

            Field mTabSpinnerField = mTabScrollView.getClass()
                    .getDeclaredField("mTabSpinner");
            mTabSpinnerField.setAccessible(true);

            Object mTabSpinner = mTabSpinnerField.get(mTabScrollView);
            if (mTabSpinner != null) {
                ((Spinner) mTabSpinner).setSelection(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onNewChannelJoined(final String channelName, final String nick, final String buffer) {
        final ChannelFragment channel = new ChannelFragment();
        final Bundle b = new Bundle();
        b.putString("channel", channelName);
        b.putString("nick", nick);
        b.putString("serverName", builder.getTitle());
        b.putString("buffer", buffer);
        channel.setArguments(b);

        final int position = mIRCPagerAdapter.addView(channel);
        addTab(position);
        getActionBar().getTabAt(position).setText(channelName);
        mViewPager.setOffscreenPageLimit(position);
    }

    private void disconnect() {
        service.disconnectFromServer((String) mIRCPagerAdapter.getPageTitle(0));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean server = (mViewPager.getCurrentItem() == 0);
        menu.findItem(R.id.activity_server_channel_ab_part).setVisible(!server);
        menu.findItem(R.id.activity_server_channel_ab_users).setVisible(!server);
        return super.onPrepareOptionsMenu(menu);
    }

    public void channelMessageToParse(String serverName, String channelName, String message) {
        parser.channelMessageToParse(serverName, channelName, message);
    }

    public void serverMessageToParse(String serverName, String message) {
        parser.serverMessageToParse(serverName, message);
    }
}
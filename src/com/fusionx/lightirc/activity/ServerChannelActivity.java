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
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Spinner;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.fragments.*;
import com.fusionx.lightirc.listeners.ActivityListener;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.parser.MessageParser;
import com.fusionx.lightirc.service.IRCService;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

public class ServerChannelActivity extends FragmentActivity implements TabListener, OnPageChangeListener {
    private UserListFragment mUserFragment;
    private IRCPagerAdapter mIRCPagerAdapter;
    private ViewPager mViewPager;
    private Configuration.Builder mBuilder;
    private ActivityListener mListener;
    private IRCService mService;
    private SlidingMenu mSlidingMenu;
    private String mentionString;

    @Getter(AccessLevel.PUBLIC)
    private final MessageParser parser = new MessageParser();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Utils.getThemeInt(getApplicationContext()));

        setContentView(R.layout.activity_server_channel);
        setUpSlidingMenu();

        mentionString = getIntent().getExtras().getString("mention", "");

        mIRCPagerAdapter = new IRCPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mIRCPagerAdapter);
        mViewPager.setOnPageChangeListener(this);

        mListener = new ActivityListener(this, mIRCPagerAdapter, mViewPager);

        mBuilder = getIntent().getExtras().getParcelable("server");

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        final Intent service = new Intent(this, IRCService.class);
        service.putExtra("server", true);
        service.putExtra("serverName", mBuilder.getTitle());
        service.putExtra("stop", false);
        startService(service);
        bindService(service, mConnection, 0);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCService.IRCBinder) binder).getService();

            parser.setService(mService);

            final Bundle bundle = new Bundle();
            bundle.putString("title", mBuilder.getTitle());

            mListener.setArrayAdapter((UserListAdapter) mUserFragment.getListAdapter());

            final ServerFragment fragment = new ServerFragment();

            if (mService.getBot(mBuilder.getTitle()) != null) {
                PircBotX bot = mService.getBot(mBuilder.getTitle());
                bot.getConfiguration().getListenerManager().addListener(mListener);
                bundle.putString("buffer", bot.getBuffer());
                fragment.setArguments(bundle);
                mIRCPagerAdapter.addView(fragment);
                addTab(mBuilder.getTitle());

                if (mService.getBot(mBuilder.getTitle()).getStatus().equals("Connected")) {
                    for (final Channel channelName : bot.getUserBot().getChannels()) {
                        onNewChannelJoined(channelName.getName(), channelName.getBuffer(), channelName.getUserList());
                    }
                    for (final User user : bot.getUserChannelDao().getPrivateMessages()) {
                        onNewPrivateMessage(user.getNick(), user.getBuffer());
                    }
                }
            } else {
                mBuilder.getListenerManager().addListener(mListener);
                mService.connectToServer(mBuilder);
                fragment.setArguments(bundle);
                mIRCPagerAdapter.addView(fragment);
                addTab(mBuilder.getTitle());
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            finish();
        }
    };

    private void setUpSlidingMenu() {
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setMode(SlidingMenu.RIGHT);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        mSlidingMenu.setMenu(R.layout.slding_menu_fragment_user);

        mUserFragment = (UserListFragment) getSupportFragmentManager().findFragmentById(R.id.user_fragment);
    }

    // Page change stuff
    @Override
    public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(final int arg0) {
    }

    @Override
    public void onPageSelected(final int position) {
        invalidateOptionsMenu();
        mSlidingMenu.showContent();

        if (mIRCPagerAdapter.getItem(position).getView() != null) {
            final ScrollView scrollView = (ScrollView) mIRCPagerAdapter
                    .getItem(position).getView().findViewById(R.id.scrollview);
            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setSelectedNavigationItem(position);
        }

        // Hack for http://code.google.com/p/android/issues/detail?id=38500
        setSpinnerSelectedNavigationItem(position);
    }

    private void userListUpdate() {
        final ChannelFragment fragment = (ChannelFragment) mIRCPagerAdapter.getItem(mViewPager.getCurrentItem());
        final UserListAdapter adapter = ((UserListAdapter) mUserFragment.getListAdapter());
        adapter.clear();

        final ArrayList<String> userList = fragment.getUserList();
        if (userList != null) {
            adapter.addAll(userList);
            adapter.sort();
        }
    }

    public void userListMention(final Set<String> users) {
        for (String userNick : users) {
            final ChannelFragment channel = (ChannelFragment) mIRCPagerAdapter.getItem(mViewPager.getCurrentItem());
            String edit = channel.getEditText().getText().toString();
            edit = Html.fromHtml(userNick) + ": " + edit;
            channel.getEditText().setText("");
            channel.getEditText().append(edit);
            channel.getEditText().requestFocus();
        }

        mSlidingMenu.showContent();
    }

    // Tab change listeners
    @Override
    public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition(), true);
    }

    @Override
    public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
    }

    // New stuff
    private void addTab(String title) {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.addTab(actionBar.newTab().setText(title).setTabListener(this));
        }
    }

    public void onNewChannelJoined(final String channelName,
                                   final String buffer, final ArrayList<String> userList) {
        final ChannelFragment channel = new ChannelFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("channel", channelName);
        bundle.putString("serverName", mBuilder.getTitle());
        bundle.putString("buffer", buffer);
        bundle.putStringArrayList("userList", userList);
        channel.setArguments(bundle);

        final int position = mIRCPagerAdapter.addView(channel);
        addTab(channelName);
        getActionBar().getTabAt(position).setText(channelName);

        if (mentionString.equals(channelName)) {
            mViewPager.setCurrentItem(position, true);
        }

        mViewPager.setOffscreenPageLimit(position);
    }

    public void onNewPrivateMessage(final String userNick, final String buffer) {
        final PMFragment pmFragment = new PMFragment();
        final Bundle b = new Bundle();
        b.putString("serverName", mBuilder.getTitle());
        b.putString("buffer", buffer);
        b.putString("nick", userNick);
        pmFragment.setArguments(b);

        final int position = mIRCPagerAdapter.addView(pmFragment);
        addTab(userNick);
        getActionBar().getTabAt(position).setText(userNick);

        if (mentionString.equals(userNick)) {
            mViewPager.setCurrentItem(position, true);
        }

        mViewPager.setOffscreenPageLimit(position);
    }

    // Removal stuff
    private void removeTab(final int i) {
        final ActionBar actionBar = getActionBar();
        actionBar.removeTabAt(i);
    }

    private void disconnect() {
        mService.disconnectFromServer(mBuilder.getTitle());
    }

    private void closePMConversation() {
        int index = mViewPager.getCurrentItem();

        mViewPager.setCurrentItem(index - 1, true);

        mService.removePrivateMessage(mBuilder.getTitle(), ((IRCFragment) mIRCPagerAdapter.getItem(index)).getTitle());

        mIRCPagerAdapter.removeView(index);
        removeTab(index);
    }

    private void partFromChannel() {
        int index = mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(index - 1, true);

        mService.partFromChannel(mBuilder.getTitle(),
                ((ChannelFragment) mIRCPagerAdapter.getItem(index)).getTitle());

        mIRCPagerAdapter.removeView(index);
        removeTab(index);
    }

    @Override
    public void onDestroy() {
        if (mService.getBot(mBuilder.getTitle()) != null) {
            mService.getBot(mBuilder.getTitle()).getConfiguration().getListenerManager().removeListener(mListener);
        }
        unbindService(mConnection);

        super.onDestroy();
    }

    // OptionsMenu stuff
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server_channel_ab, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final int server = mViewPager.getCurrentItem();
        if (server != 0) {
            final IRCFragment fragment = (IRCFragment) mIRCPagerAdapter.getItem(server);
            final boolean channel = fragment instanceof ChannelFragment;
            final boolean userPM = fragment instanceof PMFragment;

            menu.findItem(R.id.activity_server_channel_ab_part).setVisible(channel);
            menu.findItem(R.id.activity_server_channel_ab_users).setVisible(channel);
            menu.findItem(R.id.activity_server_channel_ab_close).setVisible(userPM);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final Intent intent = new Intent(this, MainServerListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        switch (item.getItemId()) {
            case R.id.activity_server_channel_ab_part:
                partFromChannel();
                return true;
            case R.id.activity_server_channel_ab_disconnect:
                disconnect();
                startActivity(intent);
                return true;
            case R.id.activity_server_channel_ab_users:
                if (!mSlidingMenu.isMenuShowing()) {
                    userListUpdate();
                }
                mSlidingMenu.toggle();
                return true;
            case R.id.activity_server_channel_ab_close:
                closePMConversation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Hack for http://code.google.com/p/android/issues/detail?id=38500
    private void setSpinnerSelectedNavigationItem(final int position) {
        try {
            final int id = getResources()
                    .getIdentifier("action_bar", "id", "android");
            final View actionBarView = findViewById(id);

            final Class<?> actionBarViewClass = actionBarView.getClass();
            final Field mTabScrollViewField = actionBarViewClass
                    .getDeclaredField("mTabScrollView");
            mTabScrollViewField.setAccessible(true);

            final Object mTabScrollView = mTabScrollViewField.get(actionBarView);
            if (mTabScrollView == null) {
                return;
            }

            final Field mTabSpinnerField = mTabScrollView.getClass()
                    .getDeclaredField("mTabSpinner");
            mTabSpinnerField.setAccessible(true);

            final Object mTabSpinner = mTabSpinnerField.get(mTabScrollView);
            if (mTabSpinner != null) {
                ((Spinner) mTabSpinner).setSelection(position);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
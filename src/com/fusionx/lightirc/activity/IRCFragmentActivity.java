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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ActionsArrayAdapter;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.fragments.ServerChannelActionsFragment;
import com.fusionx.lightirc.fragments.UserListFragment;
import com.fusionx.lightirc.fragments.ircfragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ircfragments.IRCFragment;
import com.fusionx.lightirc.fragments.ircfragments.PMFragment;
import com.fusionx.lightirc.fragments.ircfragments.ServerFragment;
import com.fusionx.lightirc.listeners.ActivityListener;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.parser.MessageParser;
import com.fusionx.lightirc.service.IRCService;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

public class IRCFragmentActivity extends FragmentActivity implements TabListener, OnPageChangeListener {
    private UserListFragment mUserFragment;
    private IRCPagerAdapter mIRCPagerAdapter;
    private ViewPager mViewPager;
    private ActivityListener mListener;
    private String mentionString;
    private SlidingMenu mUserSlidingMenu;
    private SlidingMenu mActionsSlidingMenu;
    private Configuration.Builder builder;
    private IRCService service;

    @Getter(AccessLevel.PUBLIC)
    private final MessageParser parser = new MessageParser();

    private String getServerTitle() {
        return builder.getTitle();
    }

    public PircBotX getBot() {
        return service.getBot(getServerTitle());
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Utils.getThemeInt(getApplicationContext()));

        setContentView(R.layout.activity_server_channel);
        setUpSlidingMenu();

        mUserFragment = (UserListFragment) getSupportFragmentManager().findFragmentById(R.id.user_fragment);

        mentionString = getIntent().getExtras().getString("mention", "");

        mIRCPagerAdapter = new IRCPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mIRCPagerAdapter);
        mViewPager.setOnPageChangeListener(this);

        mListener = new ActivityListener(this, mIRCPagerAdapter, mViewPager);

        builder = getIntent().getExtras().getParcelable("server");

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final Intent service = new Intent(this, IRCService.class);
        service.putExtra("server", true);
        service.putExtra("serverName", getServerTitle());
        service.putExtra("stop", false);
        service.putExtra("setBound", getServerTitle());
        startService(service);
        bindService(service, mConnection, 0);
    }

    public final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            service = ((IRCService.IRCBinder) binder).getService();
            parser.setService(service);

            final PircBotX bot = getBot();
            if (bot != null) {
                bot.getConfiguration().getListenerManager().addListener(mListener);
                addServerFragment();
                if (bot.getStatus().equals(getString(R.string.status_connected))) {
                    for (final Channel channelName : bot.getUserBot().getChannels()) {
                        onNewChannelJoined(channelName.getName());
                    }
                    for (final User user : bot.getUserChannelDao().getPrivateMessages()) {
                        onNewPrivateMessage(user.getNick());
                    }
                }
            } else {
                builder.getListenerManager().addListener(mListener);
                service.connectToServer(builder);
                addServerFragment();
            }
        }

        private void addServerFragment() {
            final ServerFragment fragment = new ServerFragment();
            final Bundle bundle = new Bundle();
            bundle.putString("title", getServerTitle());
            fragment.setArguments(bundle);

            mIRCPagerAdapter.addView(fragment);
            addTab(getServerTitle());
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            onUnexpectedDisconnect();
        }
    };

    private void setUpSlidingMenu() {
        mUserSlidingMenu = new SlidingMenu(this);
        mUserSlidingMenu.setMode(SlidingMenu.RIGHT);
        mUserSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mUserSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        mUserSlidingMenu.setMenu(R.layout.slding_menu_fragment_user);
        mUserSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

        mActionsSlidingMenu = new SlidingMenu(this);
        mActionsSlidingMenu.setMode(SlidingMenu.LEFT);
        mActionsSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mActionsSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mActionsSlidingMenu.setTouchmodeMarginThreshold(5);
        mActionsSlidingMenu.setMenu(R.layout.sliding_menu_fragment_actions);
        mActionsSlidingMenu.setBehindWidthRes(R.dimen.server_channel_sliding_actions_menu_width);
        mActionsSlidingMenu.setOnOpenListener(actionsSlidingOpenListener);
        mActionsSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
    }

    private final SlidingMenu.OnOpenListener actionsSlidingOpenListener = new SlidingMenu.OnOpenListener() {
        @Override
        public void onOpen() {
            final ServerChannelActionsFragment actionsFragment = (ServerChannelActionsFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.actions_fragment);
            final ActionsArrayAdapter arrayAdapter = (ActionsArrayAdapter) actionsFragment.getListView().getAdapter();
            arrayAdapter.setConnected(service != null && getBot().getStatus().equals(getString(R.string.status_connected)));
            arrayAdapter.notifyDataSetChanged();
        }
    };

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
        closeAllSlidingMenus();

        mIRCPagerAdapter.setCurrentItemIndex(position);

        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setSelectedNavigationItem(position);
        }

        // Hack for http://code.google.com/p/android/issues/detail?id=38500
        setSpinnerSelectedNavigationItem(position);
    }

    // Tab change listeners
    @Override
    public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition(), true);
    }

    @Override
    public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
    }

    // New stuff
    private void addTab(final String title) {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.addTab(actionBar.newTab().setText(title).setTabListener(this));
        }
    }

    public int onNewChannelJoined(final String channelName) {
        final ChannelFragment channel = new ChannelFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("title", channelName);
        bundle.putString("serverName", getServerTitle());

        channel.setArguments(bundle);

        final int position = mIRCPagerAdapter.addView(channel);
        addTab(channelName);
        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.getTabAt(position).setText(channelName);
        }

        if (mentionString.equals(channelName)) {
            mViewPager.setCurrentItem(position, true);
        }
        return position;
    }

    public void onNewPrivateMessage(final String userNick) {
        final PMFragment pmFragment = new PMFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("serverName", builder.getTitle());
        bundle.putString("title", userNick);
        pmFragment.setArguments(bundle);

        final int position = mIRCPagerAdapter.addView(pmFragment);
        addTab(userNick);
        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.getTabAt(position).setText(userNick);
        }

        mViewPager.setCurrentItem(position, true);
    }

    // Removal stuff
    public void removeTab(final int i) {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.removeTabAt(i);
        }
    }

    private void closeIRCFragment(final boolean channel) {
        final int index = mViewPager.getCurrentItem();
        final String title = ((IRCFragment) mIRCPagerAdapter.getItem(index)).getTitle();

        mViewPager.setCurrentItem(index - 1, true);
        removeTab(index);
        if (!channel) {
            mIRCPagerAdapter.removeView(title);
        }

        final AsyncTask<Void, Void, Void> closeFragment = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... v) {
                final UserChannelDao<User, Channel> dao = getBot().getUserChannelDao();
                if (channel) {
                    dao.getChannel(title).send().part();
                } else {
                    dao.removePrivateMessage(title);
                    dao.getUser(title).setBuffer("");
                }
                return null;
            }
        };
        closeFragment.execute();
    }

    @Override
    public void onResume() {
        if (service != null) {
            final PircBotX bot = getBot();
            if (bot != null) {
                bot.getConfiguration().getListenerManager().addListener(mListener);
            }
            service.setBoundToIRCFragmentActivity(getServerTitle());
        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (service != null) {
            unbindService(mConnection);
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (service != null) {
            if (getBot() != null) {
                getBot().getConfiguration().getListenerManager().removeListener(mListener);
            }
            service.setBoundToIRCFragmentActivity(null);
        }

        super.onPause();
    }

    // Options Menu stuff
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server_channel_ab, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final int server = mViewPager.getCurrentItem();
        if (server != 0 && service != null) {
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
            case android.R.id.home:
                mActionsSlidingMenu.toggle();
                return true;
            case R.id.activity_server_channel_ab_part:
                closeIRCFragment(true);
                return true;
            case R.id.activity_server_channel_ab_users:
                if (!mUserSlidingMenu.isMenuShowing()) {
                    userListUpdate();
                    mUserFragment.getListView().smoothScrollToPosition(0);
                }
                mUserSlidingMenu.toggle();
                return true;
            case R.id.activity_server_channel_ab_close:
                closeIRCFragment(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Sliding Menu
    public void closeAllSlidingMenus() {
        mActionsSlidingMenu.showContent();
        mUserSlidingMenu.showContent();
    }

    private void userListUpdate() {
        final ChannelFragment fragment = (ChannelFragment) mIRCPagerAdapter.getItem(mViewPager.getCurrentItem());
        final UserListAdapter adapter = ((UserListAdapter) mUserFragment.getListAdapter());
        adapter.clear();

        final ArrayList<String> userList = getBot().getUserChannelDao().getChannel(fragment.getTitle()).getUserList();
        if (userList != null) {
            adapter.addAll(userList);
            adapter.sort();
        }
    }

    public void userListMention(final Set<String> users) {
        for (final String userNick : users) {
            final ChannelFragment channel = (ChannelFragment) mIRCPagerAdapter.getItem(mViewPager.getCurrentItem());
            String edit = channel.getEditText().getText().toString();
            edit = Html.fromHtml(userNick) + ": " + edit;
            channel.getEditText().clearComposingText();
            channel.getEditText().append(edit);
            channel.getEditText().requestFocus();
        }

        closeAllSlidingMenus();
    }

    public void onUnexpectedDisconnect() {
        mViewPager.setCurrentItem(0, true);

        mIRCPagerAdapter.removeAllButServer();
        for (int i = 1; i < mIRCPagerAdapter.getCount(); i++) {
            removeTab(i);
        }

        unbindService(mConnection);
        service = null;

        mIRCPagerAdapter.disableAllEditTexts();
        closeAllSlidingMenus();
    }

    public void disconnect() {
        getBot().getConfiguration().getListenerManager().removeListener(mListener);

        getBot().sendIRC().quitServer(Utils.getQuitReason(getApplicationContext()));
        service.disconnectFromServer(getServerTitle());
        unbindService(mConnection);
        service = null;

        final Intent intent = new Intent(this, MainServerListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Hack for http://code.google.com/p/android/issues/detail?id=38500
    private void setSpinnerSelectedNavigationItem(final int position) {
        try {
            final int id = getResources().getIdentifier("action_bar", "id", "android");
            final View actionBarView = findViewById(id);

            final Class<?> actionBarViewClass = actionBarView.getClass();
            final Field mTabScrollViewField = actionBarViewClass.getDeclaredField("mTabScrollView");
            mTabScrollViewField.setAccessible(true);

            final Object mTabScrollView = mTabScrollViewField.get(actionBarView);
            if (mTabScrollView == null) {
                return;
            }

            final Field mTabSpinnerField = mTabScrollView.getClass().getDeclaredField("mTabSpinner");
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
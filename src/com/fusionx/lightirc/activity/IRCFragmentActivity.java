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
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ActionsArrayAdapter;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
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
import com.fusionx.lightlibrary.activities.AbstractPagerActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.*;

import java.util.ArrayList;
import java.util.Set;

public class IRCFragmentActivity extends AbstractPagerActivity {
    private UserListFragment mUserFragment;
    private ServerChannelActionsFragment actionsFragment;
    private ActivityListener mListener;
    private String mentionString;
    private SlidingMenu mUserSlidingMenu;
    private SlidingMenu mActionsSlidingMenu;
    private IRCService service;

    @Getter(AccessLevel.PUBLIC)
    private IRCPagerAdapter ircPagerAdapter;

    @Getter(AccessLevel.PUBLIC)
    private ViewPager viewPager;

    @Getter(AccessLevel.PRIVATE)
    private String serverTitle;

    @Getter(AccessLevel.PUBLIC)
    private final MessageParser parser = new MessageParser();

    public PircBotX getBot() {
        return service.getBot(getServerTitle());
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setTheme(Utils.getThemeInt(getApplicationContext()));

        setContentView(R.layout.activity_server_channel);
        setUpSlidingMenu();

        mUserFragment = (UserListFragment) getSupportFragmentManager().findFragmentById(R.id.user_fragment);
        actionsFragment = (ServerChannelActionsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.actions_fragment);

        mentionString = getIntent().getExtras().getString("mention", "");

        final Configuration.Builder<PircBotX> builder = getIntent().getExtras().getParcelable("server");
        serverTitle = builder.getTitle();

        ircPagerAdapter = new IRCPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(ircPagerAdapter);
        viewPager.setOnPageChangeListener(this);

        mListener = new ActivityListener(this);

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

        super.onCreate(savedInstanceState);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            service = ((IRCService.IRCBinder) binder).getService();
            parser.setService(service);

            if (getBot() != null) {
                getBot().getConfiguration().getListenerManager().addListener(mListener);
                addServerFragment();
                if (getBot().getStatus().equals(getString(R.string.status_connected))) {
                    for (final Channel channelName : getBot().getUserBot().getChannels()) {
                        onNewChannelJoined(channelName.getName());
                    }
                    for (final User user : getBot().getUserChannelDao().getPrivateMessages()) {
                        onNewPrivateMessage(user.getNick());
                    }
                }
            } else {
                final Configuration.Builder<PircBotX> builder = getIntent().getExtras().getParcelable("server");
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

            ircPagerAdapter.addFragment(fragment);
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
            final ActionsArrayAdapter arrayAdapter = (ActionsArrayAdapter) actionsFragment.getListView().getAdapter();
            arrayAdapter.setConnected(service != null && getBot().getStatus().equals(getString(R.string.status_connected)));
            arrayAdapter.notifyDataSetChanged();
        }
    };

    public IRCFragment getCurrentItem() {
        return (IRCFragment) ircPagerAdapter.getItem(viewPager.getCurrentItem());
    }

    // Page change stuff
    @Override
    public void onPageSelected(final int position) {
        invalidateOptionsMenu();
        closeAllSlidingMenus();

        ircPagerAdapter.setCurrentItemIndex(position);

        super.onPageSelected(position);
    }

    // Tab change listeners
    @Override
    public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition(), true);
    }

    // New stuff
    public int onNewChannelJoined(final String channelName) {
        final ChannelFragment channel = new ChannelFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("title", channelName);
        bundle.putString("serverName", getServerTitle());

        channel.setArguments(bundle);

        final int position = ircPagerAdapter.addFragment(channel);
        addTab(channelName);
        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.getTabAt(position).setText(channelName);
        }

        if (mentionString.equals(channelName)) {
            viewPager.setCurrentItem(position, true);
        }
        return position;
    }

    public void onNewPrivateMessage(final String userNick) {
        final PMFragment pmFragment = new PMFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("serverName", getServerTitle());
        bundle.putString("title", userNick);
        pmFragment.setArguments(bundle);

        final int position = ircPagerAdapter.addFragment(pmFragment);
        addTab(userNick);
        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.getTabAt(position).setText(userNick);
        }

        viewPager.setCurrentItem(position, true);
    }

    public void removeIRCFragment(final int index) {
        viewPager.setCurrentItem(index - 1, true);
        removeTab(index);
        ircPagerAdapter.removeFragment(index);
    }

    // Removal stuff
    private void closeIRCFragment(final boolean channel) {
        final int index = viewPager.getCurrentItem();
        if (channel) {
            viewPager.setCurrentItem(index - 1, true);
        } else {
            removeIRCFragment(index);
        }

        final AsyncTask<Void, Void, Void> closeFragment = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... v) {
                final String title = getCurrentItem().getTitle();
                final UserChannelDao<User, Channel> dao = getBot().getUserChannelDao();
                if (channel) {
                    dao.getChannel(title).send().part(Utils.getPartReason(getApplicationContext()));
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
            service.setBoundToServer(getServerTitle());
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
            service.setBoundToServer(null);
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
        final IRCFragment fragment = getCurrentItem();
        if (!(fragment instanceof ServerFragment) && service != null) {
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
                    final ArrayList<String> userList = ((ChannelFragment) getCurrentItem()).getUserList();
                    mUserFragment.userListUpdate(userList);
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

    public void userListMention(final Set<String> users) {
        for (final String userNick : users) {
            final ChannelFragment channel = (ChannelFragment) ircPagerAdapter.getItem(viewPager.getCurrentItem());
            String edit = channel.getEditText().getText().toString();
            edit = Html.fromHtml(userNick) + ": " + edit;
            channel.getEditText().clearComposingText();
            channel.getEditText().append(edit);
            channel.getEditText().requestFocus();
        }

        closeAllSlidingMenus();
    }

    public void onUnexpectedDisconnect() {
        viewPager.setCurrentItem(0, true);

        for (int i = 1; i < ircPagerAdapter.getCount() - 1; i++) {
            removeTab(i);
        }
        ircPagerAdapter.removeAllButServer();

        unbindService(mConnection);
        service = null;

        ircPagerAdapter.disableAllEditTexts();
        closeAllSlidingMenus();
    }

    public void disconnect() {
        getBot().getConfiguration().getListenerManager().removeListener(mListener);

        service.disconnectFromServer(getServerTitle());
        unbindService(mConnection);
        service = null;

        final Intent intent = new Intent(this, MainServerListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
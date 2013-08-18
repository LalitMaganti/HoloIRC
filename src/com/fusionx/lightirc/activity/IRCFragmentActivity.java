/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.activity;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.fusionx.Utils;
import com.fusionx.irc.Channel;
import com.fusionx.irc.Server;
import com.fusionx.irc.ServerConfiguration;
import com.fusionx.irc.User;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ServerChannelEventType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.IRCActionsFragment;
import com.fusionx.lightirc.fragments.UserListFragment;
import com.fusionx.lightirc.fragments.ircfragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ircfragments.IRCFragment;
import com.fusionx.lightirc.fragments.ircfragments.ServerFragment;
import com.fusionx.lightirc.handlerabstract.ServerChannelHandler;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.ui.ActionsSlidingMenu;
import com.fusionx.lightirc.ui.IRCViewPager;
import com.fusionx.uiircinterface.IRCBridgeService;
import com.fusionx.uiircinterface.MessageSender;
import com.fusionx.uiircinterface.ServerCommandSender;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;

public class IRCFragmentActivity extends FragmentActivity implements
        UserListFragment.UserListListenerInterface, ChannelFragment.ChannelFragmentCallback,
        IRCActionsFragment.IRCActionsListenerInterface, ServerFragment.ServerFragmentCallback {

    private UserListFragment mUserFragment = null;
    private IRCBridgeService mService = null;
    private IRCViewPager mViewPager = null;
    private String mServerTitle = null;
    private SlidingMenu mUserSlidingMenu = null;
    private ActionsSlidingMenu mActionsSlidingMenu = null;
    private final ViewPagerOnPagerListener listener = new ViewPagerOnPagerListener();
    private IRCActionsFragment mActionsFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ServerConfiguration.Builder builder = getIntent().getParcelableExtra("server");
        mServerTitle = builder != null ? builder.getTitle() : null;

        setTheme(Utils.getThemeInt(getApplicationContext()));
        setContentView(R.layout.activity_server_channel);

        setUpSlidingMenu();

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mServerTitle);
        }
    }

    private void setUpSlidingMenu() {
        mUserFragment = new UserListFragment();

        mUserSlidingMenu = (SlidingMenu) findViewById(R.id.slidingmenulayout);
        mUserSlidingMenu.setContent(R.layout.view_pager);
        mUserSlidingMenu.setMenu(R.layout.sliding_ment_fragment_userlist);
        mUserSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mUserSlidingMenu.setBehindScrollScale(0);
        mUserSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        mUserSlidingMenu.setMode(SlidingMenu.RIGHT);
        mUserSlidingMenu.setBehindWidthRes(R.dimen.server_channel_sliding_actions_menu_width);

        mUserFragment = (UserListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userlist_fragment);

        mActionsSlidingMenu = new ActionsSlidingMenu(this);
        mActionsFragment = (IRCActionsFragment)
                getSupportFragmentManager().findFragmentById(R.id.actions_fragment);
        mActionsSlidingMenu.setBehindScrollScale(0);

        mActionsSlidingMenu.setOnOpenListener(mActionsFragment);
        mActionsSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
    }

    private void setUpViewPager() {
        final TypedArray a = getTheme().obtainStyledAttributes(new int[]
                {android.R.attr.windowBackground});
        final int background = a.getResourceId(0, 0);

        final IRCPagerAdapter adapter = new IRCPagerAdapter(getSupportFragmentManager());
        adapter.addServerFragment(mServerTitle);
        mViewPager = (IRCViewPager) findViewById(R.id.pager);
        mViewPager.setBackgroundResource(background);
        mViewPager.setAdapter(adapter);

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
        tabs.setOnPageChangeListener(listener);
        tabs.setBackgroundResource(R.color.sliding_menu_background);
        tabs.setTextColorResource(android.R.color.white);
        tabs.setIndicatorColorResource(android.R.color.white);
        mViewPager.getAdapter().setTabStrip(tabs);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mService == null) {
            setUpService();
        }
    }

    public void setUpService() {
        final Intent service = new Intent(this, IRCBridgeService.class);
        service.putExtra("server", true);
        service.putExtra("serverName", mServerTitle);
        service.putExtra("stop", false);
        service.putExtra("setBound", mServerTitle);

        startService(service);
        bindService(service, mConnection, 0);
    }

    @Override
    public void onStop() {
        super.onStop();

        MessageSender.getSender(mServerTitle).unregisterServerChannelHandler();

        if (mService != null) {
            mService.setServerDisplayed(null);
            unbindService(mConnection);
            mService = null;
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCBridgeService.IRCBinder) binder).getService();
            setUpViewPager();

            mService.setServerDisplayed(mServerTitle);
            final ServerConfiguration.Builder builder = getIntent()
                    .getParcelableExtra("server");

            if (getServer(true) != null) {
                if (isConnectedToServer()) {
                    for (final Channel channelName : getServer(false).getUser().getChannels()) {
                        onCreateChannelFragment(channelName.getName());
                    }
                    for (final User user : getServer(false).getUser().getPrivateMessages()) {
                        onCreatePMFragment(user.getNick());
                    }
                }
            } else {
                mService.connectToServer(builder);
            }

            MessageSender.getSender(builder.getTitle())
                    .registerServerChannelHandler(mServerChannelHandler);
        }

        // Should not occur
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService = null;
        }
    };

    private final ServerChannelHandler mServerChannelHandler = new ServerChannelHandler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ServerChannelEventType type = (ServerChannelEventType) bundle.getSerializable(EventBundleKeys
                    .eventType);
            final String message = bundle.getString(EventBundleKeys.message);
            switch (type) {
                case Join:
                    onNewChannelJoined(message, true);
                    break;
                case NewPrivateMessage:
                    onCreatePMFragment(message);
                    break;
            }
        }
    };

    // Options Menu stuff
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server_channel_ab, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(getCurrentlyDisplayedFragment()
                .equals(FragmentType.Channel));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActionsSlidingMenu.toggle();
                return true;
            case R.id.activity_server_channel_ab_users:
                if (!mUserSlidingMenu.isMenuShowing()) {
                    mUserFragment.userListUpdate(getCurrentItem().getTitle());
                    mUserFragment.getListView().smoothScrollToPosition(0);
                }
                mUserSlidingMenu.toggle();
                return true;
            default:
                return false;
        }
    }

    private IRCFragment getCurrentItem() {
        return mViewPager.getAdapter().getItem(mViewPager.getCurrentItem());
    }

    /*
     * CALLBACKS START HERE
     */
    // CommonIRCListener Callbacks
    @Override
    public Server getServer(final boolean nullAllowed) {
        Server server;
        if (mService == null || (server = mService.getServer(mServerTitle)) == null) {
            if (nullAllowed) {
                return null;
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            return server;
        }
    }

    @Override
    public String getServerTitle() {
        return mServerTitle;
    }

    @Override
    public void onCreatePMFragment(final String userNick) {
        mViewPager.onNewPrivateMessage(userNick);
    }

    @Override
    public void closeAllSlidingMenus() {
        mActionsSlidingMenu.showContent();
        mUserSlidingMenu.showContent();
    }

    @Override
    public boolean isConnectedToServer() {
        final Server server = getServer(true);

        return server != null && server.getStatus()
                .equals(getString(R.string.status_connected));
    }

    @Override
    public void selectServerFragment() {
        mViewPager.setCurrentItem(0, true);
    }

    // ActivityListener Callbacks
    //@Override
    public boolean isFragmentSelected(final String title) {
        return getCurrentItem().getTitle().equals(title);
    }

    //@Override
    public IRCFragment isFragmentAvailable(final String title) {
        return mViewPager.getAdapter().getFragment(title);
    }

    @Override
    public void switchFragmentAndRemove(final String tabName) {
        final int index = mViewPager.getAdapter().getIndexFromTitle(tabName);
        if (getCurrentItem().getTitle().equals(tabName)) {
            mViewPager.setCurrentItem(index - 1, true);
        }
        mViewPager.getAdapter().removeFragment(index);
    }

    @Override
    public void updateUserList(String channelName) {
        if (getCurrentItem().getTitle().equals(channelName)) {
            mUserFragment.notifyDataSetChanged();
        }
    }

    private void onCreateChannelFragment(final String channelName) {
        onNewChannelJoined(channelName, false);
    }

    //@Override
    public void onUnexpectedDisconnect() {
        closeAllSlidingMenus();
        selectServerFragment();

        mViewPager.disconnect();

        if (getServer(true) != null) {
            final AsyncTask<Void, Void, Void> unexpectedDisconnect = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    mService.setServerDisplayed(null);
                    mService.onUnexpectedDisconnect(mServerTitle);
                    mService = null;
                    return null;
                }
            };
            unexpectedDisconnect.execute();
        }
    }

    // UserListFragment Listener Callbacks
    @Override
    public void onUserMention(final ArrayList<User> users) {
        final ChannelFragment channel = (ChannelFragment) getCurrentItem();
        channel.onUserMention(users);

        closeAllSlidingMenus();
    }

    // IRCActionsFragment Listener Callbacks
    @Override
    public String getNick() {
        return getServer(false).getUser().getNick();
    }

    @Override
    public void disconnect() {
        mService.disconnectFromServer(mServerTitle);

        final Intent intent = new Intent(this, MainServerListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void removeCurrentTab() {
        final Server server = getServer(false);
        if (getCurrentlyDisplayedFragment().equals(FragmentType.User)) {
            ServerCommandSender.sendClosePrivateMessage(server,
                    server.getUserChannelInterface().getUser(getCurrentItem().getTitle()));

            switchFragmentAndRemove(getCurrentItem().getTitle());
        } else {
            ServerCommandSender.sendPart(server, getCurrentItem().getTitle(),
                    getApplicationContext());
        }
    }

    @Override
    public FragmentType getCurrentlyDisplayedFragment() {
        return getCurrentItem().getType();
    }

    // ServerFragment Listener Callbacks
    public void onNewChannelJoined(final String channelName, final boolean forceSwitch) {
        final boolean switchToTab = channelName.equals(getIntent().getStringExtra("mention"))
                || forceSwitch;
        mViewPager.onNewChannelJoined(channelName, switchToTab);
    }

    @Override
    public void connectedToServer() {
        if(mActionsSlidingMenu.isMenuShowing()) {
            mActionsFragment.setConnectedToServer();
        }
    }

    private class ViewPagerOnPagerListener extends ViewPager.SimpleOnPageChangeListener {
        // Page change stuff
        @Override
        public void onPageSelected(final int position) {
            supportInvalidateOptionsMenu();
            invalidateOptionsMenu();
            closeAllSlidingMenus();

            if (mUserFragment.getMode() != null) {
                mUserFragment.getMode().finish();
            }

            mViewPager.getAdapter().setCurrentItemIndex(position);
        }
    }
}
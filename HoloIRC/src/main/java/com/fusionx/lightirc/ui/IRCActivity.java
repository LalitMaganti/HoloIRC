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

package com.fusionx.lightirc.ui;

import com.astuetz.PagerSlidingTabStrip;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.FragmentType;
import com.fusionx.lightirc.ui.widget.DrawerToggle;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.ServerStatus;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.communication.ServerEventBus;
import com.fusionx.relay.event.channel.WorldUserEvent;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.DisconnectEvent;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.squareup.otto.Subscribe;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Collection;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;

import static butterknife.ButterKnife.findById;

/**
 * Activity which contains all the communication code between the fragments It also implements a lot
 * of callbacks to stop exposing objects to the fragments
 *
 * @author Lalit Maganti
 */
public abstract class IRCActivity extends ActionBarActivity implements UserListFragment.Callbacks,
        ServiceFragment.Callbacks, ActionsPagerFragment.Callbacks, IRCPagerFragment.Callbacks {

    /*
     * Listener used when the view pages changes pages
     */
    private final ViewPager.SimpleOnPageChangeListener mListener = new ViewPager
            .SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(final int position) {
            supportInvalidateOptionsMenu();
            closeAllSlidingMenus();

            mActionsPagerFragment.onPageChanged(mIRCPagerFragment.getCurrentType());

            if (mActionsSlidingMenu != null) {
                mActionsSlidingMenu.setTouchModeAbove(position == 0 ? SlidingMenu
                        .TOUCHMODE_FULLSCREEN : SlidingMenu.TOUCHMODE_MARGIN);
            }
            mUserSlidingMenu.setTouchModeAbove(position == 0 ? SlidingMenu.TOUCHMODE_NONE :
                    SlidingMenu.TOUCHMODE_MARGIN);
        }
    };

    private final Object mEventReceiver = new Object() {

        @Subscribe
        public void onDisconnected(final DisconnectEvent event) {
            if (event.userSent) {
                final Intent intent = new Intent(IRCActivity.this, ServerListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                getSupportActionBar().setSubtitle(getString(R.string.status_disconnected));
                closeAllSlidingMenus();
                mIRCPagerFragment.onUnexpectedDisconnect();
                mActionsPagerFragment.updateConnectionStatus(false);
                if (!event.retryPending && getServer() != null) {
                    mServiceFragment.onFinalUnexpectedDisconnect();
                }
            }
        }

        @Subscribe
        public void onServerConnected(final ConnectEvent event) {
            mActionsPagerFragment.updateConnectionStatus(true);
            getSupportActionBar().setSubtitle(MiscUtils.getStatusString(IRCActivity.this,
                    getServer().getStatus()));
        }

        @Subscribe
        public void onChannelMessage(final WorldUserEvent event) {
            if (mUserSlidingMenu.isMenuShowing()) {
                onUserListDisplayed();
            }
        }
    };

    protected ActionsPagerFragment mActionsPagerFragment;

    protected SlidingMenu mActionsSlidingMenu;

    protected DrawerToggle mDrawerToggle;

    // Sliding menus
    protected SlidingMenu mUserSlidingMenu;

    // The Fragments
    private ServiceFragment mServiceFragment;

    private UserListFragment mUserListFragment;

    private IRCPagerFragment mIRCPagerFragment;

    // Other objects
    private String mServerTitle;

    // Do not do any IRC work here - views may not have been set up, activities not instantiated
    // etc.
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irc);

        final ServerConfiguration.Builder builder = getIntent().getParcelableExtra("server");
        mServerTitle = builder != null ? builder.getTitle() : getIntent().getStringExtra
                ("serverTitle");

        final FragmentManager fm = getSupportFragmentManager();

        setUpSlidingMenu(fm);

        mIRCPagerFragment = (IRCPagerFragment) fm.findFragmentById(R.id.pager_fragment);
        mServiceFragment = (ServiceFragment) fm.findFragmentByTag("service");

        final boolean isFirstStart = mServiceFragment == null;
        final ActionBar actionBar = getSupportActionBar();
        if (isFirstStart) {
            mServiceFragment = new ServiceFragment();
            fm.beginTransaction().add(mServiceFragment, "service").commit();
            actionBar.setSubtitle(getString(R.string.status_connecting));
        } else if (getServer() != null) {
            final ServerEventBus bus = getServer().getServerEventBus();
            bus.register(mEventReceiver);
            // TODO - this is misnamed - rename it
            mIRCPagerFragment.onCreateServerFragment(mServerTitle);
            actionBar.setSubtitle(MiscUtils.getStatusString(this, getServer().getStatus()));
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mServerTitle);

        final PagerSlidingTabStrip tabs = findById(this, R.id.pager_tabs);
        tabs.setOnPageChangeListener(mListener);
        tabs.setTextColorResource(android.R.color.white);
    }

    private void setUpSlidingMenu(final FragmentManager manager) {
        mUserSlidingMenu = findById(this, R.id.user_sliding_menu);
        mUserSlidingMenu.setContent(R.layout.view_pager_fragment);
        mUserSlidingMenu.setMenu(R.layout.sliding_menu_fragment_userlist);
        mUserSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mUserSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        mUserSlidingMenu.setTouchmodeMarginThreshold(10);
        mUserSlidingMenu.setMode(SlidingMenu.RIGHT);
        mUserSlidingMenu.setBehindWidthRes(R.dimen.user_menu_sliding_width);

        mUserListFragment = (UserListFragment) manager.findFragmentById(R.id.userlist_fragment);

        mUserSlidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
            @Override
            public void onOpen() {
                mUserListFragment.onMenuOpened(getServer().getUserChannelInterface()
                        .getChannelIfExists(mIRCPagerFragment.getCurrentTitle()));
                onUserListDisplayed();
            }
        });
        mUserSlidingMenu.setOnCloseListener(new SlidingMenu.OnCloseListener() {
            @Override
            public void onClose() {
                getSupportActionBar().setSubtitle(MiscUtils.getStatusString(IRCActivity.this,
                        getServer().getStatus()));
                mUserListFragment.onClose();
            }
        });

        setUpActionsFragment();
    }

    // This is different for tablets and phones so get subclasses to do the work
    protected abstract void setUpActionsFragment();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call this regardless of whether this is a resumption or not - the correct checks to
        // whether to bind to the service will be done in the ServiceFragment
        mServiceFragment.connectToServer(this, mServerTitle);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getServer().getServerCache().setIrcTitle(mIRCPagerFragment.getCurrentTitle());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onDestroy() {
        Crouton.clearCroutonsForActivity(this);
        getServer().getServerEventBus().unregister(mEventReceiver);

        super.onDestroy();
    }

    // Options Menu stuff
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server_channel_ab, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final MenuItem userMenu = menu.findItem(R.id.activity_server_channel_ab_users);
        final boolean isChannel = FragmentType.Channel == mIRCPagerFragment.getCurrentType();
        userMenu.setVisible(isChannel && mUserSlidingMenu != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mDrawerToggle == null || !mDrawerToggle.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {
                case R.id.activity_server_channel_ab_users:
                    mUserSlidingMenu.toggle();
                    return true;
                default:
                    return false;
            }
        } else {
            return true;
        }
    }
    // Options menu end

    private void onUserListDisplayed() {
        getSupportActionBar().setSubtitle(mUserListFragment.getRealAdapter().getCount() + " users");
    }

    @Override
    public void onServerAvailable(final Server server) {
        final ServerEventBus bus = server.getServerEventBus();
        bus.register(mEventReceiver);
        bus.register(mIRCPagerFragment);
        server.setIgnoreList(MiscUtils.getIgnoreList(getApplicationContext(), mServerTitle));
    }

    @Override
    public void onSetupViewPager() {
        mIRCPagerFragment.onCreateServerFragment(mServerTitle);
        getSupportActionBar().setSubtitle(MiscUtils.getStatusString(this, getServer().getStatus()));

        if (isConnectedToServer()) {
            String tabTitle = getIntent().getStringExtra("mention");
            if (tabTitle == null) {
                tabTitle = getServer().getServerCache().getIrcTitle();
            }
            for (final Channel channel : getServer().getUser().getChannels()) {
                final boolean switchToTab = tabTitle.equals(channel.getName());
                mIRCPagerFragment.onCreateChannelFragment(channel.getName(), switchToTab);
            }
            final Collection<PrivateMessageUser> privateMessages = getServer()
                    .getUserChannelInterface().getPrivateMessageUsers();
            for (final PrivateMessageUser user : privateMessages) {
                final boolean switchToTab = tabTitle.equals(user.getNick());
                mIRCPagerFragment.onCreateMessageFragment(user.getNick(), switchToTab);
            }
            // Do this so that the options menu can pick up whether to display the user button
            // or not
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public Server getServer() {
        return mServiceFragment.getServer();
    }

    @Override
    public void closeAllSlidingMenus() {
        if (mActionsSlidingMenu != null) {
            mActionsSlidingMenu.showContent();
        }
        mUserSlidingMenu.showContent();
    }

    @Override
    public void disconnectFromServer() {
        mServiceFragment.disconnectFromServer();
    }

    @Override
    public boolean isUserSlidingMenuOpen() {
        return mUserSlidingMenu.isMenuShowing();
    }

    @Override
    public boolean isConnectedToServer() {
        final Server server = getServer();
        return server != null && server.getStatus() == ServerStatus.CONNECTED;
    }

    @Override
    public void onUserMention(final List<WorldUser> users) {
        mIRCPagerFragment.onMentionRequested(users);
        closeAllSlidingMenus();
    }

    @Override
    public String getNick() {
        return getServer().getUser().getNick();
    }

    @Override
    public void onRemoveCurrentFragment() {
        final Server server = getServer();
        if (FragmentType.User.equals(mIRCPagerFragment.getCurrentType())) {
            mIRCPagerFragment.onRemoveFragment(mIRCPagerFragment.getCurrentTitle());

            final PrivateMessageUser user = server.getUserChannelInterface()
                    .getPrivateMessageUserIfExists(mIRCPagerFragment.getCurrentTitle());
            server.getServerCallBus().sendClosePrivateMessage(user);
        } else {
            server.getServerCallBus().sendPart(mIRCPagerFragment.getCurrentTitle());
        }
    }

    // Getters and setters
    @Override
    public String getServerTitle() {
        return mServerTitle;
    }
}
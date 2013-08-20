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
import com.fusionx.irc.ChannelUser;
import com.fusionx.irc.PrivateMessageUser;
import com.fusionx.irc.Server;
import com.fusionx.irc.ServerConfiguration;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ServerChannelEventType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.IRCActionsFragment;
import com.fusionx.lightirc.fragments.UserListFragment;
import com.fusionx.lightirc.fragments.ircfragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ircfragments.IRCFragment;
import com.fusionx.lightirc.fragments.ircfragments.ServerFragment;
import com.fusionx.lightirc.fragments.ircfragments.UserFragment;
import com.fusionx.lightirc.handlerabstract.ChannelFragmentHandler;
import com.fusionx.lightirc.handlerabstract.PMFragmentHandler;
import com.fusionx.lightirc.handlerabstract.ServerChannelHandler;
import com.fusionx.lightirc.handlerabstract.ServerFragHandler;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.ui.ActionsSlidingMenu;
import com.fusionx.lightirc.ui.IRCViewPager;
import com.fusionx.uiircinterface.IRCBridgeService;
import com.fusionx.uiircinterface.MessageSender;
import com.fusionx.uiircinterface.ServerCommandSender;
import com.fusionx.uiircinterface.interfaces.FragmentSideHandlerInterface;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Activity which contains all the communication code between the fragments
 * It also implements a lot of callbacks to stop exposing objects to the fragments
 *
 * @author Lalit Maganti
 */
public class IRCFragmentActivity extends FragmentActivity implements
        UserListFragment.UserListCallback, ChannelFragment.ChannelFragmentCallback,
        IRCActionsFragment.IRCActionsCallback, ServerFragment.ServerFragmentCallback,
        FragmentSideHandlerInterface {

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

        MessageSender.getSender(mServerTitle).registerServerChannelHandler
                (IRCFragmentActivity.this);

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
                        createChannelFragment(channelName.getName());
                    }
                    final Iterator<PrivateMessageUser> iterator = getServer(false).getUser()
                            .getPrivateMessageIterator();
                    while (iterator.hasNext()) {
                        createPMFragment(iterator.next().getNick());
                    }
                }
            } else {
                mService.connectToServer(builder);
            }
        }

        // Should not occur
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService.disconnectFromServer(mServerTitle);
            mService = null;
        }
    };

    @Override
    public void onStop() {
        super.onStop();

        MessageSender.getSender(mServerTitle).unregisterFragmentSideHandlerInterface();
        if (mService != null) {
            mService.setServerDisplayed(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mConnection);
        mService = null;
    }

    private final ServerChannelHandler mServerChannelHandler = new ServerChannelHandler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ServerChannelEventType type = (ServerChannelEventType)
                    bundle.getSerializable(EventBundleKeys.eventType);
            final String message = bundle.getString(EventBundleKeys.message);
            switch (type) {
                case Join:
                    createChannelFragment(message, true);
                    break;
                case NewPrivateMessage:
                    createPMFragment(message);
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
                    mUserFragment.onMenuOpened(getCurrentItem().getTitle());
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

    /**
     * Get the name of the currently displayed server
     *
     * @return - the current server title
     */
    @Override
    public String getServerTitle() {
        return mServerTitle;
    }

    /**
     * Method called when a new UserFragment is to be created
     *
     * @param userNick - the nick of the user the PM is to
     */
    @Override
    public void createPMFragment(final String userNick) {
        mViewPager.onNewPrivateMessage(userNick);
    }

    /**
     * Close all SlidingMenus (if open)
     */
    @Override
    public void closeAllSlidingMenus() {
        mActionsSlidingMenu.showContent();
        mUserSlidingMenu.showContent();
    }

    /**
     * Checks if the app is connected to the server
     *
     * @return whether the app is connected to the server
     */
    @Override
    public boolean isConnectedToServer() {
        final Server server = getServer(true);

        return server != null && server.getStatus()
                .equals(getString(R.string.status_connected));
    }

    /**
     * Selects the ServerFragment regardless of what is currently selected
     */
    @Override
    public void selectServerFragment() {
        mViewPager.setCurrentItem(0, true);
    }

    /**
     * If the currently displayed fragment is the one being removed then switch
     * to one tab back. Then remove the fragment regardless.
     *
     * @param fragmentTitle - name of the fragment to be removed
     */
    @Override
    public void switchFragmentAndRemove(final String fragmentTitle) {
        final int index = mViewPager.getAdapter().getIndexFromTitle(fragmentTitle);
        if (getCurrentItem().getTitle().equals(fragmentTitle)) {
            mViewPager.setCurrentItem(index - 1, true);
        }
        mViewPager.getAdapter().removeFragment(index);
    }

    /**
     * Called when a user list update occurs
     *
     * @param channelName - name of channel which was updated
     */
    @Override
    public void updateUserList(final String channelName) {
        if (getCurrentItem().getTitle().equals(channelName)) {
            mUserFragment.updateUserList();
        }
    }

    /**
     * Create a ChannelFragment with the specified name
     *
     * @param channelName - name of the channel to create
     */
    private void createChannelFragment(final String channelName) {
        createChannelFragment(channelName, false);
    }

    /**
     * Method called when the server disconnects unexpectedly - this could be due to
     * loss of connection or for some reason the server has kicked us out
     */
    @Override
    public void onUnexpectedDisconnect() {
        closeAllSlidingMenus();
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

                @Override
                protected void onPostExecute(Void aVoid) {
                    mActionsFragment.updateConnectionStatus();
                }
            };
            unexpectedDisconnect.execute();
        }
    }

    // UserListFragment Listener Callbacks

    /**
     * Method which is called when the user requests a mention from
     * the UserListFragment
     *
     * @param users - the list of users which the app user wants to mentuin
     */
    @Override
    public void onUserMention(final ArrayList<ChannelUser> users) {
        final ChannelFragment channel = (ChannelFragment) getCurrentItem();
        channel.onUserMention(users);

        closeAllSlidingMenus();
    }

    // IRCActionsFragment Listener Callbacks

    /**
     * Method which returns the nick of the user
     *
     * @return - the nick of the user
     */
    @Override
    public String getNick() {
        return getServer(false).getUser().getNick();
    }

    /**
     * Called when a disconnect is requested by the user
     */
    @Override
    public void disconnect() {
        if (mService != null) {
            mService.disconnectFromServer(mServerTitle);
        }

        final Intent intent = new Intent(this, MainServerListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Close the currently displayed PM or part the currently displayed channel
     */
    @Override
    public void closeOrPartCurrentTab() {
        final Server server = getServer(false);
        if (getCurrentlyDisplayedFragment().equals(FragmentType.User)) {
            ServerCommandSender.sendClosePrivateMessage(server, getCurrentItem().getTitle());

            switchFragmentAndRemove(getCurrentItem().getTitle());
        } else {
            ServerCommandSender.sendPart(server, getCurrentItem().getTitle(),
                    getApplicationContext());
        }
    }

    /**
     * Returns the type of the currently displayed fragment in the ViewPager
     *
     * @return - the type of fragment
     */
    @Override
    public FragmentType getCurrentlyDisplayedFragment() {
        return getCurrentItem().getType();
    }

    /**
     * Start of the ServerFragment callbacks
     */
    /**
     * Method called when a new ChannelFragment is to be created
     *
     * @param channelName - name of the channel joined
     * @param forceSwitch - whether the channel should be forcibly switched to
     */
    public void createChannelFragment(final String channelName, final boolean forceSwitch) {
        final boolean switchToTab = channelName.equals(getIntent().getStringExtra("mention"))
                || forceSwitch;
        mViewPager.onNewChannelJoined(channelName, switchToTab);
    }

    /**
     * Method called when the server reports that it has been connected to
     */
    @Override
    public void connectedToServer() {
        if (mActionsSlidingMenu.isMenuShowing()) {
            mActionsFragment.updateConnectionStatus();
        }
    }

    @Override
    public ServerChannelHandler getServerChannelHandler() {
        return mServerChannelHandler;
    }

    @Override
    public ServerFragHandler getServerFragmentHandler() {
        final IRCFragment fragment = mViewPager.getAdapter().getFragment(mServerTitle,
                FragmentType.Server);
        return fragment == null ? null : ((ServerFragment) fragment).getServerFragHandler();
    }

    @Override
    public ChannelFragmentHandler getChannelFragmentHandler(String channelName) {
        final IRCFragment fragment = mViewPager.getAdapter().getFragment(channelName,
                FragmentType.Channel);
        return fragment == null ? null : ((ChannelFragment) fragment).getChannelFragmentHandler();
    }

    @Override
    public PMFragmentHandler getUserFragmentHandler(String userNick) {
        final IRCFragment fragment = mViewPager.getAdapter().getFragment(userNick,
                FragmentType.User);
        return fragment == null ? null : ((UserFragment) fragment).getUserFragmnetHandler();
    }

    /**
     * Listener used when the view pages changes pages
     */
    private class ViewPagerOnPagerListener extends ViewPager.SimpleOnPageChangeListener {
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
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
import android.view.Menu;
import android.view.MenuItem;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.IRCActionsFragment;
import com.fusionx.lightirc.fragments.UserListFragment;
import com.fusionx.lightirc.fragments.ircfragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ircfragments.IRCFragment;
import com.fusionx.lightirc.fragments.ircfragments.PMFragment;
import com.fusionx.lightirc.fragments.ircfragments.ServerFragment;
import com.fusionx.lightirc.interfaces.CommonIRCListenerInterface;
import com.fusionx.lightirc.listeners.ActivityListener;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.parser.MessageParser;
import com.fusionx.lightirc.parser.ServerCommunicator;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.ui.ActionsSlidingMenu;
import com.fusionx.lightirc.ui.IRCViewPager;
import com.fusionx.lightirc.ui.UserListSlidingMenu;
import com.fusionx.lightlibrary.activities.AbstractPagerActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

import java.util.ArrayList;
import java.util.Set;

public class IRCFragmentActivity extends AbstractPagerActivity
        implements ActivityListener.ActivityListenerInterface, UserListFragment.UserListListenerInterface,
        CommonIRCListenerInterface, ChannelFragment.ChannelFragmentListenerInterface,
        IRCActionsFragment.IRCActionsListenerInterface, ServerFragment.ServerFragmentListenerInterface,
        PMFragment.PMFragmentListenerInterface {

    private IRCActionsFragment mActionsFragment = null;
    private ActivityListener mListener = null;
    private IRCService mService = null;
    private IRCViewPager mViewPager = null;
    private String mServerTitle = null;
    private UserListSlidingMenu mUserSlidingMenu = null;
    private ActionsSlidingMenu mActionsSlidingMenu = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getThemeInt(getApplicationContext()));

        setContentView(R.layout.activity_server_channel);

        setUpSlidingMenu();

        mActionsFragment = (IRCActionsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.actions_fragment);

        final Configuration.Builder<PircBotX> builder = getIntent().getExtras().getParcelable("server");
        mServerTitle = builder != null ? builder.getTitle() : null;

        mViewPager = (IRCViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new IRCPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(this);

        mListener = new ActivityListener(this);

        final ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setUpService();
    }

    private void setUpSlidingMenu() {
        mUserSlidingMenu = new UserListSlidingMenu(this);
        mUserSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

        mActionsSlidingMenu = new ActionsSlidingMenu(this);
        mActionsSlidingMenu.setOnOpenListener(mActionsFragment);
        mActionsSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
    }

    private void setUpService() {
        final Intent service = new Intent(this, IRCService.class);
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

        if (mService != null && getBot() != null) {
            getBot().getConfiguration().getListenerManager().removeListener(mListener);
            mService.setServerDisplayed(null);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();

        if (mService != null && getBot() != null) {
            getBot().getConfiguration().getListenerManager().addListener(mListener);
            mService.setServerDisplayed(mServerTitle);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mConnection);
            mService = null;
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCService.IRCBinder) binder).getService();
            if (getBot() != null) {
                mActionsFragment.connectionStatusChanged(isConnectedToServer());

                getBot().getConfiguration().getListenerManager().addListener(mListener);
                addServerFragment();

                if (isConnectedToServer()) {
                    for (final Channel channelName : getBot().getUserBot().getChannels()) {
                        onCreateChannelFragment(channelName.getName());
                    }
                    for (final User user : getBot().getUserChannelDao().getPrivateMessages()) {
                        onCreatePMFragment(user.getNick());
                    }
                }
            } else {
                final Configuration.Builder<PircBotX> builder = getIntent().getExtras().getParcelable("server");
                if(builder != null) {
                    builder.getListenerManager().addListener(mListener);
                }
                mService.connectToServer(builder);
                addServerFragment();
            }
        }

        private void addServerFragment() {
            mViewPager.addServerFragment(mServerTitle);
            addTab(mServerTitle);
        }

        // Should not occur
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService = null;
        }
    };

    // Page change stuff
    @Override
    public void onPageSelected(final int position) {
        invalidateOptionsMenu();
        closeAllSlidingMenus();

        mViewPager.getAdapter().setCurrentItemIndex(position);

        super.onPageSelected(position);
    }

    @Override
    public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition(), true);
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
        if (!(fragment instanceof ServerFragment) && isConnectedToServer()) {
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
                ServerCommunicator.sendPart(getBot(), getCurrentItem().getTitle(), getApplicationContext());
                return true;
            case R.id.activity_server_channel_ab_users:
                if (!mUserSlidingMenu.isMenuShowing()) {
                    final UserListFragment mUserFragment = (UserListFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.user_fragment);
                    mUserFragment.userListUpdate(getCurrentItem().getTitle());
                    mUserFragment.getListView().smoothScrollToPosition(0);
                }
                mUserSlidingMenu.toggle();
                return true;
            case R.id.activity_server_channel_ab_close:
                ServerCommunicator.sendClosePrivateMessage(getUser(getCurrentItem().getTitle()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    public void onCreatePMFragment(final String userNick) {
        addTab(userNick);

        final int position = mViewPager.onNewPrivateMessage(userNick);

        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.getTabAt(position).setText(userNick);
        }
    }

    @Override
    public void closeAllSlidingMenus() {
        mActionsSlidingMenu.showContent();
        mUserSlidingMenu.showContent();
    }

    @Override
    public boolean isConnectedToServer() {
        return mService != null && getBot() != null
                && getBot().getStatus().equals(getString(R.string.status_connected));
    }

    // ActivityListener Callbacks
    @Override
    public boolean isFragmentSelected(final String title) {
        return getCurrentItem().getTitle().equals(title);
    }

    @Override
    public IRCFragment isFragmentAvailable(final String title) {
        return mViewPager.getAdapter().getFragment(title);
    }

    @Override
    public void selectServerFragment() {
        mViewPager.setCurrentItem(0, true);
    }

    @Override
    public void switchFragmentAndRemove(final String channelName) {
        final int index = mViewPager.getAdapter().getIndexFromTitle(channelName);
        if(getCurrentItem().getTitle().equals(channelName)) {
            mViewPager.setCurrentItem(index - 1, true);
        }
        removeTab(index);
        mViewPager.getAdapter().removeFragment(index);
    }

    @Override
    public void onConnect() {
        mActionsFragment.connectionStatusChanged(true);
    }

    private void onCreateChannelFragment(final String channelName) {
        onNewChannelJoined(channelName, false);
    }

    @Override
    public void onNewChannelJoined(final String channelName, final boolean forceSwitch) {
        addTab(channelName);

        final boolean switchToTab = channelName.equals(getIntent().getExtras().getString("mention", ""))
                || forceSwitch;
        final int position = mViewPager.onNewChannelJoined(channelName, switchToTab);

        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.getTabAt(position).setText(channelName);
        }
    }

    @Override
    public void onUnexpectedDisconnect() {
        closeAllSlidingMenus();
        selectServerFragment();

        final ActionBar bar = getActionBar();
        if(bar != null) {
            for (int i = 1; i < bar.getTabCount(); ) {
                removeTab(i);
            }
        }
        mViewPager.disconnect();
        mActionsFragment.connectionStatusChanged(false);

        final AsyncTask<Void, Void, Void> unexpectedDisconnect = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getBot().getConfiguration().getListenerManager().removeListener(mListener);
                mService.setServerDisplayed(null);
                mService.onUnexpectedDisconnect(mServerTitle);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                unbindService(mConnection);
                mService = null;
            }
        };
        unexpectedDisconnect.execute();
    }

    // UserListFragment Listener Callbacks
    @Override
    public void onUserMention(final Set<String> users) {
        final ChannelFragment channel = (ChannelFragment) getCurrentItem();
        channel.onUserMention(users);

        closeAllSlidingMenus();
    }

    @Override
    public boolean isNickOtherUsers(final String nick) {
        return !getBot().getNick().equals(nick);
    }

    @Override
    public ArrayList<String> getUserList(final String channelName) {
        return getBot().getUserChannelDao().getChannel(channelName).getUserList();
    }

    // ChannelFragment Listener Callbacks
    @Override
    public Channel getChannel(final String channelName) {
        return getBot().getUserChannelDao().getChannel(channelName);
    }

    @Override
    public void sendChannelMessage(final String channelName, final String message) {
        MessageParser.channelMessageToParse(getApplicationContext(), getBot(), channelName, message);
    }

    // ActionsFragment Listener Callbacks
    @Override
    public String getNick() {
        return getBot().getNick();
    }

    @Override
    public void joinChannel(final String channel) {
        ServerCommunicator.sendJoin(getBot(), channel);
    }

    @Override
    public void changeNick(String newNick) {
        ServerCommunicator.sendNickChange(getBot(), newNick);
    }

    @Override
    public void disconnect() {
        if (mService != null && getBot() != null) {
            getBot().getConfiguration().getListenerManager().removeListener(mListener);

            mService.disconnectFromServer(mServerTitle);
            unbindService(mConnection);
            mService = null;
        }

        final Intent intent = new Intent(this, MainServerListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // PMFragment Listener Callbacks
    @Override
    public User getUser(final String userNick) {
        return getBot().getUserChannelDao().getUser(userNick);
    }

    @Override
    public void sendUserMessage(final String nick, final String message) {
        MessageParser.userMessageToParse(getBot(), nick, message);
    }

    // ServerFragment Listener Callbacks
    @Override
    public PircBotX getBot() {
        if (mService != null) {
            return mService.getBot(mServerTitle);
        } else {
            return null;
        }
    }

    @Override
    public void sendServerMessage(final String message) {
        MessageParser.serverMessageToParse(getBot(), message);
    }
}
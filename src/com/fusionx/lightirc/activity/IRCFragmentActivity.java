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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.fusionx.lightirc.fragments.IRCActionsFragment;
import com.fusionx.lightirc.fragments.PagerFragment;
import com.fusionx.lightirc.fragments.ServiceFragment;
import com.fusionx.lightirc.fragments.UserListFragment;
import com.fusionx.lightirc.fragments.ircfragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ircfragments.IRCFragment;
import com.fusionx.lightirc.fragments.ircfragments.ServerFragment;
import com.fusionx.lightirc.handlerabstract.ChannelFragmentHandler;
import com.fusionx.lightirc.handlerabstract.PMFragmentHandler;
import com.fusionx.lightirc.handlerabstract.ServerChannelHandler;
import com.fusionx.lightirc.handlerabstract.ServerFragHandler;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.ui.ActionsSlidingMenu;
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
        FragmentSideHandlerInterface, ServiceFragment.ServiceFragmentCallback {

    private ServiceFragment mServiceFragment = null;
    private UserListFragment mUserFragment = null;
    private PagerFragment mPagerFragment = null;
    private String mServerTitle = null;
    private SlidingMenu mUserSlidingMenu = null;
    private ActionsSlidingMenu mActionsSlidingMenu = null;
    private final ViewPagerOnPagerListener listener = new ViewPagerOnPagerListener();
    private IRCActionsFragment mActionsFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ServerConfiguration.Builder builder = getIntent().getParcelableExtra("server");
        mServerTitle = builder != null ? builder.getTitle() : getIntent().getStringExtra
                ("serverTitle");

        setTheme(Utils.getThemeInt(getApplicationContext()));
        setContentView(R.layout.activity_server_channel);

        setUpSlidingMenu();

        final FragmentManager fm = getSupportFragmentManager();
        mServiceFragment = (ServiceFragment) fm.findFragmentByTag("service");

        if (mServiceFragment == null) {
            mServiceFragment = new ServiceFragment();
            fm.beginTransaction().add(mServiceFragment, "service").commit();
        } else {
            setUpViewPager();
        }

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mServerTitle);
        }
    }

    private void setUpSlidingMenu() {
        mUserSlidingMenu = (SlidingMenu) findViewById(R.id.slidingmenulayout);
        mUserSlidingMenu.setContent(R.layout.view_pager_fragment);
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

    @Override
    public void setUpViewPager() {
        mPagerFragment = (PagerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.pager_fragment);

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mPagerFragment.getViewPager());
        tabs.setOnPageChangeListener(listener);
        tabs.setBackgroundResource(R.color.sliding_menu_background);
        tabs.setTextColorResource(android.R.color.white);
        tabs.setIndicatorColorResource(android.R.color.white);
        mPagerFragment.getPagerAdapter().setTabStrip(tabs);
    }

    @Override
    public void repopulateFragmentsInPager() {
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
        return mPagerFragment.getCurrentItem();
    }

    /*
     * CALLBACKS START HERE
     */
    // CommonIRCListener Callbacks
    @Override
    public Server getServer(final boolean nullAllowed) {
        return mServiceFragment.getServer(nullAllowed);
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
        mPagerFragment.createPMFragment(userNick);
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
        return server != null && server.getStatus().equals(getString(R.string.status_connected));
    }

    /**
     * Selects the ServerFragment regardless of what is currently selected
     */
    @Override
    public void selectServerFragment() {
        mPagerFragment.selectServerFragment();
    }

    /**
     * If the currently displayed fragment is the one being removed then switch
     * to one tab back. Then remove the fragment regardless.
     *
     * @param fragmentTitle - name of the fragment to be removed
     */
    @Override
    public void switchFragmentAndRemove(final String fragmentTitle) {
        mPagerFragment.switchFragmentAndRemove(fragmentTitle);
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
        mPagerFragment.getViewPager().disconnect();

        if (getServer(true) != null) {
            final AsyncTask<Void, Void, Void> unexpectedDisconnect = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    mServiceFragment.removeServiceReference();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    mActionsFragment.updateConnectionStatus();
                }
            };
            unexpectedDisconnect.execute();
        } else {
            mActionsFragment.updateConnectionStatus();
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
        mServiceFragment.disconnect();
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
        mPagerFragment.createChannelFragment(channelName, forceSwitch);
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
        return mPagerFragment.getServerFragmentHandler();
    }

    @Override
    public ChannelFragmentHandler getChannelFragmentHandler(final String channelName) {
        return mPagerFragment.getChannelFragmentHandler(channelName);
    }

    @Override
    public PMFragmentHandler getUserFragmentHandler(final String userNick) {
        return mPagerFragment.getUserFragmentHandler(userNick);
    }

    public void mention(final String destination) {
        final String message = String.format(getString(R.string.activity_mentioned), destination);
        final Toast toast = new Toast(this);
        final View view = getLayoutInflater().inflate(R.layout.toast_mention,
                (ViewGroup) findViewById(R.id.toast_layout_root));
        final TextView textView = (TextView) view.findViewById(R.id.toast_text);
        textView.setText(message);
        toast.setView(view);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
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

            mPagerFragment.getPagerAdapter().setCurrentItemIndex(position);
        }
    }
}
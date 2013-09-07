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

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.irc.event.ConnectedEvent;
import com.fusionx.lightirc.irc.event.FinalDisconnectEvent;
import com.fusionx.lightirc.irc.event.JoinEvent;
import com.fusionx.lightirc.irc.event.MentionEvent;
import com.fusionx.lightirc.irc.event.PartEvent;
import com.fusionx.lightirc.irc.event.PrivateMessageEvent;
import com.fusionx.lightirc.irc.event.RetryPendingDisconnectEvent;
import com.fusionx.lightirc.irc.event.SwitchToServerEvent;
import com.fusionx.lightirc.ui.widget.ActionsSlidingMenu;
import com.fusionx.lightirc.ui.widget.DecorChildLayout;
import com.fusionx.lightirc.uiircinterface.ServerCommandSender;
import com.fusionx.lightirc.util.UIUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Activity which contains all the communication code between the fragments
 * It also implements a lot of callbacks to stop exposing objects to the fragments
 *
 * @author Lalit Maganti
 */
public class IRCActivity extends ActionBarActivity implements UserListFragment.UserListCallback,
        ServiceFragment.ServiceFragmentCallback, ActionsPagerFragment
                .ActionsPagerFragmentCallback, IRCPagerFragment.IRCPagerInterface {

    private ServiceFragment mServiceFragment = null;
    private UserListFragment mUserListFragment = null;
    private IRCPagerFragment mIRCPagerFragment = null;
    private ActionsPagerFragment mActionsPagerFragment = null;

    // Sliding menus
    private SlidingMenu mUserSlidingMenu = null;
    private ActionsSlidingMenu mActionsSlidingMenu = null;

    // Mention things
    private final Handler mMentionHandler = new Handler();
    private View mMentionView;

    private String mServerTitle = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt(this));

        super.onCreate(savedInstanceState);

        final ServerConfiguration.Builder builder = getIntent().getParcelableExtra("server");
        mServerTitle = builder != null ? builder.getTitle() : getIntent().getStringExtra
                ("serverTitle");

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

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mServerTitle);
        }

        // Get Window Decor View
        final ViewGroup decorView = (ViewGroup) getWindow().getDecorView();

        // Create Header view and then add to Decor View
        mMentionView = LayoutInflater.from(this.getSupportActionBar().getThemedContext()).inflate(R
                .layout.toast_mention, decorView, false);
        mMentionView.setVisibility(View.GONE);

        // Create DecorChildLayout which will move all of the system's decor
        // view's children + the  Header View to itself. See DecorChildLayout for more info.
        final DecorChildLayout decorContents = new DecorChildLayout(this, decorView, mMentionView);

        // Now add the DecorChildLayout to the decor view
        decorView.addView(decorContents, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void setUpSlidingMenu() {
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (!tabletSize) {
            mUserSlidingMenu = (SlidingMenu) findViewById(R.id.slidingmenulayout);
            mUserSlidingMenu.setContent(R.layout.view_pager_fragment);
            mUserSlidingMenu.setMenu(R.layout.sliding_menu_fragment_userlist);
            mUserSlidingMenu.setShadowDrawable(R.drawable.shadow);
            mUserSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            mUserSlidingMenu.setTouchmodeMarginThreshold(10);
            mUserSlidingMenu.setMode(SlidingMenu.RIGHT);
            mUserSlidingMenu.setBehindWidthRes(R.dimen.server_channel_sliding_actions_menu_width);
        }

        mUserListFragment = (UserListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userlist_fragment);

        if (tabletSize) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(mUserListFragment);
            ft.commit();
        } else {
            mUserSlidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
                @Override
                public void onOpen() {
                    mUserListFragment.onMenuOpened(mIRCPagerFragment.getCurrentTitle());
                }
            });
            mUserSlidingMenu.setOnCloseListener(mUserListFragment);
        }

        mActionsSlidingMenu = new ActionsSlidingMenu(this);

        mActionsPagerFragment = (ActionsPagerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.actions_fragment);

        mActionsSlidingMenu.setOnOpenListener(mActionsPagerFragment.getActionFragmentListener());

        if (UIUtils.hasHoneycomb()) {
            mActionsSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        } else {
            // get the window background
            final TypedArray a = getTheme().obtainStyledAttributes(new int[]{android.R.attr
                    .windowBackground});
            int background = a.getResourceId(0, 0);
            a.recycle();

            // take the above view out of
            final ViewGroup contentParent = (ViewGroup) ((ViewGroup) findViewById(android.R.id
                    .content)).getChildAt(0);
            View content = contentParent.getChildAt(1);
            contentParent.removeView(content);
            contentParent.addView(mActionsSlidingMenu);
            mActionsSlidingMenu.setContent(content);
            if (content.getBackground() == null) {
                content.setBackgroundResource(background);
            }
        }
        mActionsSlidingMenu.setOnCloseListener(mActionsPagerFragment.getIgnoreFragmentListener());
    }

    @Override
    public void setUpViewPager() {
        mIRCPagerFragment = (IRCPagerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.pager_fragment);
        mIRCPagerFragment.createServerFragment(mServerTitle);

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mIRCPagerFragment.setTabStrip(tabs);
        tabs.setOnPageChangeListener(mListener);
        tabs.setBackgroundResource(R.color.sliding_menu_background);
        tabs.setTextColorResource(android.R.color.white);
        tabs.setIndicatorColorResource(android.R.color.white);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServiceFragment.getSender().setDisplayed(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mServiceFragment.getSender().setDisplayed(true);
    }

    @Subscribe
    public void onRetryPendingDisconnect(final RetryPendingDisconnectEvent event) {
        mIRCPagerFragment.notifyDataSetChanged(mServerTitle);
        onDisconnect(false, true);
    }

    @Subscribe
    public void onFinalDisconnect(final FinalDisconnectEvent event) {
        mIRCPagerFragment.notifyDataSetChanged(mServerTitle);
        onDisconnect(event.disconnectExpected, false);
    }

    @Subscribe
    public void onChannelJoin(final JoinEvent event) {
        mIRCPagerFragment.createChannelFragment(event.channelToJoin, true);
    }

    @Subscribe
    public void onChannelPart(final PartEvent event) {
        mIRCPagerFragment.switchFragmentAndRemove(event.channelName);
    }

    @Subscribe
    public void onNewPrivateMessage(final PrivateMessageEvent event) {
        createPMFragment(event.nick);
    }

    @Subscribe
    public void onSwitchToServer(final SwitchToServerEvent event) {
        mIRCPagerFragment.notifyDataSetChanged(mServerTitle);

        mIRCPagerFragment.selectServerFragment();
    }

    /**
     * Method called when the server reports that it has been connected to
     */
    @Subscribe
    public void onServerConnected(final ConnectedEvent event) {
        mIRCPagerFragment.notifyDataSetChanged(mServerTitle);

        mIRCPagerFragment.connectedToServer();
        mActionsPagerFragment.updateConnectionStatus(true);
    }

    // Options Menu stuff
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server_channel_ab, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(FragmentTypeEnum.Channel
                .equals(mIRCPagerFragment.getCurrentType()) && mUserSlidingMenu != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActionsSlidingMenu.toggle();
                return true;
            case R.id.activity_server_channel_ab_users:
                mUserSlidingMenu.toggle();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void repopulateFragmentsInPager() {
        if (isConnectedToServer()) {
            for (final Channel channel : getServer(false).getUser().getChannels()) {
                createChannelFragment(channel.getName());
            }
            final Iterator<PrivateMessageUser> iterator = getServer(false).getUser()
                    .getPrivateMessageIterator();
            while (iterator.hasNext()) {
                createPMFragment(iterator.next().getNick());
            }
        }
    }

    /*
     * CALLBACKS START HERE
     */
    // CommonIRCListener Callbacks
    @Override
    public Server getServer(final boolean nullAllowed) {
        return mServiceFragment.getServer(nullAllowed, mServerTitle);
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
        mIRCPagerFragment.createPMFragment(userNick);
    }

    /**
     * Close all SlidingMenus (if open)
     */
    @Override
    public void closeAllSlidingMenus() {
        mActionsSlidingMenu.showContent();
        if (mUserSlidingMenu != null) {
            mUserSlidingMenu.showContent();
        }
    }

    /**
     * Checks if the app is connected to the server
     *
     * @return whether the app is connected to the server
     */
    @Override
    public boolean isConnectedToServer() {
        final Server server = getServer(true);
        return server != null && server.isConnected(this);
    }

    /**
     * Called when a user list update occurs
     *
     * @param channelName - name of channel which was updated
     */
    @Override
    public void onUserListChanged(final String channelName) {
        if (channelName.equals(mIRCPagerFragment.getCurrentTitle())) {
            mUserListFragment.onUserListUpdated();
        }
    }

    /**
     * Create a ChannelFragment with the specified name
     *
     * @param channelName - name of the channel to create
     */
    private void createChannelFragment(final String channelName) {
        mIRCPagerFragment.createChannelFragment(channelName, false);
    }

    /**
     * Method called when the server disconnects
     *
     * @param expected     - whether the disconnect was triggered by the user
     * @param retryPending - whether there is a reconnection attempt pending
     */
    @Override
    public void onDisconnect(final boolean expected, final boolean retryPending) {
        if (expected && !retryPending) {
            if (getServer(true) != null) {
                mServiceFragment.removeServiceReference(mServerTitle);
            }
            final Intent intent = new Intent(this, ServerListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (!expected && retryPending) {
            closeAllSlidingMenus();
            mIRCPagerFragment.onUnexpectedDisconnect();
            mActionsPagerFragment.updateConnectionStatus(isConnectedToServer());
        } else if (!expected) {
            closeAllSlidingMenus();
            mIRCPagerFragment.onUnexpectedDisconnect();

            if (getServer(true) != null) {
                mServiceFragment.removeServiceReference(mServerTitle);
            }
            mActionsPagerFragment.updateConnectionStatus(false);
        } else {
            throw new IllegalArgumentException();
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
        mIRCPagerFragment.onMentionRequested(users);

        closeAllSlidingMenus();
    }

    // ActionsFragment Listener Callbacks

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
     * Close the currently displayed PM or part the currently displayed channel
     */
    @Override
    public void closeOrPartCurrentTab() {
        final Server server = getServer(false);
        if (FragmentTypeEnum.User.equals(mIRCPagerFragment.getCurrentType())) {
            ServerCommandSender.sendClosePrivateMessage(server,
                    mIRCPagerFragment.getCurrentTitle());

            mIRCPagerFragment.switchFragmentAndRemove(mIRCPagerFragment.getCurrentTitle());
        } else {
            ServerCommandSender.sendPart(server, mIRCPagerFragment.getCurrentTitle(),
                    getApplicationContext());
        }
    }

    /**
     * Start of the ServerFragment callbacks
     */
    /**
     * Method called when the user nick is mentioned by another user
     *
     * @param event - the event which contains the destination
     */
    @Subscribe
    public void onMention(final MentionEvent event) {
        final String message = String.format(getString(R.string.activity_mentioned),
                event.destination);

        final TextView textView = (TextView) mMentionView.findViewById(R.id.toast_text);
        textView.setText(message);

        mMentionView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.action_bar_in));
        mMentionView.setVisibility(View.VISIBLE);

        mMentionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMentionView.startAnimation(AnimationUtils.loadAnimation
                        (IRCActivity.this, R.anim.action_bar_out));
                mMentionView.setVisibility(View.INVISIBLE);
            }
        }, 2500);
    }

    /**
     * Listener used when the view pages changes pages
     */
    final ViewPager.SimpleOnPageChangeListener mListener = new ViewPager
            .SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(final int position) {
            supportInvalidateOptionsMenu();
            closeAllSlidingMenus();

            mActionsPagerFragment.onPageChanged(mIRCPagerFragment.getCurrentType());

            mActionsSlidingMenu.setTouchModeAbove(position == 0 ? SlidingMenu
                    .TOUCHMODE_FULLSCREEN : SlidingMenu.TOUCHMODE_MARGIN);
            mUserSlidingMenu.setTouchModeAbove(position == 0 ? SlidingMenu
                    .TOUCHMODE_NONE : SlidingMenu.TOUCHMODE_MARGIN);

            mIRCPagerFragment.setCurrentItemIndex(position);
        }
    };
}
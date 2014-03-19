package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.StatusChangeEvent;
import com.fusionx.relay.interfaces.SubServerObject;
import com.squareup.otto.Subscribe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static butterknife.ButterKnife.findById;

public class MainActivity extends ActionBarActivity implements ServerListFragment.Callback,
        IRCFragment.Callback, SlidingPaneLayout.PanelSlideListener, DrawerLayout.DrawerListener,
        ActionsPagerFragment.Callback {

    // Constants
    public static final int SETTINGS_ACTIVITY = 0;

    // Fields
    // IRC
    private Server mServer;

    // Fragments
    private IRCFragment mCurrentFragment;

    private ActionsPagerFragment mActionsFragment;

    private ServerListFragment mServerListFragment;

    // Views
    private SlidingPaneLayout mSlidingPane;

    private DrawerLayout mDrawerLayout;

    private View mRightDrawer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppPreferences.setUpPreferences(this);
        SharedPreferencesUtils.onInitialSetup(this);

        setContentView(R.layout.new_main_activity);

        mSlidingPane = findById(this, R.id.sliding_pane_layout);
        mSlidingPane.setParallaxDistance(100);
        mSlidingPane.openPane();
        mSlidingPane.setPanelSlideListener(this);

        mDrawerLayout = findById(this, R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setDrawerListener(this);

        mRightDrawer = findById(this, R.id.right_drawer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            mServerListFragment = new ServerListFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_list_frame,
                    mServerListFragment).commit();

            mActionsFragment = new ActionsPagerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.right_drawer,
                    mActionsFragment).commit();
        } else {
            mServerListFragment = (ServerListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.sliding_list_frame);
            mActionsFragment = (ActionsPagerFragment) getSupportFragmentManager().findFragmentById(R
                    .id.right_drawer);

            mServer = mServerListFragment.onActivityRestored();
            if (mServer != null) {
                mServer.getServerEventBus().register(this);
            }

            mCurrentFragment = (IRCFragment) getSupportFragmentManager().findFragmentById(R.id
                    .content_frame);
            if (mCurrentFragment != null) {
                findById(this, R.id.content_frame_empty_textview).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mServer != null) {
            mServer.getServerEventBus().unregister(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.new_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final MenuItem item = menu.findItem(R.id.open_actions);
        item.setVisible(!mSlidingPane.isOpen() && mServer != null);

        final MenuItem addServer = menu.findItem(R.id.add_server);
        addServer.setVisible(mSlidingPane.isOpen());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.home:
                UIUtils.toggleSlidingPane(mSlidingPane);
                return true;
            case R.id.open_actions:
                UIUtils.toggleDrawerLayout(mDrawerLayout, mRightDrawer);
                return true;
            case R.id.add_server:
                addNewServer();
                return true;
        }
        return false;
    }

    private void addNewServer() {
        final Intent intent = new Intent(this, ServerPreferenceActivity.class);

        intent.putExtra("new", true);
        intent.putExtra("file", "server");
        startActivityForResult(intent, SETTINGS_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SETTINGS_ACTIVITY:
                    mServerListFragment.refreshServers();
                    break;
            }
        }
    }

    @Override
    public void onServerClicked(final Server server) {
        if (shouldReplaceFragment(server)) {
            final Bundle bundle = new Bundle();
            bundle.putString("title", server.getTitle());

            final IRCFragment fragment = new ServerFragment();
            fragment.setArguments(bundle);

            if (mServer != server) {
                if (mServer != null) {
                    mServer.getServerEventBus().unregister(this);
                }
                server.getServerEventBus().register(this);
            }

            mServer = server;
            onChangeCurrentFragment(fragment);

            setActionBarTitle(server.getTitle());
            setActionBarSubtitle(MiscUtils.getStatusString(this, server.getStatus()));
            mActionsFragment.onFragmentTypeChanged(fragment.getType());
        }
        mSlidingPane.closePane();
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onSubServerClicked(final SubServerObject object) {
        if (shouldReplaceFragment(object)) {
            final Bundle bundle = new Bundle();
            bundle.putString("title", object.getId());

            final IRCFragment fragment;
            // TODO - fix this awful code
            if (object instanceof Channel) {
                fragment = new ChannelFragment();
            } else {
                fragment = new UserFragment();
            }
            fragment.setArguments(bundle);

            if (mServer != object.getServer()) {
                if (mServer != null) {
                    mServer.getServerEventBus().unregister(this);
                }
                object.getServer().getServerEventBus().register(this);
            }

            mServer = object.getServer();

            onChangeCurrentFragment(fragment);

            setActionBarTitle(object.getId());
            setActionBarSubtitle(object.getServer().getTitle());
            mActionsFragment.onFragmentTypeChanged(fragment.getType());
        }
        mSlidingPane.closePane();
        supportInvalidateOptionsMenu();
    }

    private void onChangeCurrentFragment(final IRCFragment fragment) {
        mCurrentFragment = fragment;

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.content_frame, fragment).commit();

        findById(this, R.id.content_frame_empty_textview).setVisibility(View.GONE);
    }

    private void onRemoveFragment() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.remove(mCurrentFragment).commit();

        findById(this, R.id.content_frame_empty_textview).setVisibility(View.VISIBLE);
        setTitle(getString(R.string.app_name));
        setActionBarSubtitle(null);
        mCurrentFragment = null;
    }

    @Override
    public void onServerDisconnected(final Server server) {
        closeDrawer();
        supportInvalidateOptionsMenu();
        onRemoveFragment();
    }

    private boolean shouldReplaceFragment(final Server server) {
        return mCurrentFragment == null || !mServer.equals(server)
                || !mCurrentFragment.getType().equals(FragmentType.SERVER);
    }

    private boolean shouldReplaceFragment(final SubServerObject object) {
        return mCurrentFragment == null
                || !object.getServer().equals(mServer)
                || !mCurrentFragment.getTitle().equals(object.getId());
    }

    @Override
    public void onRemoveCurrentFragment() {
        // TODO
    }

    @Override
    public Server getServer() {
        return mServer;
    }

    @Override
    public void disconnectFromServer() {
        mServerListFragment.disconnectFromServer(mServer);
    }

    @Override
    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }

    public void setActionBarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

    public void setActionBarSubtitle(final String subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }

    // Subscribe events
    @Subscribe
    public void onStatusChanged(final StatusChangeEvent event) {
        // Null happens when the disconnect handler is called first & the fragment has already been
        // removed by the disconnect handler
        if (mCurrentFragment != null && mCurrentFragment.getType() == FragmentType.SERVER) {
            setActionBarSubtitle(MiscUtils.getStatusString(this, mServer.getStatus()));
            mActionsFragment.onConnectionStatusChanged(mServer.getStatus());
        }
    }

    @Subscribe
    public void onConnected(final ConnectEvent event) {
        mCurrentFragment.onResetUserInput();
    }

    @Override
    public void onPanelOpened(final View view) {
        supportInvalidateOptionsMenu();
        // TODO - write the opposing code in onPanelClosed so this is done
        //getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onPanelClosed(final View view) {
        supportInvalidateOptionsMenu();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPanelSlide(final View view, final float v) {
        // Empty
    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {
    }

    @Override
    public void onDrawerOpened(final View drawerView) {
    }

    @Override
    public void onDrawerClosed(final View drawerView) {
        mActionsFragment.onDrawerClosed();
    }

    @Override
    public void onDrawerStateChanged(final int newState) {
    }
}
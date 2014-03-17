package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.interfaces.SubServerObject;
import com.squareup.otto.Subscribe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import static butterknife.ButterKnife.findById;

public class MainActivity extends ActionBarActivity implements ServerListFragment.Callback,
        IRCFragment.Callback, SlidingPaneLayout.PanelSlideListener {

    private Server mServer;

    private IRCFragment mCurrentFragment;

    private DrawerLayout mDrawerLayout;

    private RelativeLayout mRightDrawer;

    private SlidingPaneLayout mSlidingPane;

    private ActionsFragment mActionsFragment;

    private ServerListFragment mServerListFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppPreferences.setUpPreferences(getApplicationContext());
        initialSetup();

        setContentView(R.layout.new_main_activity);

        mSlidingPane = findById(this, R.id.sliding_pane_layout);
        mSlidingPane.setParallaxDistance(100);
        mSlidingPane.openPane();
        mSlidingPane.setPanelSlideListener(this);
        //mSlidingPane.setSliderFadeColor(Color.parseColor("#E5E4E2"));

        mDrawerLayout = findById(this, R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mRightDrawer = findById(this, R.id.right_drawer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            mServerListFragment = new ServerListFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.sliding_list_frame,
                    mServerListFragment).commit();

            mActionsFragment = new ActionsFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.right_drawer,
                    mActionsFragment).commit();
        } else {
            mServerListFragment = (ServerListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.sliding_list_frame);
            mActionsFragment = (ActionsFragment) getSupportFragmentManager().findFragmentById(R
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
        item.setVisible(!mSlidingPane.isOpen());

        final MenuItem addServer = menu.findItem(R.id.add_server);
        addServer.setVisible(mSlidingPane.isOpen());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.home:
                if (mSlidingPane.isOpen()) {
                    mSlidingPane.closePane();
                } else {
                    mSlidingPane.openPane();
                }
                return true;
            case R.id.open_actions:
                if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                    mDrawerLayout.closeDrawer(mRightDrawer);
                } else {
                    mDrawerLayout.openDrawer(mRightDrawer);
                }
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
        startActivity(intent);
    }

    private void initialSetup() {
        final SharedPreferences globalSettings = getSharedPreferences("main", MODE_PRIVATE);
        final boolean firstRun = globalSettings.getBoolean("firstrun", true);

        if (firstRun) {
            SharedPreferencesUtils.firstTimeServerSetup(this);
            globalSettings.edit().putBoolean("firstrun", false).commit();
        }
    }

    @Override
    public void onServerClicked(final Server server) {
        if (mCurrentFragment == null || mServer != server || !mCurrentFragment.getType().equals
                (FragmentType.SERVER)) {
            if (mServer != null && mServer != server) {
                mServer.getServerEventBus().unregister(this);
            }

            final Bundle bundle = new Bundle();
            bundle.putString("title", server.getConfiguration().getTitle());

            final IRCFragment fragment = new ServerFragment();
            fragment.setArguments(bundle);

            if (mServer != server) {
                server.getServerEventBus().register(this);
            }

            mCurrentFragment = fragment;
            mServer = server;

            onChangeCurrentFragment(fragment);
        }

        mSlidingPane.closePane();
    }

    @Override
    public void onSubServerClicked(final SubServerObject object) {
        if (mCurrentFragment == null || !object.getServer().getConfiguration().getTitle().equals
                (mServer.getConfiguration().getTitle()) || !mCurrentFragment.getTitle().equals
                (object.getId())) {
            if (mServer != null && mServer != object.getServer()) {
                mServer.getServerEventBus().unregister(this);
            }

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
                object.getServer().getServerEventBus().register(this);
            }

            mCurrentFragment = fragment;
            mServer = object.getServer();

            onChangeCurrentFragment(fragment);
        }
        mSlidingPane.closePane();
    }

    private void onChangeCurrentFragment(final Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                fragment).commit();
        findById(this, R.id.content_frame_empty_textview).setVisibility(View.GONE);
    }

    @Override
    public void onServerDisconnected(Server server) {
    }

    @Override
    public Server getServer() {
        return mServer;
    }

    public void setActionBarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

    @Subscribe
    public void onConnected(final ConnectEvent event) {
        mCurrentFragment.onResetUserInput();
    }

    @Override
    public void onPanelOpened(final View view) {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPanelClosed(final View view) {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPanelSlide(final View view, final float v) {
        // Empty
    }
}
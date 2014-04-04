package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.server.StatusChangeEvent;
import com.fusionx.relay.interfaces.Conversation;
import com.squareup.otto.Subscribe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
        ActionsPagerFragment.Callback, WorkerFragment.Callback {

    public static final int SERVER_SETTINGS = 1;

    public static final String WORKER_FRAGMENT = "WorkerFragment";

    private static final String ACTION_BAR_TITLE = "action_bar_title";

    private static final String ACTION_BAR_SUBTITLE = "action_bar_subtitle";

    // Fields
    // IRC
    private Conversation mConversation;

    // Fragments
    private WorkerFragment mWorkerFragment;

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
        mSlidingPane.setPanelSlideListener(this);

        mDrawerLayout = findById(this, R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setDrawerListener(this);

        mRightDrawer = findById(this, R.id.right_drawer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            mServerListFragment = new ServerListFragment();
            transaction.replace(R.id.sliding_list_frame, mServerListFragment);

            mActionsFragment = new ActionsPagerFragment();
            transaction.add(R.id.right_drawer, mActionsFragment);

            mWorkerFragment = new WorkerFragment();
            transaction.add(mWorkerFragment, WORKER_FRAGMENT);

            transaction.commit();
        } else {
            mServerListFragment = (ServerListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.sliding_list_frame);
            mActionsFragment = (ActionsPagerFragment) getSupportFragmentManager().findFragmentById(R
                    .id.right_drawer);
            mWorkerFragment = (WorkerFragment) getSupportFragmentManager()
                    .findFragmentByTag(WORKER_FRAGMENT);

            mConversation = mWorkerFragment.getSavedConversation();
            if (mConversation != null) {
                mConversation.getServer().getServerEventBus().register(this);
            }

            mCurrentFragment = (IRCFragment) getSupportFragmentManager().findFragmentById(R.id
                    .content_frame);
            if (mCurrentFragment != null) {
                findById(this, R.id.content_frame_empty_textview).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the action bar title & sub-title
        if (outState != null) {
            outState.putString(ACTION_BAR_TITLE, getSupportActionBar().getTitle().toString());
            outState.putString(ACTION_BAR_SUBTITLE, getSupportActionBar().getSubtitle().toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the action bar title & sub-title
        if (savedInstanceState != null) {
            getSupportActionBar().setTitle(savedInstanceState.getString(ACTION_BAR_TITLE));
            getSupportActionBar().setSubtitle(savedInstanceState.getString(ACTION_BAR_SUBTITLE));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mConversation != null) {
            mWorkerFragment.getService().setSavedConversation(mConversation);
        }
        mWorkerFragment.setSavedConversation(mConversation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mConversation != null) {
            mConversation.getServer().getServerEventBus().unregister(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_ab, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final MenuItem item = menu.findItem(R.id.activity_main_ab_actions);
        item.setVisible(!mSlidingPane.isOpen() && mConversation != null);

        final MenuItem addServer = menu.findItem(R.id.activity_main_ab_add);
        addServer.setVisible(mSlidingPane.isOpen());

        final MenuItem settings = menu.findItem(R.id.activity_main_ab_settings);
        settings.setVisible(mSlidingPane.isOpen());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.home:
                UIUtils.toggleSlidingPane(mSlidingPane);
                return true;
            case R.id.activity_main_ab_actions:
                UIUtils.toggleDrawerLayout(mDrawerLayout, mRightDrawer);
                return true;
            case R.id.activity_main_ab_add:
                addNewServer();
                return true;
            case R.id.activity_main_ab_settings:
                openAppSettings();
                return true;
        }
        return false;
    }

    private void openAppSettings() {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void addNewServer() {
        final Intent intent = new Intent(this, ServerPreferenceActivity.class);
        intent.putExtra("new", true);
        intent.putExtra("file", "server");
        startActivityForResult(intent, SERVER_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_OK == resultCode) {
            if (requestCode == SERVER_SETTINGS) {
                mServerListFragment.refreshServers();
            }
        }
    }

    @Override
    public void onServerClicked(final Server server) {
        if (!server.equals(mConversation)) {
            final Bundle bundle = new Bundle();
            bundle.putString("title", server.getTitle());

            final IRCFragment fragment = new ServerFragment();
            fragment.setArguments(bundle);

            if (mConversation != null) {
                if (mConversation.getServer() != server) {
                    mConversation.getServer().getServerEventBus().unregister(this);
                    server.getServerEventBus().register(this);
                }
            } else {
                server.getServerEventBus().register(this);
            }

            mConversation = server;
            onChangeCurrentFragment(fragment);

            setActionBarTitle(server.getTitle());
            setActionBarSubtitle(MiscUtils.getStatusString(this, server.getStatus()));
            mActionsFragment.onFragmentTypeChanged(fragment.getType());
        }
        mSlidingPane.closePane();
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onSubServerClicked(final Conversation object) {
        if (!object.equals(mConversation)) {
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

            if (mConversation != null) {
                if (mConversation.getServer() != object.getServer()) {
                    mConversation.getServer().getServerEventBus().unregister(this);
                    object.getServer().getServerEventBus().register(this);
                }
            } else {
                object.getServer().getServerEventBus().register(this);
            }

            mConversation = object;

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
        setActionBarTitle(getString(R.string.app_name));
        setActionBarSubtitle(null);
        mCurrentFragment = null;
        mConversation = null;
    }

    @Override
    public void onServerDisconnected(final Server server) {
        closeDrawer();
        supportInvalidateOptionsMenu();
        onRemoveFragment();
    }

    @Override
    public IRCService getService() {
        return mWorkerFragment.getService();
    }

    @Override
    public void onRemoveCurrentFragment() {
        // TODO
    }

    @Override
    public Server getServer() {
        if (mConversation == null) {
            return null;
        }
        return mConversation.getServer();
    }

    @Override
    public void disconnectFromServer() {
        mWorkerFragment.disconnectFromServer(mConversation.getServer());
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
        if (mCurrentFragment != null) {
            if (mCurrentFragment.getType() == FragmentType.SERVER) {
                setActionBarSubtitle(MiscUtils.getStatusString(this,
                        mConversation.getServer().getStatus()));
            }
            mActionsFragment.onConnectionStatusChanged(mConversation.getServer().getStatus());
        }
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

    @Override
    public void onServiceConnected(final IRCService service) {
        mServerListFragment.onServiceConnected(service);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Conversation conversation = service.getSavedConversation();
                if (conversation != null) {
                    // TODO - what if disconnection occurred when not attached
                    if (conversation.getServer().equals(conversation)) {
                        onServerClicked(conversation.getServer());
                    } else {
                        onSubServerClicked(conversation);
                    }
                } else {
                    mSlidingPane.openPane();
                }
            }
        });
    }
}
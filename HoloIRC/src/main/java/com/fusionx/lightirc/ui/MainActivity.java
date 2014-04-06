package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.event.OnMentionEvent;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.server.StatusChangeEvent;
import com.fusionx.relay.interfaces.Conversation;

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

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import static com.fusionx.lightirc.util.UIUtils.findById;

public class MainActivity extends ActionBarActivity implements ServerListFragment.Callback,
        IRCFragment.Callback, SlidingPaneLayout.PanelSlideListener, DrawerLayout.DrawerListener,
        NavigationDrawerFragment.Callback, WorkerFragment.Callback {

    public static final int SERVER_SETTINGS = 1;

    private static final String WORKER_FRAGMENT = "WorkerFragment";

    private static final String ACTION_BAR_TITLE = "action_bar_title";

    private static final String ACTION_BAR_SUBTITLE = "action_bar_subtitle";

    private static final EventBus mEventBus = EventBus.getDefault();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Object mMentionHelper = new Object() {

        @SuppressWarnings("unused")
        public void onEvent(final OnMentionEvent event) {
            mEventBus.cancelEventDelivery(event);

            final Crouton crouton = Crouton.makeText(MainActivity.this, event.channelName,
                    Style.INFO);
            crouton.show();
        }
    };

    // Fields
    // IRC
    private Conversation mConversation;

    private final Object mConversationChanged = new Object() {
        @SuppressWarnings("unused")
        public void onEvent(final OnConversationChanged event) {
            mConversation = event.conversation;
        }
    };

    // Fragments
    private WorkerFragment mWorkerFragment;

    private IRCFragment mCurrentFragment;

    private NavigationDrawerFragment mNavigationDrawerFragment;

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
        mDrawerLayout.setDrawerListener(this);

        mRightDrawer = findById(this, R.id.right_drawer);

        // This is just registration because we'll retrieve the sticky event later
        mEventBus.register(mConversationChanged);
        mEventBus.register(mMentionHelper, 100);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            mServerListFragment = new ServerListFragment();
            transaction.replace(R.id.sliding_list_frame, mServerListFragment);

            mNavigationDrawerFragment = new NavigationDrawerFragment();
            transaction.add(R.id.right_drawer, mNavigationDrawerFragment);

            mWorkerFragment = new WorkerFragment();
            transaction.add(mWorkerFragment, WORKER_FRAGMENT);

            transaction.commit();
        } else {
            mServerListFragment = (ServerListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.sliding_list_frame);
            mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                    .findFragmentById(R
                            .id.right_drawer);
            mWorkerFragment = (WorkerFragment) getSupportFragmentManager()
                    .findFragmentByTag(WORKER_FRAGMENT);

            if (mConversation != null) {
                mConversation.getServer().getServerEventBus().register(this);
                mCurrentFragment = (IRCFragment) getSupportFragmentManager().findFragmentById(R.id
                        .content_frame);

                findById(this, R.id.content_frame_empty_textview).setVisibility(View.GONE);
                supportInvalidateOptionsMenu();
            }
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the action bar title & sub-title
        if (outState != null) {
            outState.putString(ACTION_BAR_TITLE, getSupportActionBar().getTitle().toString());
            // It's null if there's no fragment currently displayed
            if (getSupportActionBar().getSubtitle() != null) {
                outState.putString(ACTION_BAR_SUBTITLE, getSupportActionBar().getSubtitle()
                        .toString());
            }
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
    protected void onDestroy() {
        Crouton.clearCroutonsForActivity(this);

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

    // TODO - unify the server and subserver code
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

            setActionBarTitle(server.getTitle());
            setActionBarSubtitle(MiscUtils.getStatusString(this, server.getStatus()));

            mEventBus.postSticky(new OnConversationChanged(server, FragmentType.SERVER));
            onChangeCurrentFragment(fragment);
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
            if (object.getClass().equals(Channel.class)) {
                fragment = new ChannelFragment();
            } else if (object.getClass().equals(PrivateMessageUser.class)) {
                fragment = new UserFragment();
            } else {
                throw new IllegalArgumentException();
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

            setActionBarTitle(object.getId());
            setActionBarSubtitle(object.getServer().getTitle());

            mEventBus.postSticky(new OnConversationChanged(object, fragment.getType()));
            onChangeCurrentFragment(fragment);
        }
        mSlidingPane.closePane();
        supportInvalidateOptionsMenu();
    }

    private void onChangeCurrentFragment(final IRCFragment fragment) {
        mCurrentFragment = fragment;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.content_frame, fragment).commit();
            }
        }, 200);

        findById(this, R.id.content_frame_empty_textview).setVisibility(View.GONE);
    }

    private void onRemoveFragment() {
        final FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.remove(mCurrentFragment).commit();
        mCurrentFragment = null;

        findById(this, R.id.content_frame_empty_textview).setVisibility(View.VISIBLE);
    }

    @Override
    public void onServerDisconnected(final Server server) {
        closeDrawer();
        supportInvalidateOptionsMenu();
        onRemoveFragment();

        mEventBus.postSticky(new OnConversationChanged(null, null));

        setActionBarTitle(getString(R.string.app_name));
        setActionBarSubtitle(null);
    }

    @Override
    public IRCService getService() {
        return mWorkerFragment.getService();
    }

    /**
     * Removes the current displayed fragment
     *
     * Pre: this method is only called when mCurrentFragment and mConversation are not null
     * Post: the current fragment is removed - either by parting or removing the PM
     */
    @Override
    public void onRemoveCurrentFragment() {
        if (mCurrentFragment.getType() == FragmentType.CHANNEL) {
            mConversation.getServer().getServerCallBus().sendPart(mConversation.getId());
        } else {
            // TODO - fix this up
        }
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
        mNavigationDrawerFragment.onDrawerClosed();
    }

    @Override
    public void onDrawerStateChanged(final int newState) {
    }

    @Override
    public void onServiceConnected(final IRCService service) {
        mServerListFragment.onServiceConnected(service);
        final OnConversationChanged event = mEventBus.getStickyEvent(OnConversationChanged.class);
        final Conversation conversation = event != null ? event.conversation : null;

        if (conversation != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (conversation.getServer().equals(conversation)) {
                        onServerClicked(conversation.getServer());
                    } else {
                        onSubServerClicked(conversation);
                    }
                }
            });
        } else {
            mSlidingPane.openPane();
        }
    }

    // Subscribe events
    @SuppressWarnings("unused")
    public void onEventMainThread(final StatusChangeEvent event) {
        // Null happens when the disconnect handler is called first & the fragment has already been
        // removed by the disconnect handler
        if (mCurrentFragment != null) {
            final ConnectionStatus status = mConversation.getServer().getStatus();
            if (mCurrentFragment.getType() == FragmentType.SERVER) {
                setActionBarSubtitle(MiscUtils.getStatusString(this, status));
            }
            mEventBus.postSticky(new OnCurrentServerStatusChanged(status));
        }
    }
}
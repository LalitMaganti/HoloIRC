package com.fusionx.lightirc.ui;

import com.fusionx.bus.Bus;
import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.event.OnQueryEvent;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.misc.Theme;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.ui.widget.ProgrammableSlidingPaneLayout;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.lightirc.util.NotificationUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.QueryUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.event.server.KickEvent;
import com.fusionx.relay.event.server.PartEvent;
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

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;

import static com.fusionx.lightirc.misc.FragmentType.CHANNEL;
import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.UIUtils.findById;
import static com.fusionx.lightirc.util.UIUtils.isAppFromRecentApps;

public class MainActivity extends ActionBarActivity implements ServerListFragment.Callback,
        SlidingPaneLayout.PanelSlideListener, DrawerLayout.DrawerListener,
        NavigationDrawerFragment.Callback, WorkerFragment.Callback, IRCFragment.Callback {

    public static final int SERVER_SETTINGS = 1;

    public static final String CLEAR_CACHE = "clear_event_cache";

    private static final String WORKER_FRAGMENT = "WorkerFragment";

    private static final String ACTION_BAR_TITLE = "action_bar_title";

    private static final String ACTION_BAR_SUBTITLE = "action_bar_subtitle";

    private static final Bus mEventBus = getBus();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // Fields
    // IRC
    private Conversation mConversation;

    // Mention helper
    private final Object mMentionHelper = new Object() {
        @Subscribe(cancellable = true)
        public boolean onMentioned(final OnChannelMentionEvent event) {
            if (!event.channel.equals(mConversation)) {
                NotificationUtils.notifyInApp(MainActivity.this, event.channel);
            }
            return true;
        }

        @Subscribe(cancellable = true)
        public boolean onQueried(final OnQueryEvent event) {
            if (!event.queryUser.equals(mConversation)) {
                NotificationUtils.notifyInApp(MainActivity.this, event.queryUser);
            }
            return true;
        }
    };

    private final Object mConversationChanged = new Object() {
        @Subscribe
        public void onConversationChanged(final OnConversationChanged event) {
            mConversation = event.conversation;
        }
    };

    // Fragments
    private WorkerFragment mWorkerFragment;

    private IRCFragment mCurrentFragment;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private ServerListFragment mServerListFragment;

    // Views
    private ProgrammableSlidingPaneLayout mSlidingPane;

    private DrawerLayout mDrawerLayout;

    private View mNavigationDrawerView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        AppPreferences.setupAppPreferences(this);
        setTheme(UIUtils.getThemeInt());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mDrawerLayout = findById(this, R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);
        mDrawerLayout.setFocusableInTouchMode(false);

        mSlidingPane = findById(this, R.id.sliding_pane_layout);
        mSlidingPane.setParallaxDistance(100);
        mSlidingPane.setPanelSlideListener(this);
        if (AppPreferences.getAppPreferences().getTheme() == Theme.DARK) {
            // TODO - fix this hack
            mSlidingPane.setSliderFadeColor(0);
        }

        mNavigationDrawerView = findById(this, R.id.right_drawer);

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
                    .findFragmentById(R.id.right_drawer);
            mWorkerFragment = (WorkerFragment) getSupportFragmentManager()
                    .findFragmentByTag(WORKER_FRAGMENT);
            mCurrentFragment = (IRCFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.content_frame);

            // If the current fragment is not null then retrieve the matching convo
            if (mCurrentFragment != null) {
                mConversation = mEventBus.getStickyEvent(OnConversationChanged.class).conversation;
                // Make sure we re-register to the event bus on rotation - otherwise we miss
                // important status updates
                mConversation.getServer().getServerEventBus().register(this);
            } else {
                findById(MainActivity.this, R.id.content_frame_empty_textview).setVisibility
                        (View.VISIBLE);
            }

            supportInvalidateOptionsMenu();
        }
        if (mSlidingPane.isSlideable()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // TODO - unify the server and subserver code
    @Override
    public void onServerClicked(final Server server) {
        changeCurrentConversation(server, true);
    }

    @Override
    public void onSubServerClicked(final Conversation object) {
        changeCurrentConversation(object, true);
    }

    @Override
    public void onServerStopCompleteted(final Server server) {
        closeDrawer();
        supportInvalidateOptionsMenu();

        if (mCurrentFragment != null) {
            onRemoveCurrentFragmentAndConversation();
        }
        getService().removeLoggingHandlerAndEventCache(server);

        setActionBarTitle(getString(R.string.app_name));
        setActionBarSubtitle(null);
    }

    @Override
    public IRCService getService() {
        return mWorkerFragment.getService();
    }

    @Override
    public void onPart(final String serverName, final PartEvent event) {
        if (mConversation != null) {
            final boolean isCurrent = mConversation.getServer().getTitle().equals(serverName)
                    && mConversation.getId().equals(event.channelName);

            if (isCurrent) {
                onRemoveCurrentFragmentAndConversation();
            }
        }
    }

    @Override
    public boolean onKick(final String serverName, final KickEvent event) {
        if (mConversation != null) {
            final boolean isCurrent = mConversation.getServer().getTitle().equals(serverName)
                    && mConversation.getId().equals(event.channelName);

            if (isCurrent) {
                onRemoveCurrentFragmentAndConversation();
            }
            return isCurrent;
        }
        return false;
    }

    @Override
    public void onPrivateMessageClosed() {
        if (mCurrentFragment != null) {
            onRemoveCurrentFragmentAndConversation();
        }
    }

    /**
     * Removes the current displayed fragment
     *
     * Pre: this method is only called when mCurrentFragment and mConversation are not null
     * Post: the current fragment is removed - either by parting or removing the PM
     */
    @Override
    public void removeCurrentFragment() {
        if (mCurrentFragment.getType() == CHANNEL) {
            mConversation.getServer().getServerCallBus().sendPart(mConversation.getId());
        } else if (mCurrentFragment.getType() == FragmentType.USER) {
            mConversation.getServer().getServerCallBus().sendClosePrivateMessage(
                    (QueryUser) mConversation);
        }
    }

    @Override
    public void disconnectFromServer() {
        mWorkerFragment.disconnectFromServer(mConversation.getServer());
    }

    @Override
    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(mNavigationDrawerView);
    }

    @Override
    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        if (!mNavigationDrawerFragment.onBackPressed()) {
            if (mDrawerLayout.isDrawerOpen(mNavigationDrawerView)) {
                mDrawerLayout.closeDrawer(mNavigationDrawerView);
                return;
            }
            if (!mSlidingPane.isOpen()) {
                mSlidingPane.openPane();
                return;
            }
            super.onBackPressed();
        }
    }

    // TODO - fix this hack
    @Override
    public void onMentionMultipleUsers(List<WorldUser> users) {
        ((ChannelFragment) mCurrentFragment).onMentionMultipleUsers(users);
    }

    @Override
    public void reconnectToServer() {
        mWorkerFragment.reconnectToServer(mConversation.getServer());
    }

    @Override
    public void onPanelSlide(final View view, final float v) {
        // Empty
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
        mServerListFragment.onPanelClosed();
    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {
    }

    @Override
    public void onDrawerOpened(final View drawerView) {
        mSlidingPane.setSlideable(false);
    }

    @Override
    public void onDrawerClosed(final View drawerView) {
        mNavigationDrawerFragment.onDrawerClosed();
        mSlidingPane.setSlideable(true);
    }

    @Override
    public void onDrawerStateChanged(final int newState) {
    }

    @Override
    public void onServiceConnected(final IRCService service) {
        mServerListFragment.onServiceConnected(service);

        final boolean fromRecents = isAppFromRecentApps(getIntent().getFlags());
        final boolean clearCaches = getIntent().getBooleanExtra(CLEAR_CACHE, false);

        final String serverName = getIntent().getStringExtra("server_name");
        final String channelName = getIntent().getStringExtra("channel_name");

        final Conversation conversation;
        // If we are launching from recents then we are definitely not coming from the
        // notification - ignore what's in the intent
        if (!fromRecents && serverName != null) {
            // Try to remove the extras from the intent - this probably won't work though if the
            // activity finishes which is why we have the recents check
            getIntent().removeExtra("server_name");
            getIntent().removeExtra("channel_name");

            final Server server = service.getServerIfExists(serverName);
            conversation = server.getUserChannelInterface().getChannel(channelName);
        } else {
            final OnConversationChanged event = mEventBus.getStickyEvent(OnConversationChanged
                    .class);
            conversation = event != null ? event.conversation : null;
        }

        if (!fromRecents && clearCaches) {
            // Try to remove the extras from the intent - this probably won't work though if the
            // activity finishes which is why we have the recents check
            getIntent().removeExtra(CLEAR_CACHE);

            service.clearAllEventCaches();
        }

        onExternalConversationUpdate(conversation);
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_ab, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final boolean navigationDrawerEnabled = (!mSlidingPane.isSlideable() || !mSlidingPane
                .isOpen()) && mConversation != null;

        final MenuItem item = menu.findItem(R.id.activity_main_ab_actions);
        item.setVisible(navigationDrawerEnabled);

        final MenuItem users = menu.findItem(R.id.activity_main_ab_users);
        users.setVisible(navigationDrawerEnabled && mCurrentFragment.getType() == CHANNEL);

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
                UIUtils.toggleDrawerLayout(mDrawerLayout, mNavigationDrawerView);
                // If the drawer is now closed then we don't need to pass on the event
                return !mDrawerLayout.isDrawerOpen(mNavigationDrawerView);
            case R.id.activity_main_ab_users:
                if (!mDrawerLayout.isDrawerOpen(mNavigationDrawerView)) {
                    mDrawerLayout.openDrawer(mNavigationDrawerView);
                }
                // Not fully handled - still more work to do in the fragment
                return false;
            case R.id.activity_main_ab_add:
                addNewServer();
                return true;
            case R.id.activity_main_ab_settings:
                openAppSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public EventCache getEventCache() {
        return getService().getEventCache(mConversation.getServer());
    }

    @Override
    protected void onStop() {
        super.onStop();

        mEventBus.unregister(mConversationChanged);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the action bar title & sub-title
        getSupportActionBar().setTitle(savedInstanceState.getString(ACTION_BAR_TITLE));
        getSupportActionBar().setSubtitle(savedInstanceState.getString(ACTION_BAR_SUBTITLE));
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
    protected void onDestroy() {
        Crouton.clearCroutonsForActivity(this);

        super.onDestroy();

        if (mConversation != null) {
            mConversation.getServer().getServerEventBus().unregister(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final String serverName = intent.getStringExtra("server_name");
        final String channelName = intent.getStringExtra("channel_name");

        if (serverName != null) {
            // Try to remove the extras from the intent - this probably won't work though if the
            // activity finishes which is why we have the recents check
            getIntent().removeExtra("server_name");
            getIntent().removeExtra("channel_name");

            final Server server = getService().getServerIfExists(serverName);
            final Conversation conversation = server.getUserChannelInterface().getChannel
                    (channelName);
            onExternalConversationUpdate(conversation);
        }
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
    protected void onStart() {
        super.onStart();

        // This is just registration because we'll retrieve the sticky event later
        mEventBus.register(mConversationChanged);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mEventBus.unregister(mMentionHelper);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEventBus.register(mMentionHelper, 100);
    }

    void setActionBarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

    void setActionBarSubtitle(final String subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }

    private void changeCurrentConversation(final Server server, final boolean delayChange) {
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
            onChangeCurrentFragment(fragment, delayChange);
        }
        mSlidingPane.closePane();
        supportInvalidateOptionsMenu();
    }

    private void changeCurrentConversation(final Conversation object, boolean delayChange) {
        if (!object.equals(mConversation)) {
            final Bundle bundle = new Bundle();
            bundle.putString("title", object.getId());

            final IRCFragment fragment;
            if (object.getClass().equals(Channel.class)) {
                fragment = new ChannelFragment();
            } else if (object.getClass().equals(QueryUser.class)) {
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
            onChangeCurrentFragment(fragment, delayChange);
        }
        mSlidingPane.closePane();
        supportInvalidateOptionsMenu();
    }

    private void onChangeCurrentFragment(final IRCFragment fragment, boolean delayChange) {
        mCurrentFragment = fragment;

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.content_frame, fragment).commit();
                getSupportFragmentManager().executePendingTransactions();

                findById(MainActivity.this, R.id.content_frame_empty_textview)
                        .setVisibility(View.GONE);
            }
        };

        if (delayChange) {
            mHandler.postDelayed(runnable, 300);
        } else {
            mHandler.post(runnable);
        }
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

    private void onRemoveCurrentFragmentAndConversation() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.remove(mCurrentFragment).commit();
        mCurrentFragment = null;
        getSupportFragmentManager().executePendingTransactions();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findById(MainActivity.this, R.id.content_frame_empty_textview).setVisibility(View
                        .VISIBLE);
            }
        }, 300);

        // Don't listen for any more events from this server
        mConversation.getServer().getServerEventBus().unregister(this);
        mEventBus.postSticky(new OnConversationChanged(null, null));
    }

    private void onExternalConversationUpdate(final Conversation conversation) {
        if (conversation != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (conversation.getServer().equals(conversation)) {
                        changeCurrentConversation(conversation.getServer(), false);
                    } else {
                        changeCurrentConversation(conversation, false);
                    }
                }
            });

            supportInvalidateOptionsMenu();
        } else {
            mSlidingPane.openPane();

            findById(MainActivity.this, R.id.content_frame_empty_textview).setVisibility
                    (View.VISIBLE);
        }
    }
}
package com.fusionx.lightirc.ui;

import com.google.common.base.Optional;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.event.OnQueryEvent;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.util.CompatUtils;
import com.fusionx.lightirc.util.CrashUtils;
import com.fusionx.lightirc.util.NotificationUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.lightirc.view.ProgrammableSlidingPaneLayout;
import com.fusionx.lightirc.view.Snackbar;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

import co.fusionx.relay.conversation.Channel;
import co.fusionx.relay.conversation.Conversation;
import co.fusionx.relay.conversation.QueryUser;
import co.fusionx.relay.conversation.Server;
import co.fusionx.relay.core.ChannelUser;
import co.fusionx.relay.core.Session;
import co.fusionx.relay.core.SessionStatus;
import co.fusionx.relay.internal.dcc.base.RelayDCCChatConversation;
import co.fusionx.relay.internal.dcc.base.RelayDCCFileConversation;
import co.fusionx.relay.event.channel.PartEvent;
import co.fusionx.relay.event.server.KickEvent;
import co.fusionx.relay.event.server.StatusChangeEvent;
import co.fusionx.relay.internal.function.Optionals;

import static com.fusionx.lightirc.misc.AppPreferences.getAppPreferences;
import static com.fusionx.lightirc.misc.FragmentType.CHANNEL;
import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.MiscUtils.getStatusString;
import static com.fusionx.lightirc.util.UIUtils.isAppFromRecentApps;

/**
 * Main activity which co-ordinates everything in the app
 */
public class MainActivity extends ActionBarActivity implements SessionOverviewFragment.Callback,
        NavigationDrawerFragment.Callback, WorkerFragment.Callback,
        ConversationFragment.Callback {

    public static final int SERVER_SETTINGS = 1;

    public static final String CLEAR_CACHE = "clear_event_cache";

    private static final String WORKER_FRAGMENT = "WorkerFragment";

    private static final String ACTION_BAR_TITLE = "action_bar_title";

    private static final String ACTION_BAR_SUBTITLE = "action_bar_subtitle";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final SlidingPaneLayout.PanelSlideListener mPanelSlideListener
            = new SlidingPaneLayout.PanelSlideListener() {

        @Override
        public void onPanelSlide(final View view, final float v) {
            // Empty
        }

        @Override
        public void onPanelOpened(final View view) {
            supportInvalidateOptionsMenu();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        @Override
        public void onPanelClosed(final View view) {
            supportInvalidateOptionsMenu();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // mSessionOverviewFragment.onPanelClosed();
        }
    };

    // Fragments
    private WorkerFragment mWorkerFragment;

    // IRC
    private OnConversationChanged mConversationEvent;

    private final Object mConversationChanged = new Object() {
        @Subscribe
        public void onConversationChanged(final OnConversationChanged event) {
            mConversationEvent = event;
        }
    };

    private BaseIRCFragment mCurrentFragment;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private SessionOverviewFragment mSessionOverviewFragment;

    // Views
    private ProgrammableSlidingPaneLayout mSlidingPane;

    private DrawerLayout mDrawerLayout;

    private View mNavigationDrawerView;

    private TextView mEmptyView;

    private Snackbar mSnackbar;

    // Fields
    // Mention helper
    private final Object mMentionHelper = new Object() {
        @Subscribe(cancellable = true)
        public boolean onMentioned(final OnChannelMentionEvent event) {
            if (!event.channel.equals(mConversationEvent)) {
                NotificationUtils.notifyInApp(mSnackbar, MainActivity.this, event.connection,
                        event.channel);
            }
            return true;
        }

        @Subscribe(cancellable = true)
        public boolean onQueried(final OnQueryEvent event) {
            if (!event.queryUser.equals(mConversationEvent)) {
                NotificationUtils.notifyInApp(mSnackbar, MainActivity.this, event.connection,
                        event.queryUser);
            }
            return true;
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        AppPreferences.setupAppPreferences(this);
        setTheme(UIUtils.getThemeInt());
        if (CompatUtils.hasKitKat()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        CrashUtils.startCrashlyticsIfAppropriate(this);

        setContentView(R.layout.main_activity);

        // create our manager instance after the content view is set
        final SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // set a custom tint color for all system bars
        tintManager.setTintColor(getResources().getColor(R.color.action_bar_background_color));

        mEmptyView = (TextView) findViewById(R.id.content_frame_empty_textview);

        mSnackbar = (Snackbar) findViewById(R.id.snackbar);
        mSnackbar.post(mSnackbar::hide);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerListener());
        mDrawerLayout.setFocusableInTouchMode(false);

        mSlidingPane = (ProgrammableSlidingPaneLayout) findViewById(R.id.sliding_pane_layout);
        mSlidingPane.setParallaxDistance(100);
        mSlidingPane.setPanelSlideListener(mPanelSlideListener);
        mSlidingPane.setSliderFadeColor(0);

        mNavigationDrawerView = findViewById(R.id.right_drawer);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            mSessionOverviewFragment = new SessionOverviewFragment();
            transaction.replace(R.id.sliding_list_frame, mSessionOverviewFragment);

            mNavigationDrawerFragment = new NavigationDrawerFragment();
            transaction.add(R.id.right_drawer, mNavigationDrawerFragment);

            mWorkerFragment = new WorkerFragment();
            transaction.add(mWorkerFragment, WORKER_FRAGMENT);

            transaction.commit();
        } else {
            mSessionOverviewFragment = (SessionOverviewFragment) getSupportFragmentManager().findFragmentById(
                    R.id.sliding_list_frame);
            mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.right_drawer);
            mWorkerFragment = (WorkerFragment) getSupportFragmentManager()
                    .findFragmentByTag(WORKER_FRAGMENT);
            mCurrentFragment = (BaseIRCFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.content_frame);

            // If the current fragment is not null then retrieve the matching convo
            final OnConversationChanged event = getBus()
                    .getStickyEvent(OnConversationChanged.class);
            if (mCurrentFragment == null) {
                findViewById(R.id.content_frame_empty_textview).setVisibility(View.VISIBLE);
            } else if (event != null && event.conversation != null) {
                mConversationEvent = event;

                // Make sure we re-register to the event bus on rotation - otherwise we miss
                // important status updates
                mConversationEvent.session.registerForEvents(this);
            } else {
                onRemoveCurrentFragment();
            }
            supportInvalidateOptionsMenu();
        }

        if (mSlidingPane.isSlideable()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onConversationClicked(final Session connection,
            final Conversation conversation) {
        changeCurrentConversation(connection, conversation, true);
    }

    @Override
    public void onServerStopped(final Session connection) {
        closeDrawer();
        supportInvalidateOptionsMenu();

        if (mCurrentFragment != null && connection.equals(mConversationEvent.session)) {
            onRemoveCurrentFragmentAndConversation();
        }
    }

    @Override
    public IRCService getService() {
        return mWorkerFragment.getService();
    }

    @Override
    public void onPart(final Session connection, final PartEvent event) {
        if (event.conversation.equals(mConversationEvent.conversation)) {
            onRemoveCurrentFragmentAndConversation();
        }
    }

    @Override
    public boolean onKick(final Session connection, final KickEvent event) {
        final boolean isCurrent = event.channel.equals(mConversationEvent.conversation);
        if (isCurrent) {
            onRemoveCurrentFragmentAndConversation();
        }
        return isCurrent;
    }

    @Override
    public void onPrivateMessageClosed(final QueryUser queryUser) {
        if (queryUser.equals(mConversationEvent.conversation)) {
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
            final Channel channel = (Channel) mConversationEvent.conversation;
            channel.sendPart(Optional.fromNullable(getAppPreferences().getPartReason()));
        } else if (mCurrentFragment.getType() == FragmentType.USER) {
            final QueryUser user = (QueryUser) mConversationEvent.conversation;
            user.close();
        }
    }

    @Override
    public void disconnectFromServer() {
        getService().requestConnectionStoppage(mConversationEvent.session);
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
        if (mDrawerLayout.isDrawerOpen(mNavigationDrawerView)) {
            mDrawerLayout.closeDrawer(mNavigationDrawerView);
            return;
        } else if (!mSlidingPane.isOpen()) {
            mSlidingPane.openPane();
            return;
        }
        super.onBackPressed();
    }

    // TODO - fix this hack
    @Override
    public void onMentionMultipleUsers(final List<ChannelUser> users) {
        ((ChannelFragment) mCurrentFragment).onMentionMultipleUsers(users);
    }

    @Override
    public void reconnectToServer() {
        IRCService.requestReconnectionToServer(mConversationEvent.session);
    }

    @Override
    public void onServiceConnected(final IRCService service) {
        mSessionOverviewFragment.onServiceConnected(service);

        final boolean fromRecents = isAppFromRecentApps(getIntent().getFlags());
        final boolean clearCaches = getIntent().getBooleanExtra(CLEAR_CACHE, false);

        final String serverName = getIntent().getStringExtra("server_name");
        final String channelName = getIntent().getStringExtra("channel_name");
        final String queryNick = getIntent().getStringExtra("query_nick");

        final Optional<Session> connection;
        final Optional<? extends Conversation> optConversation;
        // If we are launching from recents then we are definitely not coming from the
        // notification - ignore what's in the intent
        if (fromRecents || serverName == null) {
            final OnConversationChanged event = getBus()
                    .getStickyEvent(OnConversationChanged.class);
            connection = Optional.fromNullable(event).transform(e -> event.session);
            optConversation = Optional.fromNullable(event).transform(e -> event.conversation);
        } else {
            // Try to remove the extras from the intent - this probably won't work though if the
            // activity finishes which is why we have the recents check
            getIntent().removeExtra("server_name");
            getIntent().removeExtra("channel_name");
            getIntent().removeExtra("query_nick");

            connection = IRCService.getSessionIfExists(serverName);
            optConversation = Optionals.flatTransform(connection, c -> channelName == null
                    ? c.getQueryManager().getQueryUser(queryNick)
                    : c.getUserChannelManager().getChannel(channelName));
        }

        if (!fromRecents && clearCaches) {
            // Try to remove the extras from the intent - this probably won't work though if the
            // activity finishes which is why we have the recents check
            getIntent().removeExtra(CLEAR_CACHE);
            service.clearAllEventCaches();
        }

        onExternalConversationChange(
                optConversation.transform(c -> new Pair<>(connection.get(), c)));
    }

    // Subscribe events
    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final StatusChangeEvent event) {
        // Null happens when the disconnect handler is called first & the fragment has already been
        // removed by the disconnect handler
        if (mCurrentFragment == null) {
            return;
        }
        final SessionStatus status = mConversationEvent.session.getStatus();
        if (mCurrentFragment.getType() == FragmentType.SERVER) {
            setActionBarSubtitle(getStatusString(this, status));
        }
        getBus().postSticky(new OnCurrentServerStatusChanged(status));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_ab, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final boolean navigationDrawerEnabled = !(mSlidingPane.isSlideable() && mSlidingPane
                .isOpen()) && mConversationEvent != null;

        final MenuItem item = menu.findItem(R.id.activity_main_ab_actions);
        item.setVisible(navigationDrawerEnabled);

        final MenuItem users = menu.findItem(R.id.activity_main_ab_users);
        users.setVisible(navigationDrawerEnabled);

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
                // mSessionOverviewFragment.addMultiple();
                return true;
            case R.id.activity_main_ab_settings:
                openAppSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public EventCache getEventCache(final Session connection) {
        return IRCService.getEventCache(connection);
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

        if (RESULT_OK == resultCode && requestCode == SERVER_SETTINGS) {
            mSessionOverviewFragment.refreshServers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mConversationEvent != null && mConversationEvent.session != null) {
            mConversationEvent.session.unregisterFromEvents(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final String serverName = intent.getStringExtra("server_name");
        final String channelName = intent.getStringExtra("channel_name");
        final String queryNick = intent.getStringExtra("query_nick");

        if (serverName == null) {
            return;
        }
        // Try to remove the extras from the intent - this probably won't work though if the
        // activity finishes which is why we have the recents check
        getIntent().removeExtra("server_name");
        getIntent().removeExtra("channel_name");
        getIntent().removeExtra("query_nick");

        final Optional<Session> connection = IRCService.getSessionIfExists(serverName);
        final Optional<? extends Conversation> optional = Optionals.flatTransform
                (connection, c -> channelName == null
                        ? c.getQueryManager().getQueryUser(queryNick)
                        : c.getUserChannelManager().getChannel(channelName));
        onExternalConversationChange(optional.transform(c -> new Pair<>(connection.get(), c)));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the action bar title & sub-title
        outState.putString(ACTION_BAR_TITLE, getSupportActionBar().getTitle().toString());
        // It's null if there's no fragment currently displayed
        if (getSupportActionBar().getSubtitle() != null) {
            outState.putString(ACTION_BAR_SUBTITLE, getSupportActionBar().getSubtitle().toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getBus().register(mMentionHelper, 100);

        // TODO - what if conversation is changed when stopped
        // This is just registration because we'll retrieve the sticky event later
        getBus().register(mConversationChanged);

        if (mCurrentFragment != null && !mCurrentFragment.isValid()) {
            onRemoveCurrentFragmentAndConversation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        getBus().unregister(mMentionHelper);

        // TODO - what if conversation is changed when stopped
        getBus().unregister(mConversationChanged);
    }

    void setActionBarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

    void setActionBarSubtitle(final String subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }

    private void changeCurrentConversation(final Session session,
            final Conversation object, final boolean delayChange) {
        if (!object.equals(mConversationEvent)) {
            final Bundle bundle = new Bundle();
            bundle.putString("title", object.getId());

            final boolean isServer = Server.class.isInstance(object);
            final BaseIRCFragment fragment;
            if (isServer) {
                fragment = new ServerFragment();
            } else if (Channel.class.isInstance(object)) {
                fragment = new ChannelFragment();
            } else if (QueryUser.class.isInstance(object)) {
                fragment = new QueryFragment();
            } else if (RelayDCCChatConversation.class.isInstance(object)) {
                fragment = new DCCChatFragment();
            } else if (RelayDCCFileConversation.class.isInstance(object)) {
                fragment = new DCCFileFragment();
            } else {
                throw new IllegalArgumentException();
            }
            fragment.setArguments(bundle);

            if (mConversationEvent == null || mConversationEvent.conversation == null) {
                session.registerForEvents(this);
            } else if (mConversationEvent.session != session) {
                mConversationEvent.session.unregisterFromEvents(this);
                session.registerForEvents(this);
            }

            setActionBarTitle(object.getId());
            setActionBarSubtitle(isServer
                    ? getStatusString(this, session.getStatus())
                    : session.getServer().getTitle());

            getBus().postSticky(new OnConversationChanged(session, object, fragment.getType()));
            changeCurrentFragment(fragment, delayChange);
        }
        mSlidingPane.closePane();
        supportInvalidateOptionsMenu();
    }

    private void changeCurrentFragment(final BaseIRCFragment fragment, boolean delayChange) {
        mCurrentFragment = fragment;

        final Runnable runnable = () -> {
            final FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.content_frame, fragment).commit();
            getSupportFragmentManager().executePendingTransactions();

            mEmptyView.setVisibility(View.GONE);
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
        onRemoveCurrentFragment();

        // Don't listen for any more events from this server
        mConversationEvent.session.unregisterFromEvents(this);
        getBus().postSticky(new OnConversationChanged(null, null, null));
    }

    private void onRemoveCurrentFragment() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.remove(mCurrentFragment).commit();
        mCurrentFragment = null;
        getSupportFragmentManager().executePendingTransactions();

        mHandler.postDelayed(() -> mEmptyView.setVisibility(View.VISIBLE), 300);

        // Remove any title/subtitle from the action bar
        setActionBarTitle(getString(R.string.app_name));
        setActionBarSubtitle(null);

        // Close the nav drawer when the current fragment is removed
        closeDrawer();
    }

    private void onExternalConversationChange(final Optional<Pair<Session,
            Conversation>> optionalPair) {
        if (optionalPair.isPresent()) {
            final Pair<Session, Conversation> pair = optionalPair.get();
            mHandler.post(() -> changeCurrentConversation(pair.first, pair.second, false));
            supportInvalidateOptionsMenu();
        } else {
            mSlidingPane.openPane();
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private class DrawerListener implements DrawerLayout.DrawerListener {

        @Override
        public void onDrawerSlide(final View drawerView, final float slideOffset) {
        }

        @Override
        public void onDrawerOpened(final View drawerView) {
            mSlidingPane.setSlideable(false);
        }

        @Override
        public void onDrawerClosed(final View drawerView) {
            mSlidingPane.setSlideable(true);
        }

        @Override
        public void onDrawerStateChanged(final int newState) {
        }
    }
}
package com.fusionx.lightirc.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.event.OnQueryEvent;
import com.fusionx.lightirc.event.OnServiceConnectionStateChanged;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.misc.Theme;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.util.CrashUtils;
import com.fusionx.lightirc.util.NotificationUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.lightirc.view.ProgrammableSlidingPaneLayout;
import com.fusionx.lightirc.view.Snackbar;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.ChannelUser;
import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.Nick;
import co.fusionx.relay.base.QueryUser;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.dcc.chat.DCCChatConversation;
import co.fusionx.relay.dcc.file.DCCFileConversation;
import co.fusionx.relay.event.channel.PartEvent;
import co.fusionx.relay.event.server.KickEvent;
import co.fusionx.relay.event.server.StatusChangeEvent;

import static com.fusionx.lightirc.misc.AppPreferences.getAppPreferences;
import static com.fusionx.lightirc.misc.FragmentType.CHANNEL;
import static com.fusionx.lightirc.misc.FragmentType.DCCCHAT;
import static com.fusionx.lightirc.misc.FragmentType.DCCFILE;
import static com.fusionx.lightirc.misc.FragmentType.SERVER;
import static com.fusionx.lightirc.misc.FragmentType.USER;
import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.MiscUtils.getStatusString;
import static com.fusionx.lightirc.util.UIUtils.isAppFromRecentApps;

/**
 * Main activity which co-ordinates everything in the app
 */
public class MainActivity extends AppCompatActivity implements ServerListFragment.Callback,
        NavigationDrawerFragment.Callback, IRCFragment.Callback,
        ViewTreeObserver.OnGlobalLayoutListener {

    public static final int SERVER_SETTINGS = 1;

    public static final String CLEAR_CACHE = "clear_event_cache";

    private static final String ACTION_BAR_TITLE = "action_bar_title";

    private static final String ACTION_BAR_SUBTITLE = "action_bar_subtitle";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final SlidingPaneLayout.PanelSlideListener mPanelSlideListener
            = new SlidingPaneLayout.PanelSlideListener() {

        @Override
        public void onPanelSlide(final View view, final float v) {
            mPaneIndicator.onPanelSlide(view, v);
        }

        @Override
        public void onPanelOpened(final View view) {
            supportInvalidateOptionsMenu();
            mPaneIndicator.onPanelOpened(view);
        }

        @Override
        public void onPanelClosed(final View view) {
            supportInvalidateOptionsMenu();
            mPaneIndicator.onPanelClosed(view);
            mServerListFragment.onPanelClosed();
        }
    };

    // IRC
    private IRCService mService;
    private Conversation mConversation;

    private final Object mConversationChanged = new Object() {
        @Subscribe
        public void onConversationChanged(final OnConversationChanged event) {
            mConversation = event.conversation;
        }
    };

    private BaseIRCFragment mCurrentFragment;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private ServerListFragment mServerListFragment;

    // Views
    private ProgrammableSlidingPaneLayout mSlidingPane;
    private SlidingPaneToggleArrow mPaneIndicator;

    private DrawerLayout mDrawerLayout;

    private View mNavigationDrawerView;

    private TextView mEmptyView;

    private Snackbar mSnackbar;

    // Fields
    // Mention helper
    private final Object mMentionHelper = new Object() {
        @Subscribe(cancellable = true)
        public boolean onMentioned(final OnChannelMentionEvent event) {
            if (!event.channel.equals(mConversation)) {
                NotificationUtils.notifyInApp(mSnackbar, MainActivity.this, event.channel, true);
            }
            return true;
        }

        @Subscribe(cancellable = true)
        public boolean onQueried(final OnQueryEvent event) {
            if (!event.queryUser.equals(mConversation)) {
                NotificationUtils.notifyInApp(mSnackbar, MainActivity.this, event.queryUser, false);
            }
            return true;
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder binder) {
            mService = ((IRCService.IRCBinder) binder).getService();
            MainActivity.this.onServiceConnected();
            getBus().postSticky(new OnServiceConnectionStateChanged(mService));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            getBus().postSticky(new OnServiceConnectionStateChanged(mService));
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());
        setStatusBarTransparency(true);
        super.onCreate(savedInstanceState);
        CrashUtils.startCrashlyticsIfAppropriate(this);

        setContentView(R.layout.main_activity);

        // create our manager instance after the content view is set
        final SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // set a custom tint color for all system bars
        tintManager.setTintColor(ResourcesCompat.getColor(
                getResources(), R.color.colorPrimaryDark, null));

        mEmptyView = (TextView) findViewById(R.id.content_frame_empty_textview);

        mSnackbar = (Snackbar) findViewById(R.id.snackbar);
        mSnackbar.post(mSnackbar::hide);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerListener());
        mDrawerLayout.setFocusableInTouchMode(false);

        mSlidingPane = (ProgrammableSlidingPaneLayout) findViewById(R.id.sliding_pane_layout);
        mSlidingPane.setParallaxDistance(100);
        mSlidingPane.setPanelSlideListener(mPanelSlideListener);
        mSlidingPane.setSliderFadeColor(0);

        mPaneIndicator = new SlidingPaneToggleArrow(this, mSlidingPane);

        mNavigationDrawerView = findViewById(R.id.right_drawer);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            mServerListFragment = new ServerListFragment();
            transaction.replace(R.id.sliding_list_frame, mServerListFragment);

            mNavigationDrawerFragment = new NavigationDrawerFragment();
            transaction.add(R.id.right_drawer, mNavigationDrawerFragment);

            transaction.commit();
        } else {
            mServerListFragment = (ServerListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.sliding_list_frame);
            mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.right_drawer);
            mCurrentFragment = (BaseIRCFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.content_frame);

            // If the current fragment is not null then retrieve the matching convo
            final OnConversationChanged event = getBus()
                    .getStickyEvent(OnConversationChanged.class);
            if (event != null && event.conversation != null) {
                if (mCurrentFragment == null ||
                        conversationToFragmentType(event.conversation) != mCurrentFragment.getType()) {
                    onRemoveCurrentFragment();
                } else {
                    mConversation = event.conversation;
                    // Make sure we re-register to the event bus on rotation - otherwise we miss
                    // important status updates
                    mConversation.getServer().getServerWideBus().register(this);
                }
            } else {
                onRemoveCurrentFragment();
            }
            supportInvalidateOptionsMenu();
        }

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onGlobalLayout() {
        final ViewTreeObserver vto = getWindow().getDecorView().getViewTreeObserver();
        if (Build.VERSION.SDK_INT >= 16) {
            vto.removeOnGlobalLayoutListener(this);
        } else {
            vto.removeGlobalOnLayoutListener(this);
        }
        if (mSlidingPane.isSlideable()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mPaneIndicator.syncState();
    }

    @Override
    public void onServerClicked(final Server server) {
        changeCurrentConversation(server, true);
    }

    @Override
    public void onSubServerClicked(final Conversation object) {
        changeCurrentConversation(object, true);
    }

    @Override
    public void onServerStopped(final Server server) {
        closeDrawer();
        supportInvalidateOptionsMenu();

        if (mCurrentFragment != null && server.equals(mConversation.getServer())) {
            onRemoveCurrentFragmentAndConversation();
        }
    }

    @Override
    public void onPart(final String serverName, final PartEvent event) {
        if (event.channel.equals(mConversation)) {
            onRemoveCurrentFragmentAndConversation();
        }
    }

    @Override
    public boolean onKick(final Server server, final KickEvent event) {
        final boolean isCurrent = event.channel.equals(mConversation);
        if (isCurrent) {
            onRemoveCurrentFragmentAndConversation();
        }
        return isCurrent;
    }

    @Override
    public void onPrivateMessageClosed(final QueryUser queryUser) {
        if (queryUser.equals(mConversation)) {
            onRemoveCurrentFragmentAndConversation();
        }
    }

    /**
     * Removes the current displayed fragment
     * <p>
     * Pre: this method is only called when mCurrentFragment and mConversation are not null
     * Post: the current fragment is removed - either by parting or removing the PM
     */
    @Override
    public void removeCurrentFragment() {
        if (mCurrentFragment.getType() == CHANNEL) {
            final Channel channel = (Channel) mConversation;
            channel.sendPart(Optional.fromNullable(getAppPreferences().getPartReason()));
        } else if (mCurrentFragment.getType() == FragmentType.USER) {
            final QueryUser user = (QueryUser) mConversation;
            user.close();
        }
    }

    @Override
    public void disconnectFromServer() {
        mService.requestConnectionStoppage(mConversation.getServer());
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
    public void onMentionMultipleUsers(final List<Nick> users) {
        ((ChannelFragment) mCurrentFragment).onMentionMultipleUsers(users);
    }

    @Override
    public void reconnectToServer() {
        mService.requestReconnectionToServer(mConversation.getServer());
    }

    private void onServiceConnected() {
        final boolean fromRecents = isAppFromRecentApps(getIntent().getFlags());
        final boolean clearCaches = getIntent().getBooleanExtra(CLEAR_CACHE, false);

        final String serverName = getIntent().getStringExtra("server_name");
        final String channelName = getIntent().getStringExtra("channel_name");
        final String queryNick = getIntent().getStringExtra("query_nick");

        final Optional<? extends Conversation> optConversation;
        // If we are launching from recents then we are definitely not coming from the
        // notification - ignore what's in the intent
        if (fromRecents || serverName == null) {
            final OnConversationChanged event = getBus()
                    .getStickyEvent(OnConversationChanged.class);
            optConversation = event == null
                    ? Optional.absent()
                    : Optional.fromNullable(event.conversation);
        } else {
            // Try to remove the extras from the intent - this probably won't work though if the
            // activity finishes which is why we have the recents check
            getIntent().removeExtra("server_name");
            getIntent().removeExtra("channel_name");
            getIntent().removeExtra("query_nick");

            final Server server = mService.getServerIfExists(serverName);
            optConversation = channelName == null
                    ? server.getUserChannelInterface().getQueryUser(queryNick)
                    : server.getUserChannelInterface().getChannel(channelName);
        }

        if (!fromRecents && clearCaches) {
            // Try to remove the extras from the intent - this probably won't work though if the
            // activity finishes which is why we have the recents check
            getIntent().removeExtra(CLEAR_CACHE);
            mService.clearAllEventCaches();
        }

        onExternalConversationUpdate(optConversation);
    }

    // Subscribe events
    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final StatusChangeEvent event) {
        // Null happens when the disconnect handler is called first & the fragment has already been
        // removed by the disconnect handler
        if (mCurrentFragment == null) {
            return;
        }
        final ConnectionStatus status = mConversation.getServer().getStatus();
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
        final boolean navigationDrawerEnabled =
                (!mSlidingPane.isSlideable() || !mSlidingPane.isOpen()) && mConversation != null;

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
        if (mPaneIndicator.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.activity_main_ab_actions:
                boolean nowOpen = UIUtils.toggleDrawerLayout(mDrawerLayout, mNavigationDrawerView);
                // If the drawer is now closed then we don't need to pass on the event
                return !nowOpen;
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
    public EventCache getEventCache(final Conversation conversation) {
        return IRCService.getEventCache(conversation.getServer(),
                AppPreferences.getAppPreferences().getTheme() == Theme.DARK);
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
            mServerListFragment.refreshServers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mConversation != null) {
            mConversation.getServer().getServerWideBus().unregister(this);
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

        final Server server = mService.getServerIfExists(serverName);
        final Optional<? extends Conversation> optConversation = channelName == null
                ? server.getUserChannelInterface().getQueryUser(queryNick)
                : server.getUserChannelInterface().getChannel(channelName);
        onExternalConversationUpdate(optConversation);
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
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, IRCService.class), mServiceConnection,
                BIND_AUTO_CREATE | BIND_ADJUST_WITH_ACTIVITY);
        NotificationUtils.cancelMentionNotification(this, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
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

    private void setStatusBarTransparency(boolean transparent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        getWindow().setFlags(transparent ? WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS : 0,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private void changeCurrentConversation(final Conversation object, final boolean delayChange) {
        if (!object.equals(mConversation)) {
            final Bundle bundle = new Bundle();
            bundle.putString("title", object.getId());

            final boolean isServer = Server.class.isInstance(object);
            final BaseIRCFragment fragment;
            if (isServer) {
                fragment = new ServerFragment();
            } else if (Channel.class.isInstance(object)) {
                fragment = new ChannelFragment();
            } else if (QueryUser.class.isInstance(object)) {
                fragment = new UserFragment();
            } else if (DCCChatConversation.class.isInstance(object)) {
                fragment = new DCCChatFragment();
            } else if (DCCFileConversation.class.isInstance(object)) {
                fragment = new DCCFileFragment();
            } else {
                throw new IllegalArgumentException();
            }
            fragment.setArguments(bundle);

            if (mConversation == null) {
                object.getServer().getServerWideBus().register(this);
            } else if (mConversation.getServer() != object.getServer()) {
                mConversation.getServer().getServerWideBus().unregister(this);
                object.getServer().getServerWideBus().register(this);
            }

            setActionBarTitle(object.getId());
            setActionBarSubtitle(isServer
                    ? getStatusString(this, object.getServer().getStatus())
                    : object.getServer().getTitle());

            getBus().postSticky(new OnConversationChanged(object, fragment.getType()));
            changeCurrentFragment(fragment, delayChange);
        }
        mSlidingPane.closePane();
        supportInvalidateOptionsMenu();
    }

    private void changeCurrentFragment(final BaseIRCFragment fragment, boolean delayChange) {
        mCurrentFragment = fragment;

        final Runnable runnable = () -> getSupportFragmentManager().executePendingTransactions();

        final FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.content_frame, fragment).commit();
        mEmptyView.setVisibility(View.GONE);

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
        mConversation.getServer().getServerWideBus().unregister(this);
        getBus().postSticky(new OnConversationChanged(null, null));
    }

    private void onRemoveCurrentFragment() {
        if (mCurrentFragment == null) {
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        }

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

    private void onExternalConversationUpdate(final Optional<? extends Conversation> optConversation) {
        if (optConversation.isPresent()) {
            mHandler.post(() -> changeCurrentConversation(optConversation.get(), false));
            supportInvalidateOptionsMenu();
        } else {
            mSlidingPane.openPane();
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private static FragmentType conversationToFragmentType(Conversation conversation) {
        Set<Class<?>> interfaces = Collections.newSetFromMap(new ArrayMap<>());
        interfaces.addAll(Arrays.asList(conversation.getClass().getInterfaces()));
        if (interfaces.contains(Server.class)) {
            return SERVER;
        } else if (interfaces.contains(Channel.class)) {
            return CHANNEL;
        } else if (interfaces.contains(QueryUser.class)) {
            return USER;
        } else if (interfaces.contains(DCCChatConversation.class)) {
            return DCCCHAT;
        } else if (interfaces.contains(DCCFileConversation.class)) {
            return DCCFILE;
        }
        return CHANNEL;
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
            mNavigationDrawerFragment.onUserPanelNotVisible();
        }

        @Override
        public void onDrawerStateChanged(final int newState) {
        }
    }
}
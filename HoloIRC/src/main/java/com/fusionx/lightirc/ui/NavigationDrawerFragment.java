package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.service.ServiceEventInterceptor;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ChannelUser;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.Conversation;
import com.fusionx.relay.event.server.InviteEvent;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.UIUtils.findById;

public class NavigationDrawerFragment extends Fragment implements
        ActionsFragment.Callbacks, UserListFragment.Callback, InviteFragment.Callbacks {

    private ConnectionStatus mStatus;

    private FragmentType mFragmentType;

    private Conversation mConversation;

    private final Object mEventHandler = new Object() {
        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            mConversation = conversationChanged.conversation;
            mFragmentType = conversationChanged.fragmentType;
            if (conversationChanged.conversation != null) {
                mStatus = conversationChanged.conversation.getServer().getStatus();
            }
            updateUserListVisibility();
        }

        @Subscribe
        public void onEvent(final OnCurrentServerStatusChanged statusChanged) {
            mStatus = statusChanged.status;
            updateUserListVisibility();
        }
    };

    private TextView mUserListTextView;

    private View mSlideUpLayout;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    private Callback mCallback;

    private Fragment mCurrentFragment;

    private ActionsFragment mActionsFragment;

    private IgnoreListFragment mIgnoreListFragment;

    private InviteFragment mInviteFragment;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (Callback) activity;
        } catch (final ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.navigation_drawer_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSlidingUpPanelLayout = findById(view, R.id.sliding_up_panel);

        mUserListTextView = findById(getView(), R.id.user_text_view);
        mSlideUpLayout = findById(view, R.id.bottom_panel);

        getBus().registerSticky(mEventHandler);

        mSlideUpLayout.setOnClickListener(v -> {
            if (mSlidingUpPanelLayout.isPanelExpanded()) {
                mSlidingUpPanelLayout.collapsePanel();
            } else {
                mSlidingUpPanelLayout.expandPanel();
            }
        });

        if (savedInstanceState == null) {
            mActionsFragment = new ActionsFragment();
            final UserListFragment userListFragment = new UserListFragment();

            mCurrentFragment = mActionsFragment;
            final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.actions_list_layout, mActionsFragment, "Actions");
            transaction.replace(R.id.user_list_frame_layout, userListFragment);
            transaction.commit();
        } else {
            mCurrentFragment = mActionsFragment;
            mActionsFragment = (ActionsFragment) getChildFragmentManager().findFragmentByTag
                    ("Actions");
            mIgnoreListFragment = (IgnoreListFragment) getChildFragmentManager()
                    .findFragmentByTag("Ignore");
            mInviteFragment = (InviteFragment) getChildFragmentManager()
                    .findFragmentByTag("Invite");
            if (mIgnoreListFragment != null) {
                mCurrentFragment = mIgnoreListFragment;
            } else if (mInviteFragment != null) {
                mCurrentFragment = mInviteFragment;
            }
        }

        if (mIgnoreListFragment == null) {
            mIgnoreListFragment = new IgnoreListFragment();
        }
        if (mInviteFragment == null) {
            mInviteFragment = new InviteFragment();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getBus().unregister(mEventHandler);
    }

    @Override
    public void removeCurrentFragment() {
        mCallback.removeCurrentFragment();
    }

    @Override
    public void closeDrawer() {
        mCallback.closeDrawer();
    }

    @Override
    public void disconnectFromServer() {
        mCallback.disconnectFromServer();
    }

    @Override
    public void reconnectToServer() {
        mCallback.reconnectToServer();
    }

    @Override
    public void onMentionMultipleUsers(final List<ChannelUser> users) {
        mCallback.onMentionMultipleUsers(users);
    }

    @Override
    public void switchToIgnoreFragment() {
        mCurrentFragment = mIgnoreListFragment;
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.actions_list_layout, mIgnoreListFragment, "Ignore").commit();

        updateActionBarForLists();
        updateUserListVisibility();
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void switchToInviteFragment() {
        mCurrentFragment = mInviteFragment;
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.actions_list_layout, mInviteFragment, "Invite").commit();

        updateActionBarForLists();
        updateUserListVisibility();
        getActivity().supportInvalidateOptionsMenu();
    }

    void switchToActionFragment() {
        mCurrentFragment = mActionsFragment;
        getChildFragmentManager().popBackStackImmediate();

        revertActionBarToNormal();
        updateUserListVisibility();
        getActivity().supportInvalidateOptionsMenu();
    }

    public void onDrawerClosed() {
        if (isIgnoreFragmentVisible()) {
            mIgnoreListFragment.saveIgnoreList();
            switchToActionFragment();
        } else if (isInviteFragmentVisible()) {
            switchToActionFragment();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_navigation_drawer_ab, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        final MenuItem item = menu.findItem(R.id.activity_main_ab_actions);
        item.setVisible(item.isVisible() && isActionsFragmentVisible());

        final MenuItem add = menu.findItem(R.id.ignore_list_cab_add);
        add.setVisible(isIgnoreFragmentVisible());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_main_ab_actions:
                mSlidingUpPanelLayout.collapsePanel();
                return true;
            case R.id.activity_main_ab_users:
                handleUserList();
                return true;
            case R.id.ignore_list_cab_add:
                mIgnoreListFragment.addIgnoredUser();
                return true;
        }
        return false;
    }

    public boolean onBackPressed() {
        if (!isActionsFragmentVisible()) {
            if (isIgnoreFragmentVisible()) {
                mIgnoreListFragment.saveIgnoreList();
            }
            switchToActionFragment();
        }
        return !isActionsFragmentVisible();
    }

    @Override
    public void updateUserListVisibility() {
        final int visibility = mStatus == ConnectionStatus.CONNECTED
                && mFragmentType == FragmentType.CHANNEL
                && isActionsFragmentVisible() ? View.VISIBLE : View.GONE;
        mSlideUpLayout.setVisibility(visibility);

        if (visibility == View.VISIBLE) {
            // TODO - change this from casting
            final Channel channel = (Channel) mConversation;
            setUserTextViewText(String.format("%d users", channel.getUsers().size()));
        } else {
            if (mSlidingUpPanelLayout.isPanelExpanded()) {
                // Collapse Pane
                mSlidingUpPanelLayout.collapsePanel();
            }
        }
    }

    @Override
    public void joinMultipleChannels(final Collection<InviteEvent> inviteEvents) {
        for (final InviteEvent event : inviteEvents) {
            mConversation.getServer().getServerCallBus().sendJoin(event.channelName);
        }
    }

    @Override
    public ServiceEventInterceptor getEventInterceptor() {
        return mCallback.getService().getEventHelper(mConversation.getServer());
    }

    private void handleUserList() {
        final boolean userListVisible = mSlidingUpPanelLayout.isPanelExpanded();
        if (mCallback.isDrawerOpen() && userListVisible) {
            mSlidingUpPanelLayout.collapsePanel();
        } else if (!userListVisible) {
            mSlidingUpPanelLayout.expandPanel();
        }
    }

    private void setUserTextViewText(final CharSequence text) {
        mUserListTextView.setText(text);
    }

    private void updateActionBarForLists() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getActionBar()
                .getThemedContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater
                .inflate(R.layout.actionbar_custom_view_done, null);
        findById(customActionBarView, R.id.actionbar_done).setOnClickListener(v -> {
            if (isIgnoreFragmentVisible()) {
                mIgnoreListFragment.saveIgnoreList();
            }
            switchToActionFragment();
        });
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE
        );
        actionBar.setCustomView(customActionBarView);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void revertActionBarToNormal() {
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE,
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE |
                        ActionBar.DISPLAY_SHOW_CUSTOM
        );
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private boolean isActionsFragmentVisible() {
        return mActionsFragment == mCurrentFragment;
    }

    private boolean isIgnoreFragmentVisible() {
        return mIgnoreListFragment == mCurrentFragment;
    }

    private boolean isInviteFragmentVisible() {
        return mInviteFragment == mCurrentFragment;
    }

    public interface Callback {

        public void removeCurrentFragment();

        public void disconnectFromServer();

        public boolean isDrawerOpen();

        public void closeDrawer();

        public IRCService getService();

        public void onMentionMultipleUsers(List<ChannelUser> users);

        public void reconnectToServer();
    }
}
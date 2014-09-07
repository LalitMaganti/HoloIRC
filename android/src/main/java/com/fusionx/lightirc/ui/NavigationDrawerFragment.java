package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.service.ServiceEventInterceptor;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

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

import java.util.Collections;
import java.util.List;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.ChannelUser;
import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.event.server.InviteEvent;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class NavigationDrawerFragment extends Fragment implements
        ActionsFragment.Callbacks, UserListFragment.Callback, InviteFragment.Callbacks,
        DCCPendingFragment.Callbacks {

    private final EventHandler mEventHandler = new EventHandler();

    private ConnectionStatus mStatus;

    private FragmentType mFragmentType;

    private Conversation mConversation;

    private TextView mUserListTextView;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    private Callback mCallback;

    private ServiceEventInterceptor mEventHelper;

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

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_up_panel);
        mUserListTextView = (TextView) view.findViewById(R.id.user_text_view);

        getBus().registerSticky(mEventHandler);

        final View dragView = view.findViewById(R.id.drag_view);
        dragView.setOnClickListener(v -> {
            if (mSlidingUpPanelLayout.isPanelExpanded()) {
                mSlidingUpPanelLayout.collapsePanel();
            } else {
                mSlidingUpPanelLayout.expandPanel();
            }
        });

        if (savedInstanceState == null) {
            final ActionsFragment actionsFragment = new ActionsFragment();
            final UserListFragment userListFragment = new UserListFragment();

            final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.actions_list_layout, actionsFragment, "Actions");
            transaction.replace(R.id.user_list_frame_layout, userListFragment);
            transaction.commit();
        }
        updateUserListVisibility();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_navigation_drawer_ab, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        final MenuItem item = menu.findItem(R.id.activity_main_ab_actions);
        item.setVisible(item.isVisible());

        final MenuItem users = menu.findItem(R.id.activity_main_ab_users);
        users.setVisible(item.isVisible() && mFragmentType == FragmentType.CHANNEL);
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
        }
        return false;
    }

    @Override
    public void updateUserListVisibility() {
        final boolean visibility = mStatus == ConnectionStatus.CONNECTED
                && mFragmentType == FragmentType.CHANNEL;

        if (visibility) {
            // TODO - change this from casting
            final Channel channel = (Channel) mConversation;
            setUserTextViewText(String.format("%d users", channel.getUsers().size()));

            mSlidingUpPanelLayout.showPanel();
        } else {
            if (mSlidingUpPanelLayout.isPanelExpanded()) {
                // Collapse Pane
                mSlidingUpPanelLayout.collapsePanel();
            }
            mSlidingUpPanelLayout.hidePanel();
        }
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void acceptInviteEvents(final InviteEvent event) {
        getEventHelper().acceptInviteEvents(Collections.singletonList(event));
    }

    @Override
    public void declineInviteEvents(final InviteEvent event) {
        getEventHelper().declineInviteEvents(Collections.singletonList(event));
    }

    @Override
    public ServiceEventInterceptor getEventHelper() {
        return mEventHelper;
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

    public interface Callback {

        public void removeCurrentFragment();

        public void disconnectFromServer();

        public boolean isDrawerOpen();

        public void closeDrawer();

        public IRCService getService();

        public void onMentionMultipleUsers(List<ChannelUser> users);

        public void reconnectToServer();
    }

    private class EventHandler {

        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            mConversation = conversationChanged.conversation;
            mFragmentType = conversationChanged.fragmentType;
            if (conversationChanged.conversation == null) {
                mEventHelper = null;
            } else {
                final Server server = conversationChanged.conversation.getServer();
                mStatus = server.getStatus();
                mEventHelper = mCallback.getService().getEventHelper(server);
            }
            updateUserListVisibility();
        }

        @Subscribe
        public void onEvent(final OnCurrentServerStatusChanged statusChanged) {
            mStatus = statusChanged.status;
            updateUserListVisibility();
        }
    }
}
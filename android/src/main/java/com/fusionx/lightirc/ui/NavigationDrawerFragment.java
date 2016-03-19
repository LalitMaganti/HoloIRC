package com.fusionx.lightirc.ui;

import android.content.Context;
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

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnServerStatusChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.ChannelUser;
import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class NavigationDrawerFragment extends Fragment implements
        ActionsFragment.Callbacks, UserListFragment.Callback {

    private final EventHandler mEventHandler = new EventHandler();

    private FragmentType mFragmentType;

    private Conversation mConversation;

    private TextView mUserListTextView;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    private Callback mCallback;

    private UserListFragment mUserListFragment;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        try {
            mCallback = (Callback) context;
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
                onUserPanelNotVisible();
            } else {
                mSlidingUpPanelLayout.expandPanel();
            }
        });

        mSlidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {
            @Override
            public void onPanelCollapsed(View view) {
                onUserPanelNotVisible();
            }
        });

        if (savedInstanceState == null) {
            final ActionsFragment actionsFragment = new ActionsFragment();
            mUserListFragment = new UserListFragment();

            final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.actions_list_layout, actionsFragment, "Actions");
            transaction.replace(R.id.user_list_frame_layout, mUserListFragment);
            transaction.commit();
        } else {
            mUserListFragment = (UserListFragment) getChildFragmentManager()
                    .findFragmentById(R.id.user_list_frame_layout);
        }
        updateUserListVisibility();
    }

    public void onUserPanelNotVisible() {
        mUserListFragment.finishCab();
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
                onUserPanelNotVisible();
                return true;
            case R.id.activity_main_ab_users:
                handleUserList();
                return true;
        }
        return false;
    }

    @Override
    public void updateUserListVisibility() {
        final boolean visibility = mConversation != null
                && mConversation.getServer().getStatus() == ConnectionStatus.CONNECTED
                && mFragmentType == FragmentType.CHANNEL;

        if (visibility) {
            // TODO - change this from casting
            final Channel channel = (Channel) mConversation;
            setUserTextViewText(getResources().getQuantityString(R.plurals.user,
                    channel.getUsers().size(), channel.getUsers().size()));

            mSlidingUpPanelLayout.showPanel();
        } else {
            mSlidingUpPanelLayout.collapsePanel();
            onUserPanelNotVisible();
            mSlidingUpPanelLayout.hidePanel();
        }
        getActivity().supportInvalidateOptionsMenu();
    }

    private void handleUserList() {
        final boolean userListVisible = mSlidingUpPanelLayout.isPanelExpanded();
        if (mCallback.isDrawerOpen() && userListVisible) {
            mSlidingUpPanelLayout.collapsePanel();
            onUserPanelNotVisible();
        } else if (!userListVisible) {
            mSlidingUpPanelLayout.expandPanel();
        }
    }

    private void setUserTextViewText(final CharSequence text) {
        mUserListTextView.setText(text);
    }

    public interface Callback {

        void removeCurrentFragment();

        void disconnectFromServer();

        boolean isDrawerOpen();

        void closeDrawer();

        void onMentionMultipleUsers(List<ChannelUser> users);

        void reconnectToServer();
    }

    private class EventHandler {

        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            mConversation = conversationChanged.conversation;
            mFragmentType = conversationChanged.fragmentType;
            updateUserListVisibility();
        }

        @Subscribe
        public void onEvent(final OnServerStatusChanged statusChanged) {
            updateUserListVisibility();
        }
    }
}
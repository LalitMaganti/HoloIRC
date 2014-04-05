package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.Server;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.interfaces.Conversation;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static com.fusionx.lightirc.util.UIUtils.findById;

public class NavigationDrawerFragment extends Fragment implements IgnoreListFragment
        .IgnoreListCallback, ActionsFragment.Callbacks, UserListFragment.Callback {

    private ConnectionStatus mStatus;

    private FragmentType mFragmentType;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    private ActionsFragment mActionFragment;

    private IgnoreListFragment mIgnoreListFragment;

    private Callback mCallback;

    private UserListFragment mUserFragment;

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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.navigation_drawer_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSlidingUpPanelLayout = findById(view, R.id.sliding_up_panel);
        mSlidingUpPanelLayout.setSlidingEnabled(false);

        findById(getView(), R.id.bottom_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSlidingUpPanelLayout.isExpanded()) {
                    mSlidingUpPanelLayout.collapsePane();
                    mUserFragment.onPanelClosed();
                } else {
                    // This is guaranteed to be a Channel so while casting is ugly, it is correct
                    mUserFragment.onMenuOpened((Channel) mCallback.getConversation());
                    mSlidingUpPanelLayout.expandPane();
                }
            }
        });

        if (savedInstanceState == null) {
            mUserFragment = new UserListFragment();
            mActionFragment = new ActionsFragment();

            final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.actions_list_layout, mActionFragment, "Actions");
            transaction.replace(R.id.user_list_frame_layout, mUserFragment);
            transaction.commit();
        } else {
            mUserFragment = (UserListFragment) getChildFragmentManager().findFragmentById(R.id
                    .user_list_frame_layout);
            mActionFragment = (ActionsFragment) getChildFragmentManager().findFragmentByTag(
                    "Actions");
            mIgnoreListFragment = (IgnoreListFragment) getChildFragmentManager().findFragmentByTag(
                    "Ignore");
            refreshUserList();
        }

        if (mIgnoreListFragment == null) {
            mIgnoreListFragment = new IgnoreListFragment();
        }
    }

    @Override
    public void switchToIRCActionFragment() {
        getChildFragmentManager().popBackStackImmediate();
    }

    @Override
    public String getServerTitle() {
        return getServer().getTitle();
    }

    @Override
    public void onRemoveCurrentFragment() {
        mCallback.onRemoveCurrentFragment();
    }

    @Override
    public void onUserMention(List<WorldUser> users) {

    }

    @Override
    public Server getServer() {
        final Conversation conversation = mCallback.getConversation();
        return conversation == null ? null : conversation.getServer();
    }

    @Override
    public boolean isDrawerOpen() {
        return false;
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
    public void switchToIgnoreFragment() {
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.actions_list_layout, mIgnoreListFragment, "Ignore").commit();
    }

    public void onDrawerClosed() {
        mIgnoreListFragment.finishActionMode();
    }

    public void onFragmentTypeChanged(final FragmentType type) {
        mFragmentType = type;
        // If this is null then we are rotating and the actions fragment takes care of its own
        // saving
        if (mActionFragment != null) {
            mActionFragment.onFragmentTypeChanged(type);
            refreshUserList();
        }
    }

    public void onConnectionStatusChanged(final ConnectionStatus status) {
        mStatus = status;
        // If this is null then we are rotating and the actions fragment takes care of its own
        // saving
        if (mActionFragment != null) {
            mActionFragment.onConnectionStatusChanged(status);
            refreshUserList();
        }
    }

    private void refreshUserList() {
        final int visibility = mStatus == ConnectionStatus.CONNECTED
                && mFragmentType == FragmentType.CHANNEL ? View.VISIBLE : View.GONE;
        findById(getView(), R.id.bottom_panel).setVisibility(visibility);

        if (visibility == View.VISIBLE) {
            // TODO - change this from casting
            final Channel channel = (Channel) mCallback.getConversation();
            final TextView textView = findById(getView(), R.id.user_text_view);
            textView.setText(String.format("%d users", channel.getUsers().size()));
        }

        // Collapse Pane
        mSlidingUpPanelLayout.collapsePane();
        mUserFragment.onPanelClosed();
    }

    public interface Callback {

        public void onRemoveCurrentFragment();

        public Conversation getConversation();

        public void disconnectFromServer();

        public void closeDrawer();
    }
}
package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ConnectionStatus;
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

import de.greenrobot.event.EventBus;

import static com.fusionx.lightirc.util.UIUtils.findById;

public class NavigationDrawerFragment extends Fragment implements IgnoreListFragment
        .IgnoreListCallback, ActionsFragment.Callbacks, UserListFragment.Callback {

    private ConnectionStatus mStatus;

    private FragmentType mFragmentType;

    private Conversation mConversation;

    private final Object mEventHandler = new Object() {
        @SuppressWarnings("unused")
        public void onEvent(final OnConversationChanged conversationChanged) {
            mConversation = conversationChanged.conversation;
            mFragmentType = conversationChanged.fragmentType;
            refreshUserList();
        }

        @SuppressWarnings("unused")
        public void onEvent(final OnCurrentServerStatusChanged statusChanged) {
            mStatus = statusChanged.status;
            refreshUserList();
        }
    };

    private TextView mTextView;

    private View mSlideUpLayout;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    private IgnoreListFragment mIgnoreListFragment;

    private Callback mCallback;

    private UserListFragment mUserListFragment;

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

        mTextView = findById(getView(), R.id.user_text_view);
        mSlideUpLayout = findById(view, R.id.bottom_panel);

        EventBus.getDefault().registerSticky(mEventHandler);

        mSlideUpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSlidingUpPanelLayout.isExpanded()) {
                    mSlidingUpPanelLayout.collapsePane();
                } else {
                    mSlidingUpPanelLayout.expandPane();
                }
            }
        });

        if (savedInstanceState == null) {
            mUserListFragment = new UserListFragment();

            final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.actions_list_layout, new ActionsFragment(), "Actions");
            transaction.replace(R.id.user_list_frame_layout, mUserListFragment);
            transaction.commit();
        } else {
            mUserListFragment = (UserListFragment) getChildFragmentManager().findFragmentById(R.id
                    .user_list_frame_layout);
            mIgnoreListFragment = (IgnoreListFragment) getChildFragmentManager().findFragmentByTag(
                    "Ignore");
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
    public void onRemoveCurrentFragment() {
        mCallback.removeCurrentFragment();
    }

    @Override
    public void onUserMention(final List<WorldUser> users) {
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

    private void refreshUserList() {
        final int visibility = mStatus == ConnectionStatus.CONNECTED
                && mFragmentType == FragmentType.CHANNEL ? View.VISIBLE : View.GONE;
        mSlideUpLayout.setVisibility(visibility);

        if (visibility == View.VISIBLE) {
            // TODO - change this from casting
            final Channel channel = (Channel) mConversation;
            mTextView.setText(String.format("%d users", channel.getUsers().size()));
        }
        if (mSlidingUpPanelLayout.isExpanded()) {
            // Collapse Pane
            mSlidingUpPanelLayout.collapsePane();
        }
    }

    public interface Callback {

        public void removeCurrentFragment();

        public void disconnectFromServer();

        public void closeDrawer();
    }
}
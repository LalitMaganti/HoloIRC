package com.fusionx.lightirc.ui;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCAdapter;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.relay.ChannelUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.ConnectedEvent;
import com.fusionx.relay.event.JoinEvent;
import com.fusionx.relay.event.KickEvent;
import com.fusionx.relay.event.NickInUseEvent;
import com.fusionx.relay.event.PartEvent;
import com.fusionx.relay.event.PrivateActionEvent;
import com.fusionx.relay.event.PrivateMessageEvent;
import com.fusionx.relay.event.SwitchToServerEvent;
import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class IRCPagerFragment extends Fragment implements ServerFragment.ServerFragmentCallback,
        ChannelFragment.ChannelFragmentCallback, UserFragment.UserFragmentCallback {

    private ViewPager mViewPager = null;

    private IRCPagerInterface mCallback = null;

    private IRCAdapter mAdapter = null;

    /**
     * Hold a reference to the parent Activity so we can report the task's current progress and
     * results. The Android framework will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (IRCPagerInterface) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement IRCPagerInterface");
        }
    }

    /**
     * Since the fragment is retained, when the activity detaches, a new activity is created so null
     * the callback when the old activity detaches
     */
    @Override
    public void onDetach() {
        super.onDetach();

        mCallback = null;
    }

    /**
     * Retain the fragment through config changes
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCallback.getServer().getServerEventBus().unregister(this);
    }

    /**
     * Create the view by inflating a generic view pager
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_pager, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = (ViewPager) getView().findViewById(R.id.pager);

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) getActivity().findViewById(R.id
                .pager_tabs);
        if (mAdapter == null) {
            mAdapter = new IRCAdapter(getChildFragmentManager(), tabs);
        }
        mViewPager.setAdapter(mAdapter);
        tabs.setViewPager(mViewPager);

        final TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]
                {android.R.attr.windowBackground});
        final int background = a.getResourceId(0, 0);
        mViewPager.setBackgroundResource(background);
    }

    /**
     * Creates the ServerFragment object
     */
    public void createServerFragment(final String serverTitle) {
        if (mAdapter.getCount() == 0) {
            mAdapter.onNewFragment(serverTitle, FragmentTypeEnum.Server);
            //mAdapter.addServerFragment(serverTitle);
        }
    }

    /**
     * Creates a UserFragment with the specified nick
     *
     * @param userNick - the nick of the user we are PMing
     */
    public void onCreateMessageFragment(final String userNick, final boolean switchToTab) {
        final int position = mAdapter.onNewFragment(userNick, FragmentTypeEnum.User);

        if (switchToTab) {
            mViewPager.setCurrentItem(position, true);
        }
    }

    /**
     * Selects the ServerFragment regardless of what is currently selected in the ViewPager
     */
    void switchToServerFragment() {
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0, true);
        }
    }

    /**
     * If the currently displayed fragment is the one being removed then switch to one tab back.
     * Then remove the fragment regardless.
     *
     * @param fragmentTitle - name of the fragment to be removed
     */
    public void onRemoveFragment(final String fragmentTitle) {
        final int index = mAdapter.getIndexFromTitle(fragmentTitle);
        if (fragmentTitle.equals(getCurrentTitle())) {
            mViewPager.setCurrentItem(index - 1, true);
        }
        final IRCFragment fragment = mAdapter.getRegisteredFragment(index);
        if (fragment != null) {
            fragment.setCachingImportant(false);
        }
        mAdapter.onRemoveFragment(index);
    }

    @Override
    public boolean isConnectedToServer() {
        return mCallback.isConnectedToServer();
    }

    /**
     * Method called when a new ChannelFragment is to be created
     *
     * @param channelName - name of the channel joined
     * @param forceSwitch - whether the channel should be forcibly switched to
     */
    public void onCreateChannelFragment(final String channelName, final boolean forceSwitch) {
        final String mention = getActivity().getIntent().getStringExtra("mention");
        final boolean switchToTab = channelName.equals(mention) || forceSwitch;

        final int position = mAdapter.onNewFragment(channelName, FragmentTypeEnum.Channel);

        if (switchToTab) {
            mViewPager.setCurrentItem(position, true);
        }
    }

    public void onMentionRequested(final List<ChannelUser> users) {
        if (FragmentTypeEnum.Channel.equals(getCurrentType())) {
            final ChannelFragment channel = (ChannelFragment) mAdapter
                    .getRegisteredFragment(mViewPager.getCurrentItem());
            channel.onUserMention(users);
        }
    }

    public void onUnexpectedDisconnect() {
        mAdapter.onUnexpectedDisconnect();

        mViewPager.setCurrentItem(0, true);
    }

    public String getCurrentTitle() {
        return mAdapter.getRegisteredFragment(mViewPager.getCurrentItem()).getTitle();
    }

    public FragmentTypeEnum getCurrentType() {
        // Since the activity waits for a callback to be received from the service before adding
        // the ServerFragment we may end up not having a fragment in the adapter by the time the
        // options menu is prepare - return null in this case
        final IRCFragment fragment = mAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        if (fragment != null) {
            return fragment.getType();
        } else {
            return null;
        }
    }

    @Override
    public Server getServer() {
        return mCallback.getServer();
    }

    // Subscribe events start here
    @Subscribe
    public void onChannelPart(final PartEvent event) {
        onRemoveFragment(event.channelName);
    }

    @Subscribe
    public void onChannelJoin(final JoinEvent event) {
        onCreateChannelFragment(event.channelToJoin, true);
    }

    @Subscribe
    public void onKicked(final KickEvent event) {
        onRemoveFragment(event.channelName);
        switchToServerFragment();
    }

    @Subscribe
    public void onPrivateMessage(final PrivateMessageEvent event) {
        if (event.newPrivateMessage) {
            onCreateMessageFragment(event.userNick, true);
        }
    }

    @Subscribe
    public void onPrivateAction(final PrivateActionEvent event) {
        if (event.newPrivateMessage) {
            onCreateMessageFragment(event.userNick, true);
        }
    }

    @Subscribe
    public void onSwitchToServer(final SwitchToServerEvent event) {
        switchToServerFragment();
    }

    @Subscribe
    public void onNickInUse(final NickInUseEvent event) {
        switchToServerFragment();
    }

    @Subscribe
    public void onServerConnected(final ConnectedEvent event) {
        final ServerFragment fragment = (ServerFragment) mAdapter.getRegisteredFragment(0);
        fragment.onConnected();
    }
    // Subscribe events end here

    // Interface for callbacks
    public interface IRCPagerInterface {

        public Server getServer();

        public boolean isConnectedToServer();
    }
}
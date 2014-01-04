package com.fusionx.lightirc.ui;

import com.astuetz.PagerSlidingTabStrip;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCAdapter;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.relay.Server;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.event.SwitchToPrivateMessage;
import com.fusionx.relay.event.channel.MentionEvent;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.ImportantServerEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.KickEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.user.WorldPrivateEvent;
import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class IRCPagerFragment extends Fragment implements ServerFragment.Callbacks,
        ChannelFragment.Callbacks, UserFragment.Callbacks {

    private ViewPager mViewPager;

    private Callbacks mCallbacks;

    private IRCAdapter mAdapter;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (Callbacks) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement IRCPagerFragment"
                    + ".Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_pager, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = (ViewPager) getView().findViewById(R.id.pager);

        final TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]
                {android.R.attr.windowBackground});
        final int background = a.getResourceId(0, 0);
        mViewPager.setBackgroundResource(background);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCallbacks.getServer().getServerEventBus().unregister(this);
    }

    /*
     * Since the fragment is retained, when the activity detaches, a new activity is created so null
     * the callback when the old activity detaches
     */
    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = null;
    }

    // Don't setup the adapter or the tab strip until we know we're going to add a server fragment
    // This is to stop an issue in PagerSlidingTabStrip
    public void onCreateServerFragment(final String serverTitle) {
        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) getActivity()
                .findViewById(R.id.pager_tabs);
        if (mAdapter == null) {
            mAdapter = new IRCAdapter(getChildFragmentManager(), tabs);
            mAdapter.onNewFragment(serverTitle, FragmentTypeEnum.Server);
        }
        mViewPager.setAdapter(mAdapter);
        tabs.setViewPager(mViewPager);
    }

    public void onCreateMessageFragment(final String userNick, final boolean switchToTab) {
        final int position = mAdapter.onNewFragment(userNick, FragmentTypeEnum.User);

        if (switchToTab) {
            mViewPager.setCurrentItem(position, true);
        }
    }

    void switchToServerFragment() {
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0, true);
        }
    }

    public void onRemoveFragment(final String fragmentTitle) {
        final int index = mAdapter.getIndexFromTitle(fragmentTitle);
        if (fragmentTitle.equals(getCurrentTitle())) {
            mViewPager.setCurrentItem(index - 1, true);
        }
        mAdapter.onRemoveFragment(index);
    }

    @Override
    public boolean isConnectedToServer() {
        return mCallbacks.isConnectedToServer();
    }

    public void onCreateChannelFragment(final String channelName, final boolean forceSwitch) {
        final String mention = getActivity().getIntent().getStringExtra("mention");
        final boolean switchToTab = channelName.equals(mention) || forceSwitch;

        final int position = mAdapter.onNewFragment(channelName, FragmentTypeEnum.Channel);

        if (switchToTab) {
            mViewPager.setCurrentItem(position, true);
        }
    }

    public void onMentionRequested(final List<WorldUser> users) {
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
        return mCallbacks.getServer();
    }

    // Subscribe events start here
    @Subscribe
    public void onChannelPart(final PartEvent event) {
        onRemoveFragment(event.channelName);
    }

    @Subscribe
    public void onChannelJoin(final JoinEvent event) {
        onCreateChannelFragment(event.channelName, true);
    }

    @Subscribe
    public void onKicked(final KickEvent event) {
        onRemoveFragment(event.channelName);
        switchToServerFragment();
    }

    @Subscribe
    public void onSwitchToPrivateMessage(final SwitchToPrivateMessage event) {
        final int index = mAdapter.getIndexFromTitle(event.nick);
        if (index == PagerAdapter.POSITION_NONE) {
            onCreateMessageFragment(event.nick, true);
        } else {
            mViewPager.setCurrentItem(index);
        }
    }

    @Subscribe
    public void onPrivateMessage(final WorldPrivateEvent event) {
        if (!getCurrentTitle().equals(event.user.getNick())) {
            final String message = String.format(getString(R.string.activity_pm),
                    event.user.getNick());
            final Configuration.Builder builder = new Configuration.Builder();
            builder.setDuration(2000);
            final Crouton crouton = Crouton.makeText(getActivity(), message,
                    Style.INFO).setConfiguration(builder.build());
            crouton.show();
        }
    }

    @Subscribe
    public void onPrivateMessage(final MentionEvent event) {
        if (!getCurrentTitle().equals(event.channelName)) {
            final String message = String.format(getString(R.string.activity_mentioned),
                    event.channelName);
            final Configuration.Builder builder = new Configuration.Builder();
            builder.setDuration(2000);
            final Crouton crouton = Crouton.makeText(getActivity(), message,
                    Style.INFO).setConfiguration(builder.build());
            crouton.show();
        }
    }

    @Subscribe
    public void onImportantServerEvent(final ImportantServerEvent event) {
        switchToServerFragment();
    }

    @Subscribe
    public void onServerConnected(final ConnectEvent event) {
        final ServerFragment fragment = (ServerFragment) mAdapter.getRegisteredFragment(0);
        fragment.onConnected();
    }
    // Subscribe events end here

    // Interface for callbacks
    public interface Callbacks {

        public Server getServer();

        public boolean isConnectedToServer();
    }
}
package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCAdapter;
import com.fusionx.lightirc.constants.FragmentType;
import com.fusionx.lightirc.model.FragmentStorage;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Server;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.event.SwitchToPrivateMessage;
import com.fusionx.relay.event.channel.MentionEvent;
import com.fusionx.relay.event.channel.WorldMessageEvent;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.ImportantServerEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.KickEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.user.WorldPrivateEvent;
import com.fusionx.slidingtabs.view.ViewPagerSlidingTabLayout;
import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import static butterknife.ButterKnife.findById;

public class IRCPagerFragment extends Fragment implements IRCFragment.Callback {

    private final static String ADAPTER_STORAGE = "ADAPTER_STROAGE";

    @InjectView(R.id.pager)
    protected ViewPager mViewPager;

    private ViewPagerSlidingTabLayout mSlidingTabLayout;

    private Callbacks mCallbacks;

    private IRCAdapter mAdapter;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (Callbacks) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement IRCPagerFragment"
                    + ".Callback");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(ADAPTER_STORAGE, mAdapter.getFragments());
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Register when we return to this and there is a saved state
        if (savedInstanceState != null) {
            mCallbacks.getServer().getServerEventBus().register(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_pager, container);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);
        UIUtils.setWindowBackgroundOnView(getActivity(), mViewPager);

        mSlidingTabLayout = findById(getActivity(), R.id.pager_tabs);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mAdapter = new IRCAdapter(getChildFragmentManager(), mViewPager);

            final ArrayList<FragmentStorage> list = savedInstanceState.getParcelableArrayList
                    (ADAPTER_STORAGE);
            mAdapter.setFragmentList(list);

            mViewPager.setAdapter(mAdapter);
            mViewPager.setOnPageChangeListener(mCallbacks.getPagerChangeListener());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
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
        if (mAdapter == null) {
            mAdapter = new IRCAdapter(getChildFragmentManager(), mViewPager);
            mAdapter.onNewFragment(serverTitle, FragmentType.SERVER);
        }
        mSlidingTabLayout.setTabAdapter(mAdapter);

        // The view pager's adapter needs to be set before the TabStrip is assigned
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(mCallbacks.getPagerChangeListener());
    }

    public void onCreateMessageFragment(final String userNick, final boolean switchToTab) {
        final int position = mAdapter.onNewFragment(userNick, FragmentType.USER);

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

    public void onCreateChannelFragment(final String channelName, final boolean forceSwitch) {
        // The channel may already exist and be using a snapshot - check this first
        int index = -1;
        ArrayList<FragmentStorage> fragments = mAdapter.getFragments();
        for (int i = 0, fragmentsSize = fragments.size(); i < fragmentsSize; i++) {
            final FragmentStorage storage = fragments.get(i);
            if (storage.getFragmentType() == FragmentType.CHANNEL
                    && storage.getTitle().equals(channelName)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            final IRCFragment fragment = mAdapter.getRegisteredFragment(index);
            if (fragment != null) {
                fragment.onResetBuffer();
            }
        } else {
            final String mention = getActivity().getIntent().getStringExtra("mention");
            final boolean switchToTab = channelName.equals(mention) || forceSwitch;

            final int position = mAdapter.onNewFragment(channelName, FragmentType.CHANNEL);

            if (switchToTab) {
                mViewPager.setCurrentItem(position, true);
            }
        }
    }

    public void onMentionRequested(final List<WorldUser> users) {
        if (FragmentType.CHANNEL.equals(getCurrentType())) {
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

    public FragmentType getCurrentType() {
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
    public void onChannelEvent(final WorldMessageEvent event) {
        //mAdapter.updateTabTitleColour(event);
    }

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
    public void onMentioned(final MentionEvent event) {
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
        mAdapter.onConnected();
    }
    // Subscribe events end here

    public void onPageScrolled(int position, float positionOffset) {
        mSlidingTabLayout.onPageScrolled(position, positionOffset);
    }

    // Interface for callbacks
    public interface Callbacks {

        public Server getServer();

        public boolean isConnectedToServer();

        public ViewPager.OnPageChangeListener getPagerChangeListener();
    }
}
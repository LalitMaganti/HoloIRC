package com.fusionx.lightirc.fragments.ircfragments;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.fusionx.irc.ChannelUser;
import com.fusionx.irc.Server;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.ui.IRCViewPager;

import java.util.ArrayList;

public class IRCPagerFragment extends Fragment implements ServerFragment
        .ServerFragmentCallback, ChannelFragment.ChannelFragmentCallback {
    private IRCViewPager mViewPager = null;
    private IRCPagerInterface mCallback = null;
    private IRCPagerAdapter mAdapter = null;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = (IRCPagerInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallback = null;
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

    public void createServerFragment() {
        if (mAdapter == null) {
            mAdapter = new IRCPagerAdapter(getChildFragmentManager());
            mAdapter.addServerFragment(mCallback.getServerTitle());
        }
        final TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]
                {android.R.attr.windowBackground});
        final int background = a.getResourceId(0, 0);
        mViewPager = (IRCViewPager) getView().findViewById(R.id.pager);
        mViewPager.setBackgroundResource(background);
        mViewPager.setAdapter(mAdapter);
    }

    private IRCFragment getCurrentItem() {
        return mAdapter.getItem(mViewPager.getCurrentItem());
    }

    public void createPMFragment(final String userNick) {
        mViewPager.onNewPrivateMessage(userNick);
    }

    /**
     * Selects the ServerFragment regardless of what is currently selected
     */
    public void selectServerFragment() {
        mViewPager.setCurrentItem(0, true);
    }

    /**
     * If the currently displayed fragment is the one being removed then switch
     * to one tab back. Then remove the fragment regardless.
     *
     * @param fragmentTitle - name of the fragment to be removed
     */
    public void switchFragmentAndRemove(final String fragmentTitle) {
        final int index = mAdapter.getIndexFromTitle(fragmentTitle);
        if (fragmentTitle.equals(getCurrentTitle())) {
            mViewPager.setCurrentItem(index - 1, true);
        }
        mAdapter.removeFragment(index);
    }

    /**
     * Method called when a new ChannelFragment is to be created
     *
     * @param channelName - name of the channel joined
     * @param forceSwitch - whether the channel should be forcibly switched to
     */
    public void createChannelFragment(final String channelName, final boolean forceSwitch) {
        final boolean switchToTab = channelName.equals(getActivity().getIntent().getStringExtra
                ("mention")) || forceSwitch;
        mViewPager.createChannelFragment(channelName, switchToTab);
    }

    public Handler getFragmentHandler(final String destination, final FragmentType type) {
        final String nonNullDestination = destination != null ? destination : mCallback
                .getServerTitle();
        if (mAdapter != null) {
            final IRCFragment fragment = mAdapter.getFragment(nonNullDestination,
                    type);
            return fragment == null ? null : fragment.getHandler();
        } else {
            return null;
        }
    }

    public void onMentionRequested(final ArrayList<ChannelUser> users) {
        if (getCurrentType().equals(FragmentType.Channel)) {
            final ChannelFragment channel = (ChannelFragment) getCurrentItem();
            channel.onUserMention(users);
        }
    }

    public void onUnexpectedDisconnect() {
        mViewPager.setCurrentItem(0, true);

        mAdapter.removeAllButServer();
        mAdapter.disableAllEditTexts();
    }

    public String getCurrentTitle() {
        return getCurrentItem().getTitle();
    }

    public FragmentType getCurrentType() {
        return getCurrentItem().getType();
    }

    public void setCurrentItemIndex(final int position) {
        mAdapter.setCurrentItemIndex(position);
    }

    public void setTabStrip(PagerSlidingTabStrip tabs) {
        tabs.setViewPager(mViewPager);
        mAdapter.setTabStrip(tabs);
    }

    @Override
    public void updateUserList(String channelName) {
        mCallback.updateUserList(channelName);
    }

    @Override
    public Server getServer(boolean nullAllowed) {
        return mCallback.getServer(nullAllowed);
    }

    public void connectedToServer() {
        final ServerFragment fragment = (ServerFragment) mAdapter.getFragment(mCallback
                .getServerTitle(), FragmentType.Server);
        fragment.onConnectedToServer();
    }

    public void writeMessageToServer(final String message) {
        final ServerFragment fragment = (ServerFragment) mAdapter.getFragment(mCallback
                .getServerTitle(), FragmentType.Server);
        fragment.appendToTextView(message + "\n");
    }

    public interface IRCPagerInterface {
        public String getServerTitle();

        public void updateUserList(String channelName);

        public Server getServer(boolean nullAllowed);
    }
}
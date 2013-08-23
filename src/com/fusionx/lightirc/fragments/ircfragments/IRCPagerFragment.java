package com.fusionx.lightirc.fragments.ircfragments;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.ui.IRCViewPager;

public class IRCPagerFragment extends Fragment {
    private IRCViewPager mViewPager = null;
    private CommonCallbacks mCallback;
    private IRCPagerAdapter mAdapter;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = (CommonCallbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    public IRCFragment getCurrentItem() {
        return mAdapter.getItem(mViewPager.getCurrentItem());
    }

    public void createPMFragment(final String userNick) {
        mViewPager.onNewPrivateMessage(userNick);
    }

    public void selectServerFragment() {
        mViewPager.setCurrentItem(0, true);
    }

    public void switchFragmentAndRemove(final String fragmentTitle) {
        final int index = mAdapter.getIndexFromTitle(fragmentTitle);
        if (getCurrentItem().getTitle().equals(fragmentTitle)) {
            mViewPager.setCurrentItem(index - 1, true);
        }
        mAdapter.removeFragment(index);
    }

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

    public IRCViewPager getViewPager() {
        return mViewPager;
    }

    public IRCPagerAdapter getPagerAdapter() {
        return mAdapter;
    }
}
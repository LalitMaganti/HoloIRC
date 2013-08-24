package com.fusionx.lightirc.fragments.actions;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ActionPagerAdapter;
import com.fusionx.lightlibrary.ui.NonSwipableViewPager;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class ActionsPagerFragment extends Fragment implements IgnoreListFragment.IgnoreListCallback {
    private NonSwipableViewPager mActionViewPager;
    private ActionPagerAdapter mActionsPagerAdapter = null;
    private ActionsPagerFragmentCallback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (ActionsPagerFragmentCallback) activity;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mActionsPagerAdapter = new ActionPagerAdapter(getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.non_scrollable_view_pager, container);

        mActionViewPager = (NonSwipableViewPager) view;
        mActionViewPager.setAdapter(mActionsPagerAdapter);

        return view;
    }

    private IRCActionsFragment getActionFragment() {
        return (IRCActionsFragment) mActionsPagerAdapter.getItem(0);
    }

    private IgnoreListFragment getIgnoreFragment() {
        return (IgnoreListFragment) mActionsPagerAdapter.getItem(1);
    }

    public void switchToIgnoreFragment() {
        mActionViewPager.setCurrentItem(1);
        getActivity().startActionMode(getIgnoreFragment());
    }

    @Override
    public void switchToIRCActionFragment() {
        mActionViewPager.setCurrentItem(0);
    }

    @Override
    public String getServerTitle() {
        return mCallback.getServerTitle();
    }

    public SlidingMenu.OnCloseListener getIgnoreFragmentListener() {
        return getIgnoreFragment();
    }

    public SlidingMenu.OnOpenListener getActionFragmentListener() {
        return getActionFragment();
    }

    public void updateConnectionStatus() {
        getActionFragment().updateConnectionStatus();
    }

    public void onPageChanged() {
        getActionFragment().onTabChanged();
    }

    public interface ActionsPagerFragmentCallback {
        public String getServerTitle();
    }
}
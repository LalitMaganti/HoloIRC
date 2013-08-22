package com.fusionx.lightirc.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fusionx.lightirc.fragments.IRCActionsFragment;
import com.fusionx.lightirc.fragments.IgnoreListFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class ActionPagerAdapter extends FragmentStatePagerAdapter {
    private final IRCActionsFragment mActionFragment;
    private final IgnoreListFragment mIgnoreListFragment;

    public ActionPagerAdapter(final FragmentManager fm) {
        super(fm);
        mActionFragment = new IRCActionsFragment();
        mIgnoreListFragment = new IgnoreListFragment();
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return mActionFragment;
            case 1:
                return mIgnoreListFragment;
            default:
                return null;
        }
    }

    public SlidingMenu.OnCloseListener getIgnoreFragmentListener() {
        return mIgnoreListFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
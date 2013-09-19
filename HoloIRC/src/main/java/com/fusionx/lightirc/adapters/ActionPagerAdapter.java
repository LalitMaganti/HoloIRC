package com.fusionx.lightirc.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fusionx.lightirc.ui.ActionsFragment;
import com.fusionx.lightirc.ui.IgnoreListFragment;

public class ActionPagerAdapter extends FragmentPagerAdapter {
    private final ActionsFragment mActionFragment;
    private final IgnoreListFragment mIgnoreListFragment;

    public ActionPagerAdapter(final FragmentManager fm) {
        super(fm);

        mActionFragment = new ActionsFragment();
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

    @Override
    public int getCount() {
        return 2;
    }
}
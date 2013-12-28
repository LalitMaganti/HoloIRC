package com.fusionx.lightirc.adapters;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.ui.ChannelFragment;
import com.fusionx.lightirc.ui.IRCFragment;
import com.fusionx.lightirc.ui.ServerFragment;
import com.fusionx.lightirc.ui.UserFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Pair;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;

public class IRCAdapter extends FragmentStatePagerAdapter {
    private final SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();
    private final ArrayList<Pair<String, FragmentTypeEnum>> mFragmentList = new ArrayList<>();
    private final PagerSlidingTabStrip mTabStrip;

    public IRCAdapter(FragmentManager fm, final PagerSlidingTabStrip tabStrip) {
        super(fm);

        mTabStrip = tabStrip;
    }

    @Override
    public Fragment getItem(final int i) {
        final Pair<String, FragmentTypeEnum> pair = mFragmentList.get(i);

        final Bundle bundle = new Bundle();
        bundle.putString("title", pair.first);

        final Fragment fragment;
        switch (pair.second) {
            case Server:
                fragment = new ServerFragment();
                break;
            case Channel:
                fragment = new ChannelFragment();
                break;
            case User:
                fragment = new UserFragment();
                break;
            default:
                fragment = null;
        }
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public int getItemPosition(final Object object) {
        final IRCFragment fragment = (IRCFragment) object;

        return getIndexFromTitle(fragment.getTitle());
    }

    public int getIndexFromTitle(final String title) {
        for (final Pair<String, FragmentTypeEnum> pair : mFragmentList) {
            if (pair.first.equals(title)) {
                return mFragmentList.indexOf(pair);
            }
        }
        return POSITION_NONE;
    }

    public int onNewFragment(final String title, final FragmentTypeEnum typeEnum) {
        final Pair<String, FragmentTypeEnum> enumPair = new Pair<>(title, typeEnum);
        mFragmentList.add(enumPair);

        notifyDataSetChanged();
        mTabStrip.notifyDataSetChanged();

        return mFragmentList.size() - 1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public void onUnexpectedDisconnect() {
        final Iterator iterator = mFragmentList.iterator();
        // We don't want to mess with the server fragment
        iterator.next();

        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        notifyDataSetChanged();
        mTabStrip.notifyDataSetChanged();

        final ServerFragment fragment = (ServerFragment) mRegisteredFragments.get(0);
        if (fragment != null) {
            fragment.onDisableInput();
        }
    }

    public void onRemoveFragment(final int index) {
        mFragmentList.remove(index);

        notifyDataSetChanged();
        mTabStrip.notifyDataSetChanged();
    }

    // ONLY CALL THIS METHOD IF YOU WANT THE CURRENTLY DISPLAYED FRAGMENT
    public IRCFragment getRegisteredFragment(int position) {
        return (IRCFragment) mRegisteredFragments.get(position);
    }

    // ONLY CALL THIS METHOD IF YOU WANT THE CURRENTLY DISPLAYED FRAGMENT
    @Override
    public CharSequence getPageTitle(final int position) {
        return mFragmentList.get(position).first;
    }
}
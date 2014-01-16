package com.fusionx.lightirc.adapters;

import com.astuetz.PagerSlidingTabStrip;
import com.fusionx.lightirc.constants.FragmentType;
import com.fusionx.lightirc.model.FragmentStorage;
import com.fusionx.lightirc.ui.ChannelFragment;
import com.fusionx.lightirc.ui.IRCFragment;
import com.fusionx.lightirc.ui.ServerFragment;
import com.fusionx.lightirc.ui.UserFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IRCAdapter extends FragmentStatePagerAdapter {

    private final SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();

    private final ArrayList<FragmentStorage> mFragmentList = new ArrayList<>();

    private final PagerSlidingTabStrip mTabStrip;

    public IRCAdapter(final FragmentManager fm, final PagerSlidingTabStrip tabStrip) {
        super(fm);

        mTabStrip = tabStrip;
    }

    @Override
    public Fragment getItem(final int i) {
        final FragmentStorage pair = mFragmentList.get(i);

        final Bundle bundle = new Bundle();
        bundle.putString("title", pair.getTitle());

        final Fragment fragment;
        switch (pair.getFragmentType()) {
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
        for (final FragmentStorage pair : mFragmentList) {
            if (pair.getTitle().equals(title)) {
                return mFragmentList.indexOf(pair);
            }
        }
        return POSITION_NONE;
    }

    public int onNewFragment(final String title, final FragmentType typeEnum) {
        final FragmentStorage enumPair = new FragmentStorage(title, typeEnum);
        mFragmentList.add(enumPair);

        notifyDataSetChanged();
        // We don't want to notify the tab strip because the TabStrip doesn't even know that this
        // is the adapter it's meant to be monitoring - it only knows after the ServerFragment
        // has been added
        if (typeEnum != FragmentType.Server) {
            mTabStrip.notifyDataSetChanged();
        }

        return mFragmentList.size() - 1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Fragment fragment = (Fragment) super.instantiateItem(container, position);
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
        // We don't want to remove the server fragment
        iterator.next();

        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        notifyDataSetChanged();
        mTabStrip.notifyDataSetChanged();
    }

    public void onRemoveFragment(final int index) {
        mFragmentList.remove(index);

        notifyDataSetChanged();
        mTabStrip.notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return mFragmentList.get(position).getTitle();
    }

    public ArrayList<FragmentStorage> getFragments() {
        return mFragmentList;
    }

    public void setFragmentList(final List<FragmentStorage> fragmentList) {
        mFragmentList.clear();
        mFragmentList.addAll(fragmentList);
    }

    // ONLY CALL THIS METHOD IF YOU WANT THE CURRENTLY DISPLAYED FRAGMENT
    public IRCFragment getRegisteredFragment(int position) {
        return (IRCFragment) mRegisteredFragments.get(position);
    }
}
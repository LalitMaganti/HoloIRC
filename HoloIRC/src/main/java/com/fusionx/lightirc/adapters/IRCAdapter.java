package com.fusionx.lightirc.adapters;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.FragmentType;
import com.fusionx.lightirc.model.FragmentStorage;
import com.fusionx.lightirc.ui.ChannelFragment;
import com.fusionx.lightirc.ui.IRCFragment;
import com.fusionx.lightirc.ui.ServerFragment;
import com.fusionx.lightirc.ui.UserFragment;
import com.fusionx.slidingtabs.model.OnTabClickListener;
import com.fusionx.slidingtabs.model.Tab;
import com.fusionx.slidingtabs.model.TabAdapter;
import com.fusionx.slidingtabs.model.TextTab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IRCAdapter extends FragmentStatePagerAdapter implements TabAdapter {

    private final OnTabClickListener mOnClickListener;

    private final SparseArray<IRCFragment> mRegisteredFragments = new SparseArray<>();

    private final ArrayList<FragmentStorage> mFragmentList = new ArrayList<>();

    private final Map<String, Integer> mFragmentIndices = new HashMap<>();

    public IRCAdapter(final FragmentManager fm, final ViewPager pager) {
        super(fm);
        mOnClickListener = new OnTabClickListener() {
            @Override
            public void onClick(int position) {
                pager.setCurrentItem(position);
            }
        };
    }

    @Override
    public Fragment getItem(final int i) {
        final FragmentStorage pair = mFragmentList.get(i);

        final Bundle bundle = new Bundle();
        bundle.putString("title", pair.getTitle());

        final Fragment fragment;
        switch (pair.getFragmentType()) {
            case SERVER:
                fragment = new ServerFragment();
                break;
            case CHANNEL:
                fragment = new ChannelFragment();
                break;
            case USER:
                fragment = new UserFragment();
                break;
            default:
                fragment = null;
        }
        fragment.setArguments(bundle);
        mFragmentIndices.put(pair.getTitle(), i);

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
        final Integer position = mFragmentIndices.get(title);
        if (position == null) {
            return POSITION_NONE;
        }
        return position;
    }

    public int onNewFragment(final String title, final FragmentType typeEnum) {
        final FragmentStorage enumPair = new FragmentStorage(title, typeEnum);
        mFragmentList.add(enumPair);
        notifyDataSetChanged();
        return mFragmentList.size() - 1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, (IRCFragment) fragment);
        return fragment;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public void onConnected() {
        for (int i = 0, size = mRegisteredFragments.size(); i < size; i++) {
            final IRCFragment obj = mRegisteredFragments.valueAt(i);
            obj.onConnected();
        }
    }

    public void onUnexpectedDisconnect() {
        for (int i = 0, size = mRegisteredFragments.size(); i < size; i++) {
            final IRCFragment obj = mRegisteredFragments.valueAt(i);
            obj.onDisconnected();
        }
    }

    public void onRemoveFragment(final int index) {
        final FragmentStorage storage = mFragmentList.remove(index);
        mFragmentIndices.remove(storage.getTitle());
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return mFragmentList.get(position).getTitle();
    }

    public void setFragmentList(final Collection<FragmentStorage> fragmentList) {
        mFragmentList.clear();
        mFragmentList.addAll(fragmentList);
    }

    // ONLY CALL THIS METHOD IF YOU WANT THE CURRENTLY DISPLAYED FRAGMENT
    public IRCFragment getRegisteredFragment(final int position) {
        return mRegisteredFragments.get(position);
    }

    // TabAdapter Stuff
    @Override
    public Tab getTab(int position) {
        final String title = mFragmentList.get(position).getTitle();
        final Tab tab = new TextTab(title, R.layout.tab_layout);
        tab.setTabClickListener(mOnClickListener);
        return tab;
    }

    public ArrayList<FragmentStorage> getFragments() {
        return mFragmentList;
    }
}
/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.adapters;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.fusionx.lightirc.irc.ircfragments.IRCFragment;
import com.fusionx.lightirc.irc.ircfragments.ServerFragment;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.MiscUtils;

import java.util.ArrayList;
import java.util.Iterator;

import lombok.NonNull;
import lombok.Setter;

public class IRCPagerAdapter extends PagerAdapter {
    private static final boolean DEBUG = false;
    private static final String TAG = "FragmentStatePagerAdapter";

    private Fragment mCurrentPrimaryItem = null;
    private FragmentTransaction mCurTransaction = null;

    private final FragmentManager mFragmentManager;
    private final ArrayList<Fragment> mFragments = new ArrayList<>();
    private final ArrayList<Fragment.SavedState> mSavedState = new ArrayList<>();

    private final ArrayList<IRCFragment> views = new ArrayList<>();

    @Setter
    private PagerSlidingTabStrip tabStrip;

    @Setter
    private int currentItemIndex;

    public IRCPagerAdapter(final FragmentManager fm) {
        mFragmentManager = fm;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        Fragment fragment = (Fragment) object;

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) Log.v(TAG, "Removing item #" + position + ": f=" + object
                + " v=" + ((Fragment) object).getView());
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }
        mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
        mFragments.set(position, null);

        mCurTransaction.remove(fragment);
    }

    @Override
    public void finishUpdate(final ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    /**
     * Return the Fragment associated with a specified position.
     */
    public IRCFragment getItem(final int position) {
        return views.get(position);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        // If we already have this item instantiated, there is nothing
        // to do. This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (mFragments.size() > position) {
            final Fragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        final Fragment fragment = getItem(position);
        if (DEBUG)
            Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        mFragments.set(position, fragment);
        mCurTransaction.add(container.getId(), fragment);

        return fragment;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public void restoreState(final Parcelable state, final ClassLoader loader) {
        if (state != null) {
            final Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            final Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mFragments.clear();
            if (fss != null) {
                for (Parcelable stat : fss) {
                    mSavedState.add((Fragment.SavedState) stat);
                }
            }
            final Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
        }
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            final Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState
                    .size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i = 0; i < mFragments.size(); i++) {
            final Fragment f = mFragments.get(i);
            if (f != null) {
                if (state == null) {
                    state = new Bundle();
                }
                final String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        return state;
    }

    public void addServerFragment(final String serverName) {
        final ServerFragment fragment = new ServerFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("title", serverName);
        fragment.setArguments(bundle);

        addFragment(fragment);
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, final Object object) {
        final Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    public int addFragment(final IRCFragment s) {
        views.add(s);
        notifyDataSetChanged();
        if (tabStrip != null) {
            tabStrip.notifyDataSetChanged();
        }
        return views.indexOf(s);
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public int getItemPosition(final Object object) {
        if (views.contains(object)) {
            return POSITION_UNCHANGED;
        } else {
            return POSITION_NONE;
        }
    }

    @Override
    public CharSequence getPageTitle(@NonNull final int position) {
        final IRCFragment fragment = views.get(position);
        return fragment.isAdded() ? fragment.getTitle() : fragment.getArguments().getString("title");
    }

    public void removeFragment(final int index) {
        views.remove(index);
        tabStrip.notifyDataSetChanged();
        notifyDataSetChanged();
    }

    public IRCFragment getFragment(@NonNull final String title, @NonNull final FragmentType type) {
        for (final IRCFragment fragment : views) {
            if (fragment.getType().equals(type) && (title.equals(fragment.getTitle()) ||
                    (type.equals(FragmentType.User) && MiscUtils.areNicksEqual(fragment.getTitle(), title)))) {
                int indexOfFragment = views.indexOf(fragment);
                if ((indexOfFragment == currentItemIndex || indexOfFragment == (currentItemIndex
                        - 1) || indexOfFragment == (currentItemIndex + 1))) {
                    return fragment;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public int getIndexFromTitle(@NonNull final String title) {
        for (final IRCFragment i : views) {
            if (title.equals(i.getTitle())) {
                return views.indexOf(i);
            }
        }
        return -1;
    }

    public void disableAllEditTexts() {
        for (final IRCFragment fragment : views) {
            fragment.disableEditText();
        }
    }

    public void removeAllButServer() {
        final Iterator<IRCFragment> iterator = views.iterator();
        iterator.next();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        tabStrip.notifyDataSetChanged();
        notifyDataSetChanged();
    }
}

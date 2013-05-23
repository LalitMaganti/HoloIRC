/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.fusionx.lightirc.fragments.IRCFragment;

import java.util.ArrayList;

public class IRCPagerAdapter extends LightFragmentStatePagerAdapter {
    private final ArrayList<IRCFragment> views = new ArrayList<IRCFragment>();

    public IRCPagerAdapter(final FragmentManager fm) {
        super(fm);
    }

    public int addView(final IRCFragment s) {
        views.add(s);
        notifyDataSetChanged();
        return views.indexOf(s);
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public Fragment getItem(final int position) {
        return views.get(position);
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
    public CharSequence getPageTitle(final int position) {
        return views.get(position).getTitle();
    }

    public void removeView(int index) {
        views.remove(index);
        notifyDataSetChanged();
    }

    public IRCFragment getTab(String title) {
        for (IRCFragment i : views) {
            if (i.getTitle().equals(title)) {
                return i;
            }
        }
        return null;
    }
}

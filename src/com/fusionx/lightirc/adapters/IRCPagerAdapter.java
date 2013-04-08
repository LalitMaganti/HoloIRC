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

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.fusionx.lightirc.fragments.IRCFragment;

public class IRCPagerAdapter extends FragmentPagerAdapter {
	private ArrayList<IRCFragment> views = new ArrayList<IRCFragment>();

	public IRCPagerAdapter(final FragmentManager fm) {
		super(fm);
	}

	@Override
	public int getItemPosition(final Object object) {
		return PagerAdapter.POSITION_NONE;
	}

	public int getItemPosition(final IRCFragment ircfragment) {
		return views.indexOf(ircfragment);
	}

	@Override
	public Fragment getItem(final int position) {
		return views.get(position);
	}

	public int addView(final IRCFragment s) {
		views.add(s);
		notifyDataSetChanged();
		return views.indexOf(s);
	}

	public int removeView(int index) {
		views.remove(index);
		notifyDataSetChanged();
		return views.size() - 1;
	}

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public CharSequence getPageTitle(final int position) {
		return views.get(position).getTitle();
	}
}

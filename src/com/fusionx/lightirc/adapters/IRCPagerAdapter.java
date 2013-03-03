package com.fusionx.lightirc.adapters;

import java.util.ArrayList;

import com.fusionx.lightirc.fragments.IRCFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class IRCPagerAdapter extends FragmentPagerAdapter {
	private ArrayList<IRCFragment> views = new ArrayList<IRCFragment>();

	public IRCPagerAdapter(final FragmentManager fm) {
		super(fm);
	}
	
	@Override
	public int getItemPosition(final Object ircfragment) {
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

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public CharSequence getPageTitle(final int position) {
		return views.get(position).getTitle();
	}
}
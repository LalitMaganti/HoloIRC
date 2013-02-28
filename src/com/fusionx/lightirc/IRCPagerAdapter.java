package com.fusionx.lightirc;

import java.util.ArrayList;

import com.fusionx.lightirc.fragments.IRCFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class IRCPagerAdapter extends FragmentPagerAdapter {
	private ArrayList<IRCFragment> views = new ArrayList<IRCFragment>();

	public IRCPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return views.get(position);
	}

	public void addView(IRCFragment s) {
		views.add(s);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return views.get(position).getTitle();
	}
}

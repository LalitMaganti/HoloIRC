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

package com.fusionx.lightirc.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.ChannelFragment;
import com.fusionx.lightirc.fragments.IRCFragment;
import com.fusionx.lightirc.fragments.ServerFragment;
import com.fusionx.lightirc.fragments.UserFragment;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class ServerChannelActivity extends SlidingFragmentActivity implements
		TabListener, OnPageChangeListener {
	private IRCPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private Menu actionBarMenu;
	private final UserFragment userFragment = new UserFragment();

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setBehindContentView(R.layout.menu_frame);

		getFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, userFragment).commit();

		setContentView(R.layout.activity_main_ui);

		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.RIGHT);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setFadeDegree(0.35f);
		sm.setBehindWidth(300);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		sm.setTouchmodeMarginThreshold(0);
		setSlidingActionBarEnabled(false);

		String s = getIntent().getExtras().getString("serverName");

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);

		mSectionsPagerAdapter = new IRCPagerAdapter(getSupportFragmentManager());

		initialisePaging(s);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		Intent intent = new Intent(this, MainServerListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		switch (item.getItemId()) {
		case android.R.id.home:
			startActivity(intent);
			return true;
		case R.id.item_channel_part:
			int index = mViewPager.getCurrentItem();
			((ChannelFragment) mSectionsPagerAdapter.getItem(index)).part();
			removeTab(index);
			mViewPager.setCurrentItem(index - 1);
			mSectionsPagerAdapter.removeView(index);
			return true;
		case R.id.item_server_disconnect:
			((ServerFragment) mSectionsPagerAdapter.getItem(0)).disconnect();
			startActivity(intent);
			return true;
		case R.id.item_channel_users:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateTabTitle(final IRCFragment fragment, final String newTitle) {
		final ActionBar actionBar = getActionBar();
		final int index = mSectionsPagerAdapter.getItemPosition(fragment);
		actionBar.getTabAt(index).setText(newTitle);
	}

	private void initialisePaging(String s) {
		final ServerFragment d = new ServerFragment();
		final Bundle b = new Bundle();
		b.putString("serverName", s);
		d.setArguments(b);
		mSectionsPagerAdapter.addView(d);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(this);

		addTab(0);
	}

	public void addTab(final int i) {
		final ActionBar actionBar = getActionBar();
		actionBar.addTab(actionBar.newTab()
				.setText(mSectionsPagerAdapter.getPageTitle(i))
				.setTabListener(this));
	}

	public void removeTab(final int i) {
		final ActionBar actionBar = getActionBar();
		actionBar.removeTabAt(i);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.server_channel_action_bar, menu);
		actionBarMenu = menu;
		return true;
	}

	public void addChannelFragment(ChannelFragment channel, String channelName) {
		final int position = mSectionsPagerAdapter.addView(channel);
		addTab(position);
		updateTabTitle(channel, channelName);
		mViewPager.setOffscreenPageLimit(position);
	}

	@Override
	public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
		if(getSlidingMenu().isSecondaryMenuShowing()) {
			getSlidingMenu().toggle();
		}
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
	}

	@Override
	public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
	}

	@Override
	public void onPageSelected(final int position) {
		actionBarMenu.findItem(R.id.item_channel_part).setVisible(
				!(position == 0));
		actionBarMenu.findItem(R.id.item_channel_users).setVisible(
				!(position == 0));
		if(position == 0) {
			getSlidingMenu().setTouchmodeMarginThreshold(0);
		} else {
			getSlidingMenu().setTouchmodeMarginThreshold(3);
			userFragment.adapter.clear();
			String userList[] = ((ChannelFragment)mSectionsPagerAdapter.getItem(position)).mUserList;
			if(userList == null) {
				userFragment.adapter.add("Unknown");
			} else {
				userFragment.adapter.addAll(userList);
			}
			userFragment.adapter.notifyDataSetChanged();
		}
		getActionBar().setSelectedNavigationItem(position);
	}

	@Override
	public void onPageScrollStateChanged(final int arg0) {
	}

	@Override
	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
	}
}

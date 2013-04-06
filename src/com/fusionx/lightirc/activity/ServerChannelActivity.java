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
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.ChannelFragment;
import com.fusionx.lightirc.fragments.IRCFragment;
import com.fusionx.lightirc.fragments.ServerFragment;

public class ServerChannelActivity extends FragmentActivity implements
		TabListener, OnPageChangeListener {
	private IRCPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private Menu actionBarMenu;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_ui);
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
		switch (item.getItemId()) {
		case android.R.id.home:
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		case R.id.item_channel_part:
			// TODO - part from channel
			int index = mViewPager.getCurrentItem();
			((ChannelFragment) mSectionsPagerAdapter.getItem(index)).part();
			removeTab(index);
			mViewPager.setOffscreenPageLimit(0);
			mViewPager.setCurrentItem(index - 1);
			/*int lastPage = */mSectionsPagerAdapter.removeView(index);
			//mViewPager.setOffscreenPageLimit(lastPage);
			return true;
		case R.id.item_server_disconnect:
			((ServerFragment) mSectionsPagerAdapter.getItem(0)).disconnect();
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
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
		getActionBar().setSelectedNavigationItem(position);
	}

	@Override
	public void onPageScrollStateChanged(final int arg0) {
	}

	@Override
	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
	}
}

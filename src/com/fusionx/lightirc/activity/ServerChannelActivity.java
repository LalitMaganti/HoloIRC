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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.IRCFragment;
import com.fusionx.lightirc.fragments.ServerFragment;

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

public class ServerChannelActivity extends FragmentActivity {

	public IRCPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

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

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			addTab(i);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainServerListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateTabTitle(final IRCFragment fragment,
			final String newTitle) {
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

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
		});
	}

	public void addTab(final int i) {
		final ActionBar actionBar = getActionBar();
		actionBar.addTab(actionBar.newTab()
				.setText(mSectionsPagerAdapter.getPageTitle(i))
				.setTabListener(new TabListener() {
					@Override
					public void onTabSelected(final Tab tab,
							final FragmentTransaction ft) {
						mViewPager.setCurrentItem(tab.getPosition());
					}

					@Override
					public void onTabReselected(final Tab tab,
							final FragmentTransaction ft) {
					}

					@Override
					public void onTabUnselected(final Tab tab,
							final FragmentTransaction ft) {
					}
				}));
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main_ui, menu);
		return true;
	}
}

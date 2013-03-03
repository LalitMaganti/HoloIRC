package com.fusionx.lightirc.activity;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.ServerFragment;
import com.fusionx.lightirc.misc.ServerObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;

public class ServerChannelActivity extends FragmentActivity {

	public IRCPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_ui);
		ServerObject s = (ServerObject) getIntent().getExtras()
				.getSerializable("serverObject");

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mSectionsPagerAdapter = new IRCPagerAdapter(getSupportFragmentManager());

		initialisePaging(s);

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			addTab(i);
		}
	}

	public void updateTabTitle(final ServerFragment fragment,
			final String newTitle) {
		final ActionBar actionBar = getActionBar();
		final int index = mSectionsPagerAdapter.getItemPosition(fragment);
		actionBar.getTabAt(index).setText(newTitle);
	}

	private void initialisePaging(final ServerObject s) {
		final ServerFragment d = new ServerFragment();
		final Bundle b = new Bundle();
		b.putSerializable("serverObject", s);
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

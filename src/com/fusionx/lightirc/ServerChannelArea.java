package com.fusionx.lightirc;

import com.fusionx.lightirc.fragments.ServerFragment;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;

public class ServerChannelArea extends FragmentActivity {

	public IRCPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_ui);

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mSectionsPagerAdapter = new IRCPagerAdapter(getSupportFragmentManager());
		
		initialisePaging();
		
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			addTab(i);
		}
	}
    
    private void initialisePaging() {
    	mSectionsPagerAdapter.addView(new ServerFragment());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
		});
	}

	public void addTab(int i) {
    	final ActionBar actionBar = getActionBar();
		actionBar.addTab(actionBar.newTab()
				.setText(mSectionsPagerAdapter.getPageTitle(i))
				.setTabListener(new TabListener() {
					@Override
					public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
						mViewPager.setCurrentItem(tab.getPosition());
					}

					@Override
					public void onTabReselected(Tab tab, FragmentTransaction ft) {}

					@Override
					public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
		}));
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_ui, menu);
		return true;
	}
}

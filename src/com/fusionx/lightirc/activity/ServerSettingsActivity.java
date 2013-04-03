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

import java.util.ArrayList;
import java.util.List;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.LightPircBotX;

import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ServerSettingsActivity extends PreferenceActivity {
	static LightPircBotX s;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainServerListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	public static class BaseServerSettingFragment extends PreferenceFragment {
		public final static String URL = "edit_text_url";

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle f = getActivity().getIntent().getExtras();
			s = (LightPircBotX) f.getParcelable("server");

			addPreferencesFromResource(R.xml.pref_general);

			PreferenceScreen prefSet = getPreferenceScreen();

			EditTextPreference mEditTextUrl = (EditTextPreference) prefSet
					.findPreference(URL);

			mEditTextUrl.setText(s.mURL);
			mEditTextUrl.setSummary(s.mURL);
		}
	}

	public static class ListViewSettingsFragment extends ListFragment implements
			OnItemLongClickListener, android.view.ActionMode.Callback {
		protected Object mActionMode;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = super
					.onCreateView(inflater, container, savedInstanceState);
			final ArrayList<String> personList = new ArrayList<String>();

			final ArrayAdapter<String> ds = new ArrayAdapter<String>(
					getActivity().getApplicationContext(),
					R.layout.layout_simple_list, personList);
			for (String k : s.mAutoJoinChannels) {
				ds.add(k);
			}

			setListAdapter(ds);
			return v;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			getListView().setOnItemLongClickListener(this);
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.toast:
				// Action here
				mode.finish(); // Action picked, so close the CAB
				return true;
			default:
				return false;
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.contex, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			mActionMode = null;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
			return false;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View arg1,
				int position, long arg3) {
			if (mActionMode != null) {
				return false;
			}

			mActionMode = getActivity().startActionMode(this);
			((ListView) parent).setItemChecked(position,
					((ListView) parent).isItemChecked(position));
			return true;
		}
	}
}

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

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.LightPircBotX;

public class ServerSettingsActivity extends PreferenceActivity {
	private static LightPircBotX bot;

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

	public static class BaseServerSettingFragment extends PreferenceFragment
			implements OnPreferenceChangeListener {
		private final static String URL = "edit_text_url";
		public final static String Title = "edit_text_title";
		public final static String Nick = "edit_text_nick";
		private EditTextPreference mEditTextUrl;
		private EditTextPreference mEditTextTitle;
		private EditTextPreference mEditTextNick;
		private static int indexOfServer;

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle f = getActivity().getIntent().getExtras();
			bot = (LightPircBotX) f.getParcelable("server");
			indexOfServer = f.getInt("indexOfServer");

			addPreferencesFromResource(R.xml.pref_general);

			PreferenceScreen prefSet = getPreferenceScreen();

			mEditTextUrl = (EditTextPreference) prefSet.findPreference(URL);
			// TODO - pref change
			mEditTextUrl.setText(bot.mURL);
			mEditTextUrl.setSummary(bot.mURL);

			mEditTextTitle = (EditTextPreference) prefSet.findPreference(Title);
			// TODO - pref change
			mEditTextTitle.setText(bot.getTitle());
			mEditTextTitle.setSummary(bot.getTitle());

			mEditTextNick = (EditTextPreference) prefSet.findPreference(Nick);
			mEditTextNick.setOnPreferenceChangeListener(this);
			mEditTextNick.setText(bot.getLogin());
			mEditTextNick.setSummary(bot.getLogin());
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference == mEditTextNick) {
				final SharedPreferences settings = getActivity()
						.getSharedPreferences("main", 0);
				final Editor e = settings.edit();
				e.putString("server_" + indexOfServer + "_nick",
						(String) newValue);
				e.commit();
				mEditTextNick.setSummary((CharSequence) newValue);
			}
			return true;
		}
	}

	public static class ListViewSettingsFragment extends ListFragment implements
			MultiChoiceModeListener, android.view.ActionMode.Callback,
			DialogInterface.OnClickListener {
		protected Object mActionMode;
		EditText inputView;
		final ArrayList<String> channelList = new ArrayList<String>();

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity().getApplicationContext(),
					R.layout.layout_simple_list, channelList);

			for (String channel : bot.mAutoJoinChannels) {
				adapter.add(channel);
			}

			inputView = new EditText(getActivity());

			setListAdapter(adapter);

			setHasOptionsMenu(true);

			return super.onCreateView(inflater, container, savedInstanceState);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			getListView().setMultiChoiceModeListener(this);
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.item_channel_context_edit:
				// TODO - edit here
				mode.finish();
				return true;
			case R.id.item_channel_context_delete:
				// TODO - delete here
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			super.onOptionsItemSelected(item);

			switch (item.getItemId()) {
			case R.id.item_channel_context_add:
				inputView.setHint("Channel name (including the starting #)");
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity())
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("Channel Name")
						.setPositiveButton("OK", ListViewSettingsFragment.this)
						.setNegativeButton("Cancel",
								ListViewSettingsFragment.this)
						.setView(inputView);

				builder.show();
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO - input validation
			channelList.add(inputView.getText().toString());
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.menu_channel_action_bar, menu);
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
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			if (getListView().getCheckedItemCount() > 1) {
				mode.getMenu().getItem(0).setVisible(false);
			} else if (getListView().getCheckedItemCount() == 1) {
				mode.getMenu().getItem(0).setVisible(true);
			}
			mode.setTitle(getListView().getCheckedItemCount()
					+ " items selected");
		}
	}
}

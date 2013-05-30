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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.LightBuilder;
import com.fusionx.lightirc.misc.Constants;

import java.util.ArrayList;
import java.util.List;

public class ServerSettingsActivity extends PreferenceActivity {
    public static class BaseServerSettingFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
        private static int indexOfServer;

        // Preference keys
        private final static String Title = "pref_title";
        private final static String URL = "pref_url";
        private final static String Nick = "pref_nick";

        private final static String Login = "pref_login";
        private final static String ServerUserName = "pref_login_username";
        private final static String ServerPassword = "pref_login_password";

        private final static String NickServ = "pref_nickserv";
        private final static String NickServPassword = "pref_nickserv_password";

        // Generic
        private EditTextPreference mEditTextNick;
        private EditTextPreference mEditTextTitle;
        private EditTextPreference mEditTextUrl;

        // Server login
        private CheckBoxPreference mLoginPref;
        private EditTextPreference mServerUserName;
        private EditTextPreference mServerPassword;

        // NickServ
        private CheckBoxPreference mNickServPref;
        private EditTextPreference mNickServPassword;

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final SharedPreferences settings = getActivity().getSharedPreferences("main", 0);

            final Bundle f = getActivity().getIntent().getExtras();
            bot = f.getParcelable("server");
            indexOfServer = f.getInt("indexOfServer");

            addPreferencesFromResource(R.xml.activty_settings_prefs);

            PreferenceScreen prefSet = getPreferenceScreen();

            // Title of server
            mEditTextTitle = (EditTextPreference) prefSet.findPreference(Title);
            mEditTextTitle.setOnPreferenceChangeListener(this);
            mEditTextTitle.setText(bot.getTitle());
            mEditTextTitle.setSummary(bot.getTitle());

            // URL of server
            mEditTextUrl = (EditTextPreference) prefSet.findPreference(URL);
            mEditTextUrl.setOnPreferenceChangeListener(this);
            mEditTextUrl.setText(bot.getServerHostname());
            mEditTextUrl.setSummary(bot.getServerHostname());

            // Nick of User
            mEditTextNick = (EditTextPreference) prefSet.findPreference(Nick);
            mEditTextNick.setOnPreferenceChangeListener(this);
            mEditTextNick.setText(bot.getName());
            mEditTextNick.setSummary(bot.getName());

            // Server login details
            final boolean loginEnabled = settings.getBoolean("loginenabled", false);

            mLoginPref = (CheckBoxPreference) prefSet.findPreference(Login);
            mLoginPref.setChecked(loginEnabled);

            mServerUserName = (EditTextPreference) prefSet.findPreference(ServerUserName);
            mServerUserName.setOnPreferenceChangeListener(this);
            mServerUserName.setEnabled(loginEnabled);

            mServerPassword = (EditTextPreference) prefSet.findPreference(ServerPassword);
            mServerPassword.setOnPreferenceChangeListener(this);
            mServerPassword.setEnabled(loginEnabled);

            if (loginEnabled) {
                mServerUserName.setText(bot.getLogin());
                mServerUserName.setSummary(bot.getLogin());

                mServerPassword.setText(bot.getServerPassword());
                mServerPassword.setSummary(bot.getServerPassword());
            }

            // Nickserv details
            final boolean nickServEnabled = settings.getBoolean("nickservenabled", false);

            mNickServPref = (CheckBoxPreference) prefSet.findPreference(NickServ);
            mNickServPref.setChecked(nickServEnabled);

            mNickServPassword = (EditTextPreference) prefSet.findPreference(NickServPassword);
            mNickServPassword.setOnPreferenceChangeListener(this);
            mNickServPassword.setEnabled(loginEnabled);

            if (loginEnabled) {
                mNickServPassword.setText(bot.getServerPassword());
                mNickServPassword.setSummary(bot.getServerPassword());
            }
        }


        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            final SharedPreferences settings = getActivity().getSharedPreferences("main", 0);
            final Editor e = settings.edit();
            if (preference == mLoginPref) {
                boolean check = mLoginPref.isChecked();
                e.putBoolean("loginenabled", check);

                mServerUserName.setEnabled(check);
                mServerPassword.setEnabled(check);
                if (!check) {
                    mServerUserName.setText("");
                    mServerUserName.setSummary("");

                    mServerPassword.setText("");
                    mServerPassword.setSummary("");

                    e.putString(Constants.serverUsernamePrefPrefix + indexOfServer, "lightirc");
                    e.putString(Constants.serverPasswordPrefPrefix + indexOfServer, "");
                }
            } else if (preference == mNickServPref) {
                boolean check = mNickServPref.isChecked();
                e.putBoolean("nickservenabled", check);

                mNickServPassword.setEnabled(check);
                if (!check) {
                    mNickServPassword.setText("");
                    mNickServPassword.setSummary("");

                    e.putString(Constants.serverNickServPasswordPrefPrefix + indexOfServer, "");
                }
            }
            e.commit();
            return true;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(newValue instanceof String) {
                final SharedPreferences settings = getActivity().getSharedPreferences("main", 0);
                final Editor e = settings.edit();

                final String newString = (String) newValue;

                if (preference == mEditTextNick) {
                    e.putString(Constants.nickPrefPrefix + indexOfServer, newString);
                } else if (preference == mEditTextTitle) {
                    e.putString(Constants.titlePrefPrefix + indexOfServer, newString);
                } else if (preference == mEditTextUrl) {
                    e.putString(Constants.urlPrefPrefix + indexOfServer, newString);
                } else if (preference == mServerUserName) {
                    e.putString(Constants.serverUsernamePrefPrefix + indexOfServer, newString);
                } else if (preference == mServerPassword) {
                    e.putString(Constants.serverPasswordPrefPrefix + indexOfServer, newString);
                } else if (preference == mNickServPassword) {
                    e.putString(Constants.serverNickServPasswordPrefPrefix + indexOfServer, newString);
                }

                e.commit();
                preference.setSummary((String) newValue);
            }
            return true;
        }
    }

    public static class ListViewSettingsFragment extends ListFragment implements
            MultiChoiceModeListener, android.view.ActionMode.Callback,
            DialogInterface.OnClickListener {
        final ArrayList<String> channelList = new ArrayList<String>();
        EditText inputView;

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.activity_server_settings_cab_edit:
                    // TODO - edit here
                    mode.finish();
                    return true;
                case R.id.activity_server_settings_cab_delete:
                    // TODO - delete here
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            getListView().setMultiChoiceModeListener(this);
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO - input validation
            channelList.add(inputView.getText().toString());
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.activty_server_settings_cab, menu);
            return true;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.activity_server_settings_ab, menu);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.layout_text_list, channelList);

            for (String channel : bot.getAutoJoinChannels().keySet()) {
                adapter.add(channel);
            }

            inputView = new EditText(getActivity());

            setListAdapter(adapter);
            setHasOptionsMenu(true);

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            mode.getMenu().getItem(0)
                    .setVisible(!(getListView().getCheckedItemCount() > 1));
            mode.setTitle(getListView().getCheckedItemCount()
                    + " items selected");
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            super.onOptionsItemSelected(item);

            switch (item.getItemId()) {
                case R.id.activity_server_settings_ab_add:
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
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }

    private static LightBuilder bot;

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

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


}

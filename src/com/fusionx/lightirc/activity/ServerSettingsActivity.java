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
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.SelectionAdapter;
import com.fusionx.lightirc.misc.Constants;
import com.fusionx.lightirc.misc.PromptDialog;
import org.pircbotx.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServerSettingsActivity extends PreferenceActivity {
    private static Configuration.Builder bot;

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public static class BaseServerSettingFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
        private static int indexOfServer;

        // Preference keys
        private final static String Title = "pref_title";
        private final static String URL = "pref_url";
        private final static String Port = "pref_port";
        private final static String Nick = "pref_nick";

        private final static String Login = "pref_login";
        private final static String ServerUserName = "pref_login_username";
        private final static String ServerPassword = "pref_login_password";

        private final static String NickServ = "pref_nickserv";
        private final static String NickServPassword = "pref_nickserv_password";

        // Generic
        private EditTextPreference mEditTextNick;
        private EditTextPreference mEditTextUrl;
        private EditTextPreference mEditTextPort;
        private EditTextPreference mEditTextTitle;

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

            // Port of server
            mEditTextPort = (EditTextPreference) prefSet.findPreference(Port);
            mEditTextPort.setOnPreferenceChangeListener(this);
            mEditTextPort.setText(String.valueOf(bot.getServerPort()));
            mEditTextPort.setSummary(String.valueOf(bot.getServerPort()));

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
            }
        }


        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);

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

                    e.putString(Constants.serverUsernamePrefPrefix + indexOfServer, "lightirc");
                    e.putString(Constants.serverPasswordPrefPrefix + indexOfServer, "");
                }
            } else if (preference == mNickServPref) {
                boolean check = mNickServPref.isChecked();
                e.putBoolean("nickservenabled", check);

                mNickServPassword.setEnabled(check);
                if (!check) {
                    mNickServPassword.setText("");

                    e.putString(Constants.serverNickServPasswordPrefPrefix + indexOfServer, "");
                }
            }
            e.commit();
            return true;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue instanceof String) {
                final SharedPreferences settings = getActivity().getSharedPreferences("main", 0);
                final Editor e = settings.edit();

                final String newString = (String) newValue;
                if (preference != mNickServPassword && preference != mServerPassword) {
                    if (preference == mEditTextNick) {
                        e.putString(Constants.nickPrefPrefix
                                + indexOfServer, newString);
                    } else if (preference == mEditTextTitle) {
                        e.putString(Constants.titlePrefPrefix
                                + indexOfServer, newString);
                    } else if (preference == mEditTextUrl) {
                        e.putString(Constants.urlPrefPrefix
                                + indexOfServer, newString);
                    } else if (preference == mEditTextPort) {
                        e.putInt(Constants.serverPortPrefPrefix
                                + indexOfServer, Integer.parseInt(newString));
                    } else if (preference == mServerUserName) {
                        e.putString(Constants.serverUsernamePrefPrefix
                                + indexOfServer, newString);
                    }
                    preference.setSummary((String) newValue);
                } else if (preference == mServerPassword) {
                    e.putString(Constants.serverPasswordPrefPrefix
                            + indexOfServer, newString);
                } else {
                    e.putString(Constants.serverNickServPasswordPrefPrefix
                            + indexOfServer, newString);
                }
                e.commit();
            }
            return true;
        }
    }

    public static class ListViewSettingsFragment extends ListFragment implements
            MultiChoiceModeListener, android.view.ActionMode.Callback {
        private SelectionAdapter adapter;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.activty_server_settings_cab, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.getItem(0).setVisible(!(getListView().getCheckedItemCount() > 1));
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final Set<String> positions = adapter.getSelectedItems();

            switch (item.getItemId()) {
                case R.id.activity_server_settings_cab_edit:
                    PromptDialog dialog = new PromptDialog(getActivity(), "Channel Name"
                            , "", (String) positions.toArray()[0]) {
                        @Override
                        public boolean onOkClicked(final String input) {
                            adapter.remove((String) positions.toArray()[0]);
                            adapter.add(input);
                            return false;
                        }
                    };
                    dialog.show();

                    mode.finish();
                    return true;
                case R.id.activity_server_settings_cab_delete:
                    for (String selected : positions) {
                        adapter.remove(selected);
                    }
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            mode.invalidate();
            mode.setTitle(getListView().getCheckedItemCount() + " items selected");
            if (checked) {
                adapter.addSelection(position);
            } else {
                adapter.removeSelection(position);
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            adapter.clearSelection();
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            getListView().setMultiChoiceModeListener(this);
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.activity_server_settings_ab, menu);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            adapter = new SelectionAdapter(getActivity(), new ArrayList<String>());

            for (String channel : bot.getAutoJoinChannels().keySet()) {
                adapter.add(channel);
            }

            setListAdapter(adapter);
            setHasOptionsMenu(true);

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            super.onOptionsItemSelected(item);

            switch (item.getItemId()) {
                case R.id.activity_server_settings_ab_add:
                    PromptDialog dialog = new PromptDialog(getActivity(), "Channel Name",
                            "Channel name (including the starting #") {
                        @Override
                        public boolean onOkClicked(String input) {
                            adapter.add(input);
                            return false;
                        }
                    };
                    dialog.show();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onPause() {
            final SharedPreferences settings = getActivity().getSharedPreferences("main", 0);
            final Editor e = settings.edit();
            e.putStringSet(Constants.autoJoinPrefPrefix + "0", adapter.getItems());
            e.commit();

            super.onDestroy();
        }
    }
}

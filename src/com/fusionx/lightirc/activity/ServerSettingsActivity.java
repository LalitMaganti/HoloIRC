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
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerSettingsActivity extends PreferenceActivity {
    private static boolean canExit;
    private static int noOfServers;
    private static int indexOfServer;
    private static boolean newServer;

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    public void onBackPressed() {
        if(!canExit && newServer) {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setTitle("Are you sure you want to exit?")
                    .setMessage("Changes have been made - discard?").setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // do nothing
                }
            });
            build.show();
        } else if (newServer) {
            final SharedPreferences settings = getSharedPreferences("main", 0);
            Editor e = settings.edit();
            e.putInt("noOfServers", noOfServers + 1);
            e.commit();
            finish();
        } else {
            finish();
        }
    }

    public static class BaseServerSettingFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
        // Generic
        private EditTextPreference mEditTextNick;
        private EditTextPreference mEditTextUrl;
        private EditTextPreference mEditTextPort;
        private EditTextPreference mEditTextTitle;

        // Server login
        private EditTextPreference mServerUserName;
        private EditTextPreference mServerPassword;

        // NickServ
        private EditTextPreference mNickServPassword;

        private List<EditTextPreference> alltheedittexts = new ArrayList<EditTextPreference>();

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);

            final Bundle f = getActivity().getIntent().getExtras();
            newServer = f.getBoolean("new", false);

            noOfServers = f.getInt("noOfServers");
            if(newServer) {
                indexOfServer = noOfServers;
                getPreferenceManager().setSharedPreferencesName("server_" + indexOfServer);
            } else {
                indexOfServer = f.getInt("indexOfServer");
                getPreferenceManager().setSharedPreferencesName("server_" + indexOfServer);
            }
            addPreferencesFromResource(R.xml.activty_settings_prefs);

            final PreferenceScreen prefSet = getPreferenceScreen();

            mEditTextTitle = (EditTextPreference) prefSet.findPreference(Constants.Title);
            mEditTextTitle.setOnPreferenceChangeListener(this);
            alltheedittexts.add(mEditTextTitle);

            // URL of server
            mEditTextUrl = (EditTextPreference) prefSet.findPreference(Constants.URL);
            mEditTextUrl.setOnPreferenceChangeListener(this);
            alltheedittexts.add(mEditTextUrl);

            // Port of server
            mEditTextPort = (EditTextPreference) prefSet.findPreference(Constants.Port);
            mEditTextPort.setOnPreferenceChangeListener(this);
            alltheedittexts.add(mEditTextPort);

            // Nick of User
            mEditTextNick = (EditTextPreference) prefSet.findPreference(Constants.Nick);
            mEditTextNick.setOnPreferenceChangeListener(this);
            alltheedittexts.add(mEditTextNick);

            mServerUserName = (EditTextPreference) prefSet.findPreference(Constants.ServerUserName);
            mServerUserName.setOnPreferenceChangeListener(this);

            mServerPassword = (EditTextPreference) prefSet.findPreference(Constants.ServerPassword);
            mServerPassword.setOnPreferenceChangeListener(this);


            mNickServPassword = (EditTextPreference) prefSet.findPreference(Constants.NickServPassword);
            mNickServPassword.setOnPreferenceChangeListener(this);

            canExit = !newServer;

            // TODO - consolidate this - use all the edittexts var
            if (newServer) {
                // Title of server
                mEditTextTitle.setSummary("This field should NOT be empty!");

                // URL of server
                mEditTextUrl.setSummary("This field should NOT be empty!");

                // Port of server
                mEditTextPort.setSummary("Usually 6667 - This field should NOT be empty!");

                // Nick of User
                mEditTextNick.setSummary("This field should NOT be empty!");
            } else {
                // Title of server
                mEditTextTitle.setSummary(mEditTextTitle.getText());

                // URL of server
                mEditTextUrl.setSummary(mEditTextUrl.getText());

                // Port of server
                mEditTextPort.setSummary(mEditTextPort.getText());

                // Nick of User
                mEditTextNick.setSummary(mEditTextNick.getText());
            }

            // Server username
            mServerUserName.setSummary(mServerUserName.getText());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue instanceof String) {
                final SharedPreferences settings = getActivity().getSharedPreferences("main", 0);
                final Editor e = settings.edit();

                final String newString = (String) newValue;
                if (preference != mNickServPassword && preference != mServerPassword) {
                    preference.setSummary(newString);
                }
                e.commit();
            }
            if(newServer) {
                for(EditTextPreference edit : alltheedittexts) {
                    if(edit.getText() == null) {
                        canExit = false;
                        break;
                    } else {
                        canExit = true;
                    }
                }
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
            inflater.inflate(R.menu.activity_server_settings_channellist_ab, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            adapter = new SelectionAdapter(getActivity(), new ArrayList<String>());

            SharedPreferences settings = getActivity()
                    .getSharedPreferences("server_" + indexOfServer, MODE_PRIVATE);
            Set<String> set = settings.getStringSet(Constants.AutoJoin, new HashSet<String>());
            for (String channel : set) {
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
            SharedPreferences settings = getActivity()
                    .getSharedPreferences("server_0", MODE_PRIVATE);
            final Editor e = settings.edit();
            e.putStringSet(Constants.AutoJoin, adapter.getItems());
            e.commit();

            super.onDestroy();
        }
    }
}

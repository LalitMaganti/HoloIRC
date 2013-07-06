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
import android.widget.Toast;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.SelectionAdapter;
import com.fusionx.lightirc.misc.Constants;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.promptdialogs.ChannelNamePromptDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerSettingsActivity extends PreferenceActivity {
    private static boolean canExit;
    private static boolean newServer;
    private static String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getThemeInt(getApplicationContext()));

        if (getIntent().getExtras().getBoolean("main")) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new BaseServerSettingFragment())
                    .commit();
        }
    }


    @Override
    public void onBackPressed() {
        if (!canExit && newServer) {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setTitle(getString(R.string.server_settings_save_question_title))
                    .setMessage(getString(R.string.server_settings_save_question_message))
                    .setNegativeButton(getString(R.string.discard), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            File folder = new File(getFilesDir().getAbsolutePath()
                                    .replace("files", "shared_prefs/") + fileName + ".xml");
                            folder.delete();
                            finish();
                        }
                    }).setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // do nothing
                }
            });
            build.show();
        } else {
            Toast.makeText(this, getString(R.string.server_settings_changes_saved), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public class BaseServerSettingFragment extends PreferenceFragment
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

        private final List<EditTextPreference> mEditTexts = new ArrayList<EditTextPreference>();

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);

            final Bundle bundle = getActivity().getIntent().getExtras();
            newServer = bundle.getBoolean("new", false);

            fileName = bundle.getString("file");

            getPreferenceManager().setSharedPreferencesName(fileName);

            addPreferencesFromResource(R.xml.activty_server_settings_prefs);

            final PreferenceScreen prefSet = getPreferenceScreen();

            mEditTextTitle = (EditTextPreference) prefSet.findPreference(Constants.Title);
            mEditTextTitle.setOnPreferenceChangeListener(this);
            mEditTexts.add(mEditTextTitle);

            // URL of server
            mEditTextUrl = (EditTextPreference) prefSet.findPreference(Constants.URL);
            mEditTextUrl.setOnPreferenceChangeListener(this);
            mEditTexts.add(mEditTextUrl);

            // Port of server
            mEditTextPort = (EditTextPreference) prefSet.findPreference(Constants.Port);
            mEditTextPort.setOnPreferenceChangeListener(this);
            mEditTexts.add(mEditTextPort);

            // Nick of User
            mEditTextNick = (EditTextPreference) prefSet.findPreference(Constants.Nick);
            mEditTextNick.setOnPreferenceChangeListener(this);
            mEditTexts.add(mEditTextNick);

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
                mEditTextTitle.setSummary(getString(R.string.server_settings_not_empty));

                // URL of server
                mEditTextUrl.setSummary(getString(R.string.server_settings_not_empty));

                // Port of server
                mEditTextPort.setSummary(getString(R.string.server_settings_not_empty_port));

                // Nick of User
                mEditTextNick.setSummary(getString(R.string.server_settings_not_empty));
            } else {
                for (EditTextPreference edit : mEditTexts) {
                    edit.setSummary(edit.getText());
                }
            }

            // Server username
            mServerUserName.setSummary(mServerUserName.getText());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue instanceof String) {
                final String newString = (String) newValue;
                if (preference != mNickServPassword && preference != mServerPassword) {
                    preference.setSummary(newString);
                }
            }
            if (newServer) {
                canExit = true;
                for (EditTextPreference edit : mEditTexts) {
                    if (edit.getText() == null) {
                        canExit = false;
                        break;
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
                    final ChannelNamePromptDialog dialog = new ChannelNamePromptDialog(getActivity(),
                            (String) positions.toArray()[0]) {
                        @Override
                        public void onOkClicked(final String input) {
                            adapter.remove((String) positions.toArray()[0]);
                            adapter.add(input);
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

            if (checked) {
                adapter.addSelection(position);
            } else {
                adapter.removeSelection(position);
            }

            int selectedItemCount = getListView().getCheckedItemCount();

            final String quantityString = getResources().getQuantityString(R.plurals.channel_selection,
                    selectedItemCount, selectedItemCount);
            mode.setTitle(quantityString);
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
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            adapter = new SelectionAdapter(getActivity(), new ArrayList<String>());

            SharedPreferences settings = getActivity()
                    .getSharedPreferences(fileName, MODE_PRIVATE);
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
                    final ChannelNamePromptDialog dialog = new ChannelNamePromptDialog(getActivity()) {
                        @Override
                        public void onOkClicked(final String input) {
                            adapter.add(input);
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
                    .getSharedPreferences(fileName, MODE_PRIVATE);
            final Editor e = settings.edit();
            e.putStringSet(Constants.AutoJoin, adapter.getItems());
            e.commit();

            super.onPause();
        }
    }
}

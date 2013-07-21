/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.holoirc.activity;

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
import android.widget.AdapterView;
import android.widget.Toast;
import com.fusionx.holoirc.R;
import com.fusionx.holoirc.misc.SharedPreferencesUtils;
import com.fusionx.holoirc.misc.Utils;
import com.fusionx.holoirc.promptdialogs.ChannelNamePromptDialogBuilder;
import com.fusionx.lightlibrary.adapters.SelectionAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.fusionx.holoirc.misc.PreferenceKeys.*;

public class ServerSettingsActivity extends PreferenceActivity {
    private static boolean canExit = true;
    private static boolean newServer = false;
    private static String fileName = null;

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
            final AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setTitle(getString(R.string.server_settings_save_question_title))
                    .setMessage(getString(R.string.server_settings_save_question_message))
                    .setNegativeButton(getString(R.string.discard), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final File folder = new File(SharedPreferencesUtils
                                    .getSharedPreferencesPath(getApplicationContext())
                                    + fileName + ".xml");
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

    public static class BaseServerSettingFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        // Server login
        private EditTextPreference mServerPassword = null;

        // NickServ
        private EditTextPreference mNickServPassword = null;

        private final List<EditTextPreference> mEditTexts = new ArrayList<>();

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);

            final Bundle bundle = getActivity().getIntent().getExtras();
            if (bundle != null) {
                newServer = bundle.getBoolean("new", false);
                fileName = bundle.getString("file");
            }

            getPreferenceManager().setSharedPreferencesName(fileName);

            addPreferencesFromResource(R.xml.activty_server_settings_prefs);

            final PreferenceScreen prefSet = getPreferenceScreen();

            EditTextPreference mEditTextTitle = (EditTextPreference) prefSet.findPreference(Title);
            if (mEditTextTitle != null) {
                mEditTextTitle.setOnPreferenceChangeListener(this);
            }
            mEditTexts.add(mEditTextTitle);

            // URL of server
            EditTextPreference mEditTextUrl = (EditTextPreference) prefSet.findPreference(URL);
            if (mEditTextUrl != null) {
                mEditTextUrl.setOnPreferenceChangeListener(this);
            }
            mEditTexts.add(mEditTextUrl);

            // Port of server
            EditTextPreference mEditTextPort = (EditTextPreference) prefSet.findPreference(Port);
            if (mEditTextPort != null) {
                mEditTextPort.setOnPreferenceChangeListener(this);
            }
            mEditTexts.add(mEditTextPort);

            // Nick of User
            EditTextPreference mEditTextNick = (EditTextPreference) prefSet.findPreference(Nick);
            if (mEditTextNick != null) {
                mEditTextNick.setOnPreferenceChangeListener(this);
            }
            mEditTexts.add(mEditTextNick);

            // Nick of User
            EditTextPreference mEditTextRealName = (EditTextPreference) prefSet.findPreference(RealName);
            if (mEditTextRealName != null) {
                mEditTextRealName.setOnPreferenceChangeListener(this);
            }
            mEditTexts.add(mEditTextRealName);

            EditTextPreference mServerUserName = (EditTextPreference) prefSet.findPreference(ServerUserName);
            if (mServerUserName != null) {
                mServerUserName.setOnPreferenceChangeListener(this);
                mServerUserName.setSummary(mServerUserName.getText());
            }

            mServerPassword = (EditTextPreference) prefSet.findPreference(ServerPassword);
            if (mServerPassword != null) {
                mServerPassword.setOnPreferenceChangeListener(this);
            }

            mNickServPassword = (EditTextPreference) prefSet.findPreference(NickServPassword);
            if (mNickServPassword != null) {
                mNickServPassword.setOnPreferenceChangeListener(this);
            }

            canExit = !newServer;

            if (newServer) {
                // Title of server
                mEditTextTitle.setSummary(getString(R.string.server_settings_not_empty));

                // URL of server
                mEditTextUrl.setSummary(getString(R.string.server_settings_not_empty));

                // Port of server
                mEditTextPort.setSummary(getString(R.string.server_settings_not_empty_port));

                // Nick of User
                mEditTextNick.setSummary(getString(R.string.server_settings_not_empty));

                // RealName of User
                mEditTextRealName.setSummary(getString(R.string.server_settings_not_empty));
            } else {
                for (EditTextPreference edit : mEditTexts) {
                    edit.setSummary(edit.getText());
                }
            }
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
            MultiChoiceModeListener, android.view.ActionMode.Callback, AdapterView.OnItemClickListener {
        private SelectionAdapter<String> adapter;
        private boolean modeStarted = false;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflate = mode.getMenuInflater();
            inflate.inflate(R.menu.activty_server_settings_cab, menu);

            modeStarted = true;

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
                    final String edited = (String) positions.toArray()[0];
                    final ChannelNamePromptDialogBuilder dialog = new ChannelNamePromptDialogBuilder
                            (getActivity(), edited) {
                        @Override
                        public void onOkClicked(final String input) {
                            adapter.remove(edited);
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

            if (selectedItemCount != 0) {
                final String quantityString = getResources().getQuantityString(R.plurals.channel_selection,
                        selectedItemCount, selectedItemCount);
                mode.setTitle(quantityString);
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            adapter.clearSelection();

            modeStarted = false;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            getListView().setMultiChoiceModeListener(this);
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
            inflate.inflate(R.menu.activity_server_settings_channellist_ab, menu);
            super.onCreateOptionsMenu(menu, inflate);
        }

        @Override
        public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            adapter = new SelectionAdapter<>(getActivity(), new ArrayList<String>());

            final SharedPreferences settings = getActivity().getSharedPreferences(fileName, MODE_PRIVATE);
            final Set<String> set = settings.getStringSet(AutoJoin, new HashSet<String>());
            for (final String channel : set) {
                adapter.add(channel);
            }

            setListAdapter(adapter);
            setHasOptionsMenu(true);

            return super.onCreateView(inflate, container, savedInstanceState);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            super.onOptionsItemSelected(item);

            switch (item.getItemId()) {
                case R.id.activity_server_settings_ab_add:
                    final ChannelNamePromptDialogBuilder dialog = new ChannelNamePromptDialogBuilder(getActivity()) {
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
            SharedPreferences settings = getActivity().getSharedPreferences(fileName, MODE_PRIVATE);
            final Editor e = settings.edit();
            e.putStringSet(AutoJoin, adapter.getItems()).commit();

            super.onPause();
        }

        @Override
        public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
            if (!modeStarted) {
                getActivity().startActionMode(this);
            }

            final boolean checked = adapter.getSelectedItems().contains(adapter.getItem(i));
            getListView().setItemChecked(i, !checked);
        }
    }
}

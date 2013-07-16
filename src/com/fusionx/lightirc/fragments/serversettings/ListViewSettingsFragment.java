package com.fusionx.lightirc.fragments.serversettings;

import android.app.Activity;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.AbsListView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.ServerSettingsListenerInterface;
import com.fusionx.lightirc.misc.PreferenceKeys;
import com.fusionx.lightirc.promptdialogs.ChannelNamePromptDialogBuilder;
import com.fusionx.lightlibrary.adapters.SelectionAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ListViewSettingsFragment extends ListFragment implements
        AbsListView.MultiChoiceModeListener, android.view.ActionMode.Callback {
    private SelectionAdapter<String> adapter;
    private ServerSettingsListenerInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ServerSettingsListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChannelFragmentListenerInterface");
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflate = mode.getMenuInflater();
        inflate.inflate(R.menu.activty_server_settings_cab, menu);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
        inflate.inflate(R.menu.activity_server_settings_channellist_ab, menu);
        super.onCreateOptionsMenu(menu, inflate);
    }

    @Override
    public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
                             final Bundle savedInstanceState) {
        adapter = new SelectionAdapter<String>(getActivity(), new ArrayList<String>());

        final SharedPreferences settings = getActivity().getSharedPreferences(mListener.getFileName(),
                getActivity().MODE_PRIVATE);
        final Set<String> set = settings.getStringSet(PreferenceKeys.AutoJoin, new HashSet<String>());
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
        SharedPreferences settings = getActivity().getSharedPreferences(mListener.getFileName(), getActivity().MODE_PRIVATE);
        final SharedPreferences.Editor e = settings.edit();
        e.putStringSet(PreferenceKeys.AutoJoin, adapter.getItems()).commit();

        super.onPause();
    }
}
package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.BaseCollectionAdapter;
import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.misc.PreferenceConstants;
import com.fusionx.lightirc.ui.dialogbuilder.DialogBuilder;
import com.fusionx.lightirc.util.MultiSelectionUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_AUTOJOIN;

public class ChannelListFragment extends ListFragment {

    private BaseCollectionAdapter<String> mAdapter;

    private MultiChoiceFragmentListener mListener = new MultiChoiceFragmentListener() {

        @Override
        protected void attachSelectionController() {
            mMultiSelectionController = MultiSelectionUtils.attachMultiSelectionController(
                    getListView(), (ActionBarActivity) getActivity(), this, true);
        }

        @Override
        protected ListAdapter getRealAdapter() {
            return mAdapter;
        }

        @Override
        protected SparseBooleanArray getCheckedItemPositions() {
            return getListView().getCheckedItemPositions();
        }

        @Override
        public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
            final MenuInflater inflate = mode.getMenuInflater();
            inflate.inflate(R.menu.activty_server_settings_cab, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final List<String> positions = getCheckedItems();

            switch (item.getItemId()) {
                case R.id.activity_server_settings_cab_edit:
                    final String edited = mAdapter.getItem(0);
                    final ChannelDialogBuilder dialog = new ChannelDialogBuilder(edited) {
                        @Override
                        public void onOkClicked(final String input) {
                            mAdapter.remove(edited);
                            mAdapter.add(input);
                        }
                    };
                    dialog.show();

                    mode.finish();
                    return true;
                case R.id.activity_server_settings_cab_delete:
                    for (String selected : positions) {
                        mAdapter.remove(selected);
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
            int selectedItemCount = getCheckedItems().size();
            if (selectedItemCount != 0) {
                final String quantityString = getResources().getQuantityString(R.plurals
                        .channel_selection, selectedItemCount, selectedItemCount);
                mode.setTitle(quantityString);
                mode.getMenu().getItem(0).setVisible(selectedItemCount == 1);
            }
        }
    };

    private ServerSettingsCallbacks mCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ServerSettingsCallbacks) activity;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences settings = getActivity().getSharedPreferences("temp",
                Context.MODE_PRIVATE);
        final Set<String> set = settings
                .getStringSet(PreferenceConstants.PREF_AUTOJOIN, new HashSet<String>());
        mAdapter = new BaseCollectionAdapter<>(getActivity(), R.layout.default_listview_textview,
                new TreeSet<>(set));

        setListAdapter(mAdapter);
        setHasOptionsMenu(true);

        mListener.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListener.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListener.onSaveInstanceState(outState);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        mListener.setMenuVisibility(menuVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
        inflate.inflate(R.menu.activity_server_settings_channellist_ab, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.activity_server_settings_ab_add:
                final ChannelDialogBuilder dialog = new ChannelDialogBuilder() {
                    @Override
                    public void onOkClicked(final String input) {
                        mAdapter.add(input);
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
        getActivity().getSharedPreferences("temp",
                Context.MODE_PRIVATE).edit().putStringSet(PREF_AUTOJOIN, mAdapter.getSetOfItems()).commit();
        super.onPause();
    }

    public abstract class ChannelDialogBuilder extends DialogBuilder {

        public ChannelDialogBuilder() {
            super(getActivity(), getActivity().getString(R.string.prompt_dialog_channel_name),
                    getActivity().getString(R.string.prompt_dialog_including_starting), "");
        }

        public ChannelDialogBuilder(String defaultText) {
            super(getActivity(), getActivity().getString(R.string.prompt_dialog_channel_name),
                    getActivity().getString(R.string.prompt_dialog_including_starting),
                    defaultText);
        }
    }
}
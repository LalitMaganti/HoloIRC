package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.BaseCollectionAdapter;
import com.fusionx.lightirc.adapters.DecoratedIgnoreListAdapter;
import com.fusionx.lightirc.constants.PreferenceConstants;
import com.fusionx.lightirc.ui.dialogbuilder.DialogBuilder;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.lightirc.util.MultiSelectionUtils;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.relay.Server;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;

import java.util.Set;
import java.util.TreeSet;

public class IgnoreListFragment extends MultiChoiceListFragment<String>
        implements OnDismissCallback {

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final IgnoreListCallback callback = FragmentUtils.getParent(this,
                IgnoreListCallback.class);

        final TreeSet<String> arrayList = new TreeSet<>(MiscUtils.getIgnoreList(getActivity(),
                callback.getServerTitle().toLowerCase()));
        final BaseCollectionAdapter<String> ignoreAdapter = new BaseCollectionAdapter<String>
                (getActivity(), R.layout.default_listview_textview, arrayList);
        final DecoratedIgnoreListAdapter listAdapter = new DecoratedIgnoreListAdapter
                (ignoreAdapter, this);

        setListAdapter(listAdapter);

        getListAdapter().setAbsListView(getListView());

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void attachSelectionController() {
        mMultiSelectionController = MultiSelectionUtils.attachMultiSelectionController(
                getListView(), (ActionBarActivity) getActivity(), this, true);
    }

    public void onClose() {
        if (mMultiSelectionController != null) {
            mMultiSelectionController.finish();
        }
    }

    @Override
    public DecoratedIgnoreListAdapter getListAdapter() {
        return (DecoratedIgnoreListAdapter) super.getListAdapter();
    }

    private BaseCollectionAdapter<String> getIgnoreAdapter() {
        return (BaseCollectionAdapter<String>) getListAdapter().getDecoratedBaseAdapter();
    }

    @Override
    public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
        // Inflate a menu resource providing context menu items
        final MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.ignore_list_cab, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.ignore_list_cab_add:
                final IgnoreListDialogBuilder builder = new IgnoreListDialogBuilder();
                builder.show();
                return true;
            case R.id.ignore_list_cab_remove:
                getListAdapter().animateDismiss(getCheckedPositions());
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle(getActivity().getString(R.string.ignore_list));
        mode.getMenu().getItem(1).setVisible(false);
        return super.onPrepareActionMode(mode, menu);
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        final IgnoreListCallback callback = FragmentUtils.getParent(this,
                IgnoreListCallback.class);
        callback.switchToIRCActionFragment();
        final SharedPreferences preferences = getActivity()
                .getSharedPreferences(callback.getServerTitle().toLowerCase(),
                        Context.MODE_PRIVATE);
        final Set<String> set = getIgnoreAdapter().getSetOfItems();
        SharedPreferencesUtils.putStringSet(preferences, PreferenceConstants.PREF_IGNORE_LIST, set);
        MiscUtils.onUpdateIgnoreList(callback.getServer(), set);
    }

    @Override
    public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
        for (final int position : reverseSortedPositions) {
            getIgnoreAdapter().remove(getIgnoreAdapter().getItem(position));
        }
    }

    @Override
    protected BaseCollectionAdapter<String> getRealAdapter() {
        return getIgnoreAdapter();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        final int checkedItemCount = getCheckedPositions().size();

        if (checkedItemCount != 0) {
            mode.setTitle(checkedItemCount + getActivity().getString(R.string.items_checked));
        } else {
            mode.setTitle(getActivity().getString(R.string.ignore_list));
        }
        mode.getMenu().getItem(0).setVisible(checkedItemCount == 0);
        mode.getMenu().getItem(1).setVisible(checkedItemCount > 0);
    }

    public interface IgnoreListCallback {

        public void switchToIRCActionFragment();

        public String getServerTitle();

        public Server getServer();
    }

    public class IgnoreListDialogBuilder extends DialogBuilder {

        public IgnoreListDialogBuilder() {
            super(getActivity(), getActivity().getString(R.string.ignore_nick_title),
                    getActivity().getString(R.string.ignore_nick_description), "");
        }

        @Override
        public void onOkClicked(final String input) {
            getIgnoreAdapter().add(input);
        }
    }
}
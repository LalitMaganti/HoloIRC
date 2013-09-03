package com.fusionx.lightirc.fragments.actions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fusionx.common.PreferenceKeys;
import com.fusionx.common.Utils;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.DecoratedIgnoreListAdapter;
import com.fusionx.lightirc.adapters.SelectionAdapter;
import com.fusionx.lightirc.misc.FragmentUtils;
import com.fusionx.lightirc.promptdialogs.IgnoreNickPromptDialogBuilder;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.TreeSet;

public class IgnoreListFragment extends ListFragment implements ActionMode.Callback,
        SlidingMenu.OnCloseListener, ListView.OnItemClickListener, AdapterView.OnItemLongClickListener, OnDismissCallback {
    private ActionMode mMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final IgnoreListCallback callback = FragmentUtils.getParent(this,
                IgnoreListCallback.class);

        final TreeSet<String> arrayList = new TreeSet<>(Utils.getIgnoreList(getActivity(),
                callback.getServerTitle().toLowerCase()));

        final SelectionAdapter<String> ignoreAdapter = new SelectionAdapter<>(getActivity(),
                arrayList);
        final DecoratedIgnoreListAdapter listAdapter = new DecoratedIgnoreListAdapter
                (ignoreAdapter, this);

        setListAdapter(listAdapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListAdapter().setAbsListView(getListView());

        getListView().setLongClickable(true);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onClose() {
        if (mMode != null) {
            mMode.finish();
        }
    }

    @Override
    public DecoratedIgnoreListAdapter getListAdapter() {
        return (DecoratedIgnoreListAdapter) super.getListAdapter();
    }

    private SelectionAdapter<String> getIgnoreAdapter() {
        return (SelectionAdapter<String>) getListAdapter().getDecoratedBaseAdapter();
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        final MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.ignore_list_cab, menu);

        getListAdapter().reset();
        getListAdapter().notifyDataSetChanged();

        mMode = actionMode;

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        final int checkedItemCount = getIgnoreAdapter().getSelectedItemCount();

        if (checkedItemCount != 0) {
            actionMode.setTitle(checkedItemCount + " items checked");
        } else {
            actionMode.setTitle("Ignore List");
        }
        menu.getItem(0).setVisible(checkedItemCount == 0);
        menu.getItem(1).setVisible(checkedItemCount > 0);

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.ignore_list_cab_add:
                final IgnoreNickPromptDialogBuilder builder = new IgnoreNickPromptDialogBuilder
                        (getActivity(), "") {
                    @Override
                    public void onOkClicked(final String input) {
                        getIgnoreAdapter().add(input);
                    }
                };
                builder.show();
                return true;
            case R.id.ignore_list_cab_remove:
                getListAdapter().animateDismiss(getIgnoreAdapter().getSelectedItemPositions());
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        final IgnoreListCallback callback = FragmentUtils.getParent(this,
                IgnoreListCallback.class);
        getIgnoreAdapter().clearSelection();

        callback.switchToIRCActionFragment();

        final SharedPreferences preferences = getActivity().getSharedPreferences
                (callback.getServerTitle().toLowerCase(), Context.MODE_PRIVATE);

        Utils.putStringSet(preferences, PreferenceKeys.IgnoreList,
                getIgnoreAdapter().getCopyOfItems());

        mMode = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final boolean checked = getIgnoreAdapter().isItemAtPositionChecked(i);
        if (checked) {
            getIgnoreAdapter().removeSelection(i);
        } else {
            getIgnoreAdapter().addSelection(i);
        }

        mMode.invalidate();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        onItemClick(adapterView, view, i, l);
        return true;
    }

    @Override
    public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            getIgnoreAdapter().remove(position);
        }
        getIgnoreAdapter().clearSelection();
        mMode.invalidate();
    }

    public interface IgnoreListCallback {
        public void switchToIRCActionFragment();

        public String getServerTitle();
    }
}
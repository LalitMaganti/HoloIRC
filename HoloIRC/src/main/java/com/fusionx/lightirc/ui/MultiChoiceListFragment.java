package com.fusionx.lightirc.ui;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import com.fusionx.lightirc.adapters.BaseCollectionAdapter;
import com.fusionx.lightirc.util.MultiSelectionUtils;

import java.util.ArrayList;
import java.util.List;

abstract class MultiChoiceListFragment<T> extends ListFragment implements
        MultiSelectionUtils.MultiChoiceModeListener {
    MultiSelectionUtils.Controller mMultiSelectionController;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        attachSelectionController();

        if (mMultiSelectionController != null) {
            mMultiSelectionController.tryRestoreInstanceState(savedInstanceState);
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected abstract void attachSelectionController();

    List<Integer> getCheckedPositions() {
        List<Integer> checkedSessionPositions = new ArrayList<Integer>();
        ListView listView = getListView();
        if (listView == null) {
            return checkedSessionPositions;
        }

        SparseBooleanArray checkedPositionsBool = listView.getCheckedItemPositions();
        for (int i = 0; i < checkedPositionsBool.size(); i++) {
            if (checkedPositionsBool.valueAt(i)) {
                checkedSessionPositions.add(checkedPositionsBool.keyAt(i));
            }
        }

        return checkedSessionPositions;
    }

    List<T> getCheckedItems() {
        List<T> checkedSessionPositions = new ArrayList<T>();
        ListView listView = getListView();
        if (listView == null) {
            return checkedSessionPositions;
        }

        SparseBooleanArray checkedPositionsBool = listView.getCheckedItemPositions();
        for (int i = 0; i < checkedPositionsBool.size(); i++) {
            if (checkedPositionsBool.valueAt(i)) {
                checkedSessionPositions.add(getRealAdapter().getItem(checkedPositionsBool
                        .keyAt(i)));
            }
        }

        return checkedSessionPositions;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMultiSelectionController != null) {
            mMultiSelectionController.finish();
            mMultiSelectionController = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMultiSelectionController != null) {
            mMultiSelectionController.saveInstanceState(outState);
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (mMultiSelectionController == null) {
            return;
        }
        // Hide the action mode when the fragment becomes invisible
        if (!menuVisible) {
            Bundle bundle = new Bundle();
            if (mMultiSelectionController.saveInstanceState(bundle)) {
                mMultiSelectionController.finish();
            }
        }
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode arg0) {
    }

    protected abstract BaseCollectionAdapter<T> getRealAdapter();
}
package com.fusionx.lightirc.ui;

import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

abstract class MultiChoiceFragmentListener<T> implements MultiSelectionUtils
        .MultiChoiceModeListener {

    MultiSelectionUtils.Controller mMultiSelectionController;

    private View mListView;

    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = view.findViewById(android.R.id.list);

        attachSelectionController();

        if (getMultiSelectionController() != null) {
            getMultiSelectionController().tryRestoreInstanceState(savedInstanceState);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void onDestroyView() {
        finish();
    }

    public void onSaveInstanceState(Bundle outState) {
        if (getMultiSelectionController() != null) {
            getMultiSelectionController().saveInstanceState(outState);
        }
    }

    public void setMenuVisibility(boolean menuVisible) {
        if (getMultiSelectionController() == null) {
            return;
        }
        // Hide the action mode when the fragment becomes invisible
        if (!menuVisible) {
            Bundle bundle = new Bundle();
            if (getMultiSelectionController().saveInstanceState(bundle)) {
                getMultiSelectionController().finish();
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

    protected abstract void attachSelectionController();

    protected abstract ListAdapter getRealAdapter();

    protected abstract SparseBooleanArray getCheckedItemPositions();

    MultiSelectionUtils.Controller getMultiSelectionController() {
        return mMultiSelectionController;
    }

    void finish() {
        if (getMultiSelectionController() != null) {
            getMultiSelectionController().finish();
            mMultiSelectionController = null;
        }
    }

    List<T> getCheckedItems() {
        final List<T> checkedSessionPositions = new ArrayList<>();
        if (mListView == null) {
            return checkedSessionPositions;
        }

        final SparseBooleanArray checkedPositionsBool = getCheckedItemPositions();
        for (int i = 0; i < checkedPositionsBool.size(); i++) {
            if (checkedPositionsBool.valueAt(i)) {
                checkedSessionPositions.add((T) getRealAdapter().getItem(checkedPositionsBool
                        .keyAt(i)));
            }
        }

        return checkedSessionPositions;
    }
}
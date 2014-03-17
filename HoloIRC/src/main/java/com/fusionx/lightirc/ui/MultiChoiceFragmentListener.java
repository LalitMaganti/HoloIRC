package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.adapters.BaseCollectionAdapter;
import com.fusionx.lightirc.util.MultiSelectionUtils;

import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public abstract class MultiChoiceFragmentListener<T> implements MultiSelectionUtils
        .MultiChoiceModeListener {

    MultiSelectionUtils.Controller mMultiSelectionController;

    private View mListView;

    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = view.findViewById(android.R.id.list);

        attachSelectionController();

        if (mMultiSelectionController != null) {
            mMultiSelectionController.tryRestoreInstanceState(savedInstanceState);
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected abstract void attachSelectionController();

    List<Integer> getCheckedPositions() {
        List<Integer> checkedSessionPositions = new ArrayList<>();
        if (mListView == null) {
            return checkedSessionPositions;
        }

        SparseBooleanArray checkedPositionsBool = getCheckedItemPositions();
        for (int i = 0; i < checkedPositionsBool.size(); i++) {
            if (checkedPositionsBool.valueAt(i)) {
                checkedSessionPositions.add(checkedPositionsBool.keyAt(i));
            }
        }

        return checkedSessionPositions;
    }

    protected List<T> getCheckedItems() {
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

    public void onDestroyView() {
        if (mMultiSelectionController != null) {
            mMultiSelectionController.finish();
            mMultiSelectionController = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mMultiSelectionController != null) {
            mMultiSelectionController.saveInstanceState(outState);
        }
    }

    public void setMenuVisibility(boolean menuVisible) {
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

    protected abstract ListAdapter getRealAdapter();

    protected abstract SparseBooleanArray getCheckedItemPositions();
}
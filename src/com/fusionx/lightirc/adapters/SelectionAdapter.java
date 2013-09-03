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

package com.fusionx.lightirc.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fusionx.common.utils.Utils;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.base.TreeSetAdapter;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class SelectionAdapter<T> extends TreeSetAdapter<T> {
    private final ArrayList<Integer> mSelectedItems = new ArrayList<>();

    private final Object mLock = new Object();

    public SelectionAdapter(final Context context, final SortedSet<T> objects) {
        super(context, R.layout.default_listview_textview, (TreeSet<T>) objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);
        Utils.setTypeface(getContext(), view);

        if (mSelectedItems.contains(position)) {
            view.setBackgroundColor(getContext().getResources().getColor(android.R.color
                    .holo_blue_light));
        } else {
            view.setBackgroundResource(R.drawable.default_item_selector);
        }

        return view;
    }

    public void remove(final int position) {
        super.remove(getItem(position));
    }

    /**
     * Adds an item to the selected items list
     *
     * @param position position of item in adapter to add to the list of selected items
     */
    public void addSelection(final int position) {
        synchronized (mLock) {
            mSelectedItems.add(position);
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Removes an item from the selected items list
     *
     * @param position position of item in adapter to remove from the list of selected items
     */
    public void removeSelection(final Integer position) {
        synchronized (mLock) {
            mSelectedItems.remove(position);
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Clears the selected items in the adapter
     */
    public void clearSelection() {
        mSelectedItems.clear();
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the list of selected items in the adapter
     *
     * @return returns a Set of the currently selected items
     */
    public ArrayList<T> getSelectedItems() {
        final ArrayList<T> list = new ArrayList<>();
        for (int i : mSelectedItems) {
            list.add(getItem(i));
        }
        return list;
    }

    public ArrayList<Integer> getSelectedItemPositions() {
        return mSelectedItems;
    }

    public TreeSet<T> getCopyOfItems() {
        return new TreeSet<>(mObjects);
    }

    public boolean isItemAtPositionChecked(final int position) {
        return mSelectedItems.contains(position);
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    public void setInternalSet(SortedSet<T> set) {
        mObjects = (TreeSet<T>) set;
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }

        synchronized (mLock) {
            mSelectedItems.clear();
        }
    }
}
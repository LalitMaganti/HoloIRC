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
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fusionx.common.Utils;
import com.fusionx.lightirc.R;
import com.fusionx.lightlibrary.adapters.TreeSetAdapter;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class SelectionAdapter<T> extends TreeSetAdapter<T> {
    private final SparseBooleanArray selectedItems = new SparseBooleanArray();

    public SelectionAdapter(final Context context, final SortedSet<T> objects) {
        super(context, R.layout.default_listview_textview, (TreeSet<T>) objects);

        for (int i = 0; i < objects.size(); i++) {
            selectedItems.put(i, false);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);
        Utils.setTypeface(getContext(), view);

        if (selectedItems.get(position)) {
            view.setBackgroundColor(getContext().getResources().getColor(android.R.color
                    .holo_blue_light));
        } else {
            view.setBackgroundResource(R.drawable.default_item_selector);
        }

        return view;
    }

    /**
     * Adds an item to the selected items list
     *
     * @param position position of item in adapter to add to the list of selected items
     */
    public void addSelection(final int position) {
        selectedItems.put(position, true);
        notifyDataSetChanged();
    }

    /**
     * Removes an item from the selected items list
     *
     * @param position position of item in adapter to remove from the list of selected items
     */
    public void removeSelection(final int position) {
        selectedItems.put(position, false);
        notifyDataSetChanged();
    }

    /**
     * Clears the selected items in the adapter
     */
    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Returns the list of selected items in the adapter
     *
     * @return returns a Set of the currently selected items
     */
    public ArrayList<T> getSelectedItems() {
        final ArrayList<T> list = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.get(i)) {
                list.add(getItem(i));
            }
        }
        return list;
    }

    public TreeSet<T> getCopyOfItems() {
        return new TreeSet<>(mObjects);
    }

    public boolean isItemAtPositionChecked(final int position) {
        return selectedItems.get(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void setInternalSet(SortedSet<T> set) {
        mObjects = (TreeSet<T>) set;
        notifyDataSetChanged();
    }
}
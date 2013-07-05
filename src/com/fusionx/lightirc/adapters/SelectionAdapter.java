/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.fusionx.lightirc.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SelectionAdapter extends ArrayAdapter<String> {
    private final HashMap<String, Boolean> selectedItems = new HashMap<String, Boolean>();
    private final ArrayList<String> arrayList;

    public SelectionAdapter(final Context context, final ArrayList<String> arrayList) {
        super(context, R.layout.layout_text_list, R.id.text1, arrayList);
        this.arrayList = arrayList;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View v = super.getView(position, convertView, parent);
        if (selectedItems.containsKey(arrayList.get(position))) {
            v.setBackgroundResource(android.R.color.holo_blue_light);
        } else {
            v.setBackgroundResource(R.drawable.selectable_background_cardbank);
        }
        return v;
    }

    public HashSet<String> getItems() {
        return new HashSet<String>(arrayList);
    }

    public void addSelection(final int position) {
        selectedItems.put(arrayList.get(position), true);
        notifyDataSetChanged();
    }

    public void removeSelection(final int position) {
        selectedItems.remove(arrayList.get(position));
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public Set<String> getSelectedItems() {
        return selectedItems.keySet();
    }
}

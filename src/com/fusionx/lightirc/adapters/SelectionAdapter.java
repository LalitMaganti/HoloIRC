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
        View v = super.getView(position, convertView, parent);
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

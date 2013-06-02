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
    protected HashMap<String, Boolean> selectedItems = new HashMap<String, Boolean>();
    protected final ArrayList<String> arrayList;

    public SelectionAdapter(Context context, ArrayList<String> arrayList) {
        super(context, R.layout.layout_text_list, R.id.text1, arrayList);
        this.arrayList = arrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if(selectedItems.containsKey(arrayList.get(position))) {
            v.setBackgroundResource(android.R.color.holo_blue_light);
        } else {
            v.setBackgroundResource(R.drawable.selectable_background_cardbank);
        }
        return v;
    }

    public HashSet<String> getItems() {
        HashSet<String> d = new HashSet<String>(arrayList);
        return d;
    }

    public void addSelection(int position) {
        selectedItems.put(arrayList.get(position), true);
        notifyDataSetChanged();
    }

    public void removeSelection(int position) {
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

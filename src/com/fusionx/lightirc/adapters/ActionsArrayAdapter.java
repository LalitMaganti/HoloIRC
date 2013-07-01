package com.fusionx.lightirc.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import lombok.AccessLevel;
import lombok.Setter;

public class ActionsArrayAdapter extends ArrayAdapter<String> {
    @Setter(AccessLevel.PUBLIC)
    private boolean connected;

    private LayoutInflater inflater;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        row.setText(getItem(position));

        if (!isEnabled(position)) {
            row.setTextColor(Color.GRAY);
        } else {
            row.setTextColor(Color.BLACK);
        }

        return row;
    }

    public ActionsArrayAdapter(Context context, int textViewResourceId, CharSequence[] objects) {
        super(context, textViewResourceId, (String[]) objects);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return !((position == 0) || (position == 1)) || connected;
    }
}

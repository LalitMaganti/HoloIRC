package com.fusionx.lightirc.adapters.actions;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.fusionx.lightirc.R;

import java.util.ArrayList;
import java.util.Set;

public class IgnoreAdapter extends ArrayAdapter<String> {
    public IgnoreAdapter(Context context, final ArrayList<String> objects) {
        super(context, R.layout.layout_text_list, objects);
    }
}
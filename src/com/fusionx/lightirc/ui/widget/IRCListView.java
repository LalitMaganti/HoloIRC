package com.fusionx.lightirc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class IRCListView extends ListView {
    public IRCListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void layoutChildren() {
        // Hacky way to get round exception
        // TODO - find a better way to do this
        if(getCount() != getAdapter().getCount()) {
            ((BaseAdapter) getAdapter()).notifyDataSetChanged();
        }
        super.layoutChildren();
    }
}
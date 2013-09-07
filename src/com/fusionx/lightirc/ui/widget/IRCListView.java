package com.fusionx.lightirc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.fusionx.lightirc.adapters.IRCMessageAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class IRCListView extends ListView {
    public IRCListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void layoutChildren() {
        // Hacky way to get round exception
        // TODO - find a better way to do this
        if(getCount() != getAdapter().getCount()) {
            getAdapter().notifyDataSetChanged();
        }
        synchronized (getAdapter().getListLock()) {
            super.layoutChildren();
        }
    }

    @Override
    public IRCMessageAdapter getAdapter() {
        if(super.getAdapter() == null) {
            return null;
        } else {
            return (IRCMessageAdapter) ((AlphaInAnimationAdapter) super.getAdapter())
                    .getDecoratedBaseAdapter();
        }
    }
}
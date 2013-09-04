package com.fusionx.lightirc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.fusionx.lightirc.util.MiscUtils;

public class HoloTextView extends TextView {
    public HoloTextView(Context context) {
        super(context);
        MiscUtils.setTypeface(getContext(), this);
    }

    public HoloTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        MiscUtils.setTypeface(getContext(), this);
    }

    public HoloTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        MiscUtils.setTypeface(getContext(), this);
    }
}

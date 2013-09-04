package com.fusionx.lightirc.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.fusionx.lightirc.utils.Util;

public class HoloTextView extends TextView {
    public HoloTextView(Context context) {
        super(context);
        Util.setTypeface(getContext(), this);
    }

    public HoloTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Util.setTypeface(getContext(), this);
    }

    public HoloTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Util.setTypeface(getContext(), this);
    }
}

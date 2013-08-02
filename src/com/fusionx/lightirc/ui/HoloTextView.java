package com.fusionx.lightirc.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.fusionx.lightirc.misc.Utils;

public class HoloTextView extends TextView {
    public HoloTextView(Context context) {
        super(context);
        Utils.setTypeface(getContext(), this);
    }

    public HoloTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Utils.setTypeface(getContext(), this);
    }

    public HoloTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Utils.setTypeface(getContext(), this);
    }
}

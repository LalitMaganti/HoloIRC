package com.fusionx.lightirc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.fusionx.lightirc.util.UIUtils;

public class RobotoThinTextView extends TextView {
    public RobotoThinTextView(Context context) {
        super(context);
        if (!isInEditMode()) {
            UIUtils.setRobotoThin(getContext(), this);
        }
    }

    public RobotoThinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            UIUtils.setRobotoThin(getContext(), this);
        }
    }

    public RobotoThinTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            UIUtils.setRobotoThin(getContext(), this);
        }
    }
}
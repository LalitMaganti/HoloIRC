package com.fusionx.lightirc.ui.widget;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ProgramableSlidingPaneLayout extends SlidingPaneLayout {

    private boolean mShouldSlide = true;

    public ProgramableSlidingPaneLayout(Context context) {
        super(context);
    }

    public ProgramableSlidingPaneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgramableSlidingPaneLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mShouldSlide && super.onInterceptTouchEvent(ev);
    }


    public void setSlideable(boolean slide) {
        mShouldSlide = slide;
    }
}
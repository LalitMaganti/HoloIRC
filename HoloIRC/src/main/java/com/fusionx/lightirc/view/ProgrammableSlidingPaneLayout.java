package com.fusionx.lightirc.view;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ProgrammableSlidingPaneLayout extends SlidingPaneLayout {

    private boolean mShouldSlide = true;

    public ProgrammableSlidingPaneLayout(final Context context) {
        super(context);
    }

    public ProgrammableSlidingPaneLayout(final Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgrammableSlidingPaneLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        return mShouldSlide && super.onInterceptTouchEvent(ev);
    }

    public void setSlideable(final boolean slide) {
        mShouldSlide = slide;
    }
}
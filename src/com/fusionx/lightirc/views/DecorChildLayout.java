package com.fusionx.lightirc.views;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * This class allows us to insert a layer in between the system decor view
 * and the actual decor. (e.g. Action Bar views). This is needed so we can
 * receive a call to fitSystemWindows(Rect) so we can adjust the header view
 * to fit the system windows too.
 * <p/>
 * THIS CLASS WAS NOT WRITTEN BY ME. This class is stolen/borrowed (:P) from
 * https://github.com/chrisbanes/ActionBar-PullToRefresh
 */
public final class DecorChildLayout extends FrameLayout {
    private final ViewGroup mHeaderViewWrapper;

    public DecorChildLayout(Context context, ViewGroup systemDecorView,
                            View headerView) {
        super(context);

        // Move all children from decor view to here
        for (int i = 0, z = systemDecorView.getChildCount(); i < z; i++) {
            View child = systemDecorView.getChildAt(i);
            systemDecorView.removeView(child);
            addView(child);
        }

        /**
         * Wrap the Header View in a FrameLayout and add it to this view. It
         * is wrapped so any inset changes do not affect the actual header
         * view.
         */
        mHeaderViewWrapper = new FrameLayout(context);
        mHeaderViewWrapper.addView(headerView);
        addView(mHeaderViewWrapper, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        // Adjust the Header View's padding to take the insets into account
        mHeaderViewWrapper.setPadding(insets.left, insets.top,
                insets.right, insets.bottom);

        // Call return super so that the rest of the
        return super.fitSystemWindows(insets);
    }
}
package com.fusionx.lightirc.adapters;

import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class AnimatedServerListAdapter extends SwingBottomInAnimationAdapter {

    private final SingleDismissCallback mCallback;

    public AnimatedServerListAdapter(BaseAdapter baseAdapter, SingleDismissCallback callback) {
        super(baseAdapter);
        mCallback = callback;
    }

    /**
     * Animate dismissal of the item at given position.
     */
    public void animateDismiss(final int position) {
        if (getAbsListView() == null) {
            throw new IllegalStateException("Call setAbsListView() on this AnimateDismissAdapter " +
                    "before calling setAdapter()!");
        }

        View view = getAbsListView().getChildAt(position);

        AnimatorSet animatorSet = new AnimatorSet();

        Animator[] animatorsArray = {
                ObjectAnimator.ofFloat(view, "translationX", 0, -view.getWidth()),
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        };
        animatorSet.setDuration(1000);

        animatorSet.playTogether(animatorsArray);
        animatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                invokeCallback(position);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }
        });
        animatorSet.start();
    }

    private void invokeCallback(final int position) {
        mCallback.onDismiss((GridView) getAbsListView(), position);
    }

    public interface SingleDismissCallback {

        public void onDismiss(GridView listView, int position);
    }
}
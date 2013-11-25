package com.fusionx.lightirc.adapters;

import com.haarman.listviewanimations.swinginadapters.SingleAnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public class AnimatedServerListAdapter extends SingleAnimationAdapter {

    private final long mAnimationDelayMillis;

    private final long mAnimationDurationMillis;

    public AnimatedServerListAdapter(BaseAdapter baseAdapter, SingleDismissCallback callback) {
        this(baseAdapter, DEFAULTANIMATIONDELAYMILLIS, DEFAULTANIMATIONDURATIONMILLIS, callback);
    }

    private AnimatedServerListAdapter(BaseAdapter baseAdapter, long animationDelayMillis,
            long animationDurationMillis,
            SingleDismissCallback callback) {
        super(baseAdapter);
        mAnimationDelayMillis = animationDelayMillis;
        mAnimationDurationMillis = animationDurationMillis;
        mCallback = callback;
    }

    @Override
    protected long getAnimationDelayMillis() {
        return mAnimationDelayMillis;
    }

    @Override
    protected long getAnimationDurationMillis() {
        return mAnimationDurationMillis;
    }

    @Override
    protected Animator getAnimator(ViewGroup parent, View view) {
        return ObjectAnimator.ofFloat(view, "translationY", 500, 0);
    }

    private final SingleDismissCallback mCallback;

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
        mCallback.onDismiss(getAbsListView(), position);
    }

    public interface SingleDismissCallback {

        public void onDismiss(AbsListView listView, int position);
    }
}
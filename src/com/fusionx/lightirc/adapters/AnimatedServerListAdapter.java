package com.fusionx.lightirc.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.swinginadapters.SingleAnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AnimatedServerListAdapter extends SingleAnimationAdapter {
    private final long mAnimationDelayMillis;
    private final long mAnimationDurationMillis;

    public AnimatedServerListAdapter(BaseAdapter baseAdapter, OnDismissCallback callback) {
        this(baseAdapter, DEFAULTANIMATIONDELAYMILLIS, DEFAULTANIMATIONDURATIONMILLIS, callback);
    }

    public AnimatedServerListAdapter(BaseAdapter baseAdapter, long animationDelayMillis,
                                     OnDismissCallback callback) {
        this(baseAdapter, animationDelayMillis, DEFAULTANIMATIONDURATIONMILLIS, callback);
    }

    private AnimatedServerListAdapter(BaseAdapter baseAdapter, long animationDelayMillis,
                                      long animationDurationMillis, OnDismissCallback callback) {
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

    private OnDismissCallback mCallback;

    /**
     * Animate dismissal of the item at given position.
     */
    public void animateDismiss(int position) {
        animateDismiss(Arrays.asList(position));
    }

    /**
     * Animate dismissal of the items at given positions.
     */
    void animateDismiss(Collection<Integer> positions) {
        final List<Integer> positionsCopy = new ArrayList<>(positions);
        if (getAbsListView() == null) {
            throw new IllegalStateException("Call setAbsListView() on this AnimateDismissAdapter before calling setAdapter()!");
        }

        List<View> views = getVisibleViewsForPositions(positionsCopy);

        if (!views.isEmpty()) {
            AnimatorSet animatorSet = new AnimatorSet();

            Animator[] animatorsArray = {
                    ObjectAnimator.ofFloat(views.get(0), "translationX", 0, -views.get(0).getWidth()),
                    ObjectAnimator.ofFloat(views.get(0), "alpha", 1f, 0f)
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
                    invokeCallback(positionsCopy);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }
            });
            animatorSet.start();
        } else {
            invokeCallback(positionsCopy);
        }
    }

    private void invokeCallback(Collection<Integer> positions) {
        ArrayList<Integer> positionsList = new ArrayList<>(positions);
        Collections.sort(positionsList);
        int[] dismissPositions = new int[positionsList.size()];
        for (int i = 0; i < positionsList.size(); i++) {
            dismissPositions[i] = positionsList.get(positionsList.size() - 1 - i);
        }
        mCallback.onDismiss(getAbsListView(), dismissPositions);
    }

    private List<View> getVisibleViewsForPositions(Collection<Integer> positions) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < getAbsListView().getChildCount(); i++) {
            View child = getAbsListView().getChildAt(i);
            if (positions.contains(getAbsListView().getPositionForView(child))) {
                views.add(child);
            }
        }
        return views;
    }
}
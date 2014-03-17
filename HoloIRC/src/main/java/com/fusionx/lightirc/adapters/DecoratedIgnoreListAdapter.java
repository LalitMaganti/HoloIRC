package com.fusionx.lightirc.adapters;

import com.haarman.listviewanimations.BaseAdapterDecorator;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * A BaseAdapterDecorator class which applies multiple Animators at once to views when they are
 * first shown. The Animators applied include the animations specified in getAnimators(ViewGroup,
 * View), plus an alpha transition.
 */
public class DecoratedIgnoreListAdapter extends BaseAdapterDecorator
        implements StickyListHeadersAdapter {

    private static final long DEFAULTANIMATIONDELAYMILLIS = 100;

    private static final long DEFAULTANIMATIONDURATIONMILLIS = 300;

    private static final long INITIALDELAYMILLIS = 150;

    private final SparseArray<AnimationInfo> mAnimators;

    private long mAnimationStartMillis;

    private int mLastAnimatedPosition;

    private int mLastAnimatedHeaderPosition;

    private boolean mHasParentAnimationAdapter;

    private boolean mShouldAnimate = true;

    public DecoratedIgnoreListAdapter(BaseAdapter baseAdapter, OnDismissCallback callback) {
        super(baseAdapter);
        mAnimators = new SparseArray<AnimationInfo>();

        mAnimationStartMillis = -1;
        mLastAnimatedPosition = -1;
        mLastAnimatedHeaderPosition = -1;

        if (baseAdapter instanceof DecoratedIgnoreListAdapter) {
            ((DecoratedIgnoreListAdapter) baseAdapter).setHasParentAnimationAdapter(true);
        }
        mCallback = callback;
    }

    /**
     * Call this method to reset animation status on all views. The next time
     * notifyDataSetChanged()
     * is called on the base adapter, all views will animate again. Will also call
     * setShouldAnimate(true).
     */
    void reset() {
        mAnimators.clear();
        mLastAnimatedPosition = -1;
        mLastAnimatedHeaderPosition = -1;
        mAnimationStartMillis = -1;
        mShouldAnimate = true;

        if (getDecoratedBaseAdapter() instanceof DecoratedIgnoreListAdapter) {
            ((DecoratedIgnoreListAdapter) getDecoratedBaseAdapter()).reset();
        }
    }

    public void setShouldAnimate(boolean shouldAnimate) {
        mShouldAnimate = shouldAnimate;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        boolean alreadyStarted = false;
        if (!mHasParentAnimationAdapter) {
            if (getAbsListView() == null) {
                throw new IllegalStateException("Call setListView() on this AnimationAdapter " +
                        "before setAdapter()!");
            }

            if (convertView != null) {
                alreadyStarted = cancelExistingAnimation(position, convertView);
            }
        }

        View itemView = super.getView(position, convertView, parent);

        if (!mHasParentAnimationAdapter && !alreadyStarted) {
            animateViewIfNecessary(position, itemView, parent);
        }
        return itemView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        boolean alreadyStarted = false;

        if (!mHasParentAnimationAdapter && convertView != null) {
            alreadyStarted = cancelExistingAnimation(position, convertView);
        }

        View itemView = null;
        if (mDecoratedBaseAdapter instanceof StickyListHeadersAdapter) {
            itemView = ((StickyListHeadersAdapter) mDecoratedBaseAdapter).getHeaderView(position,
                    convertView, parent);
        }

        if (!mHasParentAnimationAdapter && !alreadyStarted) {
            animateHeaderViewIfNecessary(position, itemView, parent);
        }

        return itemView;
    }

    @Override
    public long getHeaderId(int position) {
        if (mDecoratedBaseAdapter instanceof StickyListHeadersAdapter) {
            return ((StickyListHeadersAdapter) mDecoratedBaseAdapter).getHeaderId(position);
        }
        return 0;
    }

    private boolean cancelExistingAnimation(int position, View convertView) {
        boolean alreadyStarted = false;

        int hashCode = convertView.hashCode();
        AnimationInfo animationInfo = mAnimators.get(hashCode);
        if (animationInfo != null) {
            if (animationInfo.position != position) {
                animationInfo.animator.end();
                mAnimators.remove(hashCode);
            } else {
                alreadyStarted = true;
            }
        }

        return alreadyStarted;
    }

    private void animateViewIfNecessary(int position, View view, ViewGroup parent) {
        if (position > mLastAnimatedPosition && mShouldAnimate) {
            animateView(position, parent, view, false);
            mLastAnimatedPosition = position;
        }
    }

    private void animateHeaderViewIfNecessary(int position, View view, ViewGroup parent) {
        if (position > mLastAnimatedHeaderPosition && mShouldAnimate) {
            animateView(position, parent, view, true);
            mLastAnimatedHeaderPosition = position;
        }
    }

    private void animateView(int position, ViewGroup parent, View view, boolean isHeader) {
        if (mAnimationStartMillis == -1) {
            mAnimationStartMillis = System.currentTimeMillis();
        }

        hideView(view);

        Animator[] childAnimators;
        if (mDecoratedBaseAdapter instanceof DecoratedIgnoreListAdapter) {
            childAnimators = ((DecoratedIgnoreListAdapter) mDecoratedBaseAdapter).getAnimators
                    (parent, view);
        } else {
            childAnimators = new Animator[0];
        }
        Animator[] animators = getAnimators(parent, view);
        Animator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(concatAnimators(childAnimators, animators, alphaAnimator));
        set.setStartDelay(calculateAnimationDelay(isHeader));
        set.setDuration(getAnimationDurationMillis());
        set.start();

        mAnimators.put(view.hashCode(), new AnimationInfo(position, set));
    }

    private void hideView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0);
        AnimatorSet set = new AnimatorSet();
        set.play(animator);
        set.setDuration(0);
        set.start();
    }

    private Animator[] concatAnimators(Animator[] childAnimators, Animator[] animators,
            Animator alphaAnimator) {
        Animator[] allAnimators = new Animator[childAnimators.length + animators.length + 1];
        int i;

        for (i = 0; i < animators.length; ++i) {
            allAnimators[i] = animators[i];
        }

        for (Animator childAnimator : childAnimators) {
            allAnimators[i] = childAnimator;
            ++i;
        }

        allAnimators[allAnimators.length - 1] = alphaAnimator;
        return allAnimators;
    }

    @SuppressLint("NewApi")
    private long calculateAnimationDelay(boolean isHeader) {
        long delay;
        int numberOfItems = getAbsListView().getLastVisiblePosition() - getAbsListView()
                .getFirstVisiblePosition();
        if (numberOfItems + 1 < mLastAnimatedPosition) {
            delay = getAnimationDelayMillis();

            if (getAbsListView() instanceof GridView && Build.VERSION.SDK_INT >= 11) {
                delay += getAnimationDelayMillis() * ((mLastAnimatedPosition + 1) % ((GridView)
                        getAbsListView()).getNumColumns());
            }
        } else {
            long delaySinceStart = (mLastAnimatedPosition + 1) * getAnimationDelayMillis();
            delay = mAnimationStartMillis + getInitialDelayMillis() + delaySinceStart - System
                    .currentTimeMillis();
            delay -= isHeader && mLastAnimatedPosition > 0 ? getAnimationDelayMillis() : 0;
        }
        // System.out.println(isHeader + ": " + delay);

        return Math.max(0, delay);
    }

    /**
     * Set whether this AnimationAdapter is encapsulated by another AnimationAdapter. When this is
     * set to true, this AnimationAdapter does not apply any animations to the views. Should not be
     * set explicitly, the AnimationAdapter class manages this by itself.
     */
    void setHasParentAnimationAdapter(boolean hasParentAnimationAdapter) {
        mHasParentAnimationAdapter = hasParentAnimationAdapter;
    }

    /**
     * Get the delay in milliseconds before the first animation should start. Defaults to {@value
     * #INITIALDELAYMILLIS}.
     */
    long getInitialDelayMillis() {
        return INITIALDELAYMILLIS;
    }

    long getAnimationDelayMillis() {
        return DEFAULTANIMATIONDELAYMILLIS;
    }

    long getAnimationDurationMillis() {
        return DEFAULTANIMATIONDURATIONMILLIS;
    }

    Animator[] getAnimators(ViewGroup parent, View view) {
        return new Animator[0];
    }

    private class AnimationInfo {

        public final int position;

        public final Animator animator;

        public AnimationInfo(int position, Animator animator) {
            this.position = position;
            this.animator = animator;
        }
    }


    private final OnDismissCallback mCallback;

    /**
     * Animate dismissal of the item at given position.
     */
    public void animateDismiss(int position) {
        animateDismiss(Arrays.asList(position));
    }

    /**
     * Animate dismissal of the items at given positions.
     */
    public void animateDismiss(Collection<Integer> positions) {
        final List<Integer> positionsCopy = new ArrayList<Integer>(positions);
        if (getAbsListView() == null) {
            throw new IllegalStateException("Call setAbsListView() on this AnimateDismissAdapter " +
                    "before calling setAdapter()!");
        }

        List<View> views = getVisibleViewsForPositions(positionsCopy);

        if (!views.isEmpty()) {
            List<Animator> animators = new ArrayList<Animator>();
            for (final View view : views) {
                animators.add(createAnimatorForView(view));
            }

            AnimatorSet animatorSet = new AnimatorSet();

            Animator[] animatorsArray = new Animator[animators.size()];
            for (int i = 0; i < animatorsArray.length; i++) {
                animatorsArray[i] = animators.get(i);
            }

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
        ArrayList<Integer> positionsList = new ArrayList<Integer>(positions);
        Collections.sort(positionsList);
        int[] dismissPositions = new int[positionsList.size()];
        for (int i = 0; i < positionsList.size(); i++) {
            dismissPositions[i] = positionsList.get(positionsList.size() - 1 - i);
        }
        mCallback.onDismiss(getAbsListView(), dismissPositions);
    }

    private List<View> getVisibleViewsForPositions(Collection<Integer> positions) {
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < getAbsListView().getChildCount(); i++) {
            View child = getAbsListView().getChildAt(i);
            if (positions.contains(getAbsListView().getPositionForView(child))) {
                views.add(child);
            }
        }
        return views;
    }

    private Animator createAnimatorForView(final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        final int originalHeight = view.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                lp.height = 0;
                view.setLayoutParams(lp);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                view.setLayoutParams(lp);
            }
        });

        return animator;
    }
}

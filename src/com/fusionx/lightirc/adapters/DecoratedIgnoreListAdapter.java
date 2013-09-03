package com.fusionx.lightirc.adapters;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.haarman.listviewanimations.BaseAdapterDecorator;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DecoratedIgnoreListAdapter extends BaseAdapterDecorator {
    protected static final long DEFAULTANIMATIONDELAYMILLIS = 50;
    protected static final long DEFAULTANIMATIONDURATIONMILLIS = 150;
    private static final long INITIALDELAYMILLIS = 75;
    private SparseArray<AnimationInfo> mAnimators;
    private long mAnimationStartMillis;
    private int mLastAnimatedPosition;
    private boolean mHasParentAnimationAdapter;

    private OnDismissCallback mCallback;

    public DecoratedIgnoreListAdapter(BaseAdapter baseAdapter, OnDismissCallback callback) {
        super(baseAdapter);
        mAnimators = new SparseArray<AnimationInfo>();

        mAnimationStartMillis = -1;
        mLastAnimatedPosition = -1;

        if (baseAdapter instanceof DecoratedIgnoreListAdapter) {
            ((DecoratedIgnoreListAdapter) baseAdapter).setHasParentAnimationAdapter(true);
        }
        mCallback = callback;
    }

    /**
     * Call this method to reset animation status on all views. The next time
     * notifyDataSetChanged() is called on the base adapter, all views will
     * animate again.
     */
    public void reset() {
        mAnimators.clear();
        mLastAnimatedPosition = -1;
        mAnimationStartMillis = -1;

        if (getDecoratedBaseAdapter() instanceof DecoratedIgnoreListAdapter) {
            ((DecoratedIgnoreListAdapter) getDecoratedBaseAdapter()).reset();
        }
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        boolean alreadyStarted = false;

        if (!mHasParentAnimationAdapter) {
            if (getAbsListView() == null) {
                throw new IllegalStateException("Call setListView() on this AnimationAdapter before setAdapter()!");
            }

            if (convertView != null) {
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
            }
        }

        View itemView = super.getView(position, convertView, parent);

        if (!mHasParentAnimationAdapter && !alreadyStarted) {
            animateViewIfNecessary(position, itemView, parent);
        }
        return itemView;
    }

    private void animateViewIfNecessary(int position, View view, ViewGroup parent) {
        if (position > mLastAnimatedPosition && !mHasParentAnimationAdapter) {
            animateView(position, parent, view);
            mLastAnimatedPosition = position;
        }
    }

    private void animateView(int position, ViewGroup parent, View view) {
        if (mAnimationStartMillis == -1) {
            mAnimationStartMillis = System.currentTimeMillis();
        }

        hideView(view);

        Animator[] childAnimators;
        if (mDecoratedBaseAdapter instanceof DecoratedIgnoreListAdapter) {
            childAnimators = ((DecoratedIgnoreListAdapter) mDecoratedBaseAdapter).getAnimators(parent, view);
        } else {
            childAnimators = new Animator[0];
        }
        Animator[] animators = getAnimators(parent, view);
        Animator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(concatAnimators(childAnimators, animators, alphaAnimator));
        set.setStartDelay(calculateAnimationDelay());
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

        for (int j = 0; j < childAnimators.length; ++j) {
            allAnimators[i] = childAnimators[j];
            ++i;
        }

        allAnimators[allAnimators.length - 1] = alphaAnimator;
        return allAnimators;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private long calculateAnimationDelay() {
        long delay;
        int numberOfItems = getAbsListView().getLastVisiblePosition()
                - getAbsListView().getFirstVisiblePosition();
        if (numberOfItems + 1 < mLastAnimatedPosition) {
            delay = getAnimationDelayMillis();

            if (getAbsListView() instanceof GridView && Build.VERSION.SDK_INT >= 11) {
                delay += getAnimationDelayMillis() * ((mLastAnimatedPosition + 1) % ((GridView) getAbsListView()).getNumColumns());
            }
        } else {
            long delaySinceStart = (mLastAnimatedPosition + 1) * getAnimationDelayMillis();
            delay = mAnimationStartMillis + INITIALDELAYMILLIS + delaySinceStart
                    - System.currentTimeMillis();
        }
        return Math.max(0, delay);
    }

    /**
     * Set whether this AnimationAdapter is encapsulated by another
     * AnimationAdapter. When this is set to true, this AnimationAdapter does
     * not apply any animations to the views. Should not be set explicitly, the
     * AnimationAdapter class manages this by itself.
     */
    public void setHasParentAnimationAdapter(boolean hasParentAnimationAdapter) {
        mHasParentAnimationAdapter = hasParentAnimationAdapter;
    }

    /**
     * Get the delay in milliseconds before an animation of a view should start.
     */
    protected long getAnimationDelayMillis() {
        return DEFAULTANIMATIONDELAYMILLIS;
    }

    /**
     * Get the duration of the animation in milliseconds.
     */
    protected long getAnimationDurationMillis() {
        return DEFAULTANIMATIONDURATIONMILLIS;
    }

    /**
     * Get the Animators to apply to the views. In addition to the returned
     * Animators, an alpha transition will be applied to the view.
     *
     * @param parent The parent of the view
     * @param view   The view that will be animated, as retrieved by getView()
     */
    public Animator[] getAnimators(ViewGroup parent, View view) {
        return new Animator[0];
    }

    private class AnimationInfo {
        public int position;
        public Animator animator;

        public AnimationInfo(int position, Animator animator) {
            this.position = position;
            this.animator = animator;
        }
    }

    public void animateDismiss(int index) {
        animateDismiss(Arrays.asList(index));
    }

    public void animateDismiss(Collection<Integer> positions) {
        final List<Integer> positionsCopy = new ArrayList<Integer>(positions);
        if (getAbsListView() == null) {
            throw new IllegalStateException("Call setListView() on this AnimateDismissAdapter before calling setAdapter()!");
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
                public void onAnimationStart(Animator arg0) {
                }

                @Override
                public void onAnimationRepeat(Animator arg0) {
                }

                @Override
                public void onAnimationEnd(Animator arg0) {
                    invokeCallback(positionsCopy);
                }

                @Override
                public void onAnimationCancel(Animator arg0) {
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
            public void onAnimationStart(Animator arg0) {
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                lp.height = 0;
                view.setLayoutParams(lp);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
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

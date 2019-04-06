package com.fusionx.lightirc.view;

import com.fusionx.lightirc.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Snackbar extends TextView {

    private final Queue<String> mMessages = new ArrayDeque<>();

    private final AtomicInteger mAtomicInteger = new AtomicInteger();

    private boolean mShown = true;

    public Snackbar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(getResources().getColor(R.color.snackbar));
        setTextColor(Color.WHITE);
    }

    public void display(final String message) {
        mMessages.add(message);

        if (mMessages.size() == 1) {
            startProcessing();
        }
    }

    private void startProcessing() {
        final String message = mMessages.poll();
        final int index = mAtomicInteger.incrementAndGet();
        setText(message);
        if (!mShown) {
            show();
        }

        postDelayed(() -> {
            if (mMessages.size() > 0) {
                startProcessing();
            } else if (index == mAtomicInteger.get()) {
                hide();
            }
        }, 3000);
    }

    public void show() {
        mShown = true;

        final TranslateAnimation animation = new TranslateAnimation(0, 0, getHeight(), 0);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }
        });
        animation.setDuration(200);
        startAnimation(animation);
    }

    public void hide() {
        mShown = false;

        final TranslateAnimation animation = new TranslateAnimation(0, 0, 0, getHeight());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }
        });
        animation.setDuration(200);
        startAnimation(animation);
    }
}
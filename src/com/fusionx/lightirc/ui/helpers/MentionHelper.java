package com.fusionx.lightirc.ui.helpers;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.event.MentionEvent;
import com.fusionx.lightirc.ui.IRCActivity;
import com.fusionx.lightirc.ui.widget.DecorChildLayout;

import java.util.ArrayList;

public class MentionHelper {
    // Mention things
    private final Handler mMentionHandler = new Handler();
    private View mMentionView;
    private IRCActivity mActivity;
    private ArrayList<MentionEvent> mentionEvents = new ArrayList<>();
    private boolean mDisplayed;

    public MentionHelper(final IRCActivity activity) {
        mActivity = activity;

        // Get Window Decor View
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

        // Create Header view and then add to Decor View
        mMentionView = LayoutInflater.from(activity.getSupportActionBar().getThemedContext())
                .inflate(R.layout.toast_mention, decorView, false);
        mMentionView.setVisibility(View.GONE);

        // Create DecorChildLayout which will move all of the system's decor
        // view's children + the  Header View to itself. See DecorChildLayout for more info.
        final DecorChildLayout decorContents = new DecorChildLayout(activity, decorView,
                mMentionView);

        // Now add the DecorChildLayout to the decor view
        decorView.addView(decorContents, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void onMention(final MentionEvent event) {
        if(!mDisplayed) {
            final String message = String.format(mActivity.getString(R.string.activity_mentioned),
                    event.destination);

            final TextView textView = (TextView) mMentionView.findViewById(R.id.toast_text);
            textView.setText(message);

            mMentionView.startAnimation(AnimationUtils.loadAnimation(mActivity,
                    R.anim.action_bar_in));
            mMentionView.setVisibility(View.VISIBLE);

            mDisplayed = true;

            onMentionEnd(event);
        } else {
            mentionEvents.add(event);
        }
    }

    private void onMentionEnd(final MentionEvent event) {
        mMentionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Animation animation = AnimationUtils.loadAnimation
                        (mActivity, R.anim.action_bar_out);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mDisplayed = false;
                        if(mentionEvents.contains(event)) {
                            mentionEvents.remove(event);
                        }
                        if(!mentionEvents.isEmpty()) {
                            onMention(mentionEvents.get(0));
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mMentionView.startAnimation(animation);
                mMentionView.setVisibility(View.INVISIBLE);
            }
        }, 2500);
    }
}

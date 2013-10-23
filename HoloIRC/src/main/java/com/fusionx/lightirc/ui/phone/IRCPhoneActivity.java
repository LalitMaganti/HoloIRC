package com.fusionx.lightirc.ui.phone;

import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.ui.ActionsPagerFragment;
import com.fusionx.lightirc.ui.IRCActivity;
import com.fusionx.lightirc.ui.widget.ActionsSlidingMenu;
import com.fusionx.lightirc.ui.widget.DrawerToggle;
import com.fusionx.lightirc.util.UIUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class IRCPhoneActivity extends IRCActivity {
    @Override
    protected void setUpActionsFragment() {
        mActionsSlidingMenu = new ActionsSlidingMenu(this);
        mActionsPagerFragment = (ActionsPagerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.actions_fragment);
        mDrawerToggle = new DrawerToggle(this, mActionsSlidingMenu, R.drawable.ic_drawer,
                R.string.about, R.string.add);
        mActionsSlidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
            @Override
            public void onOpen() {
                mDrawerToggle.onDrawerOpened(mActionsSlidingMenu);
                mActionsPagerFragment.getActionFragmentListener().onOpen();
            }
        });
        mActionsSlidingMenu.setOnCloseListener(new SlidingMenu.OnCloseListener() {
            @Override
            public void onClose() {
                mDrawerToggle.onDrawerClosed(mActionsSlidingMenu);
                mActionsPagerFragment.getIgnoreFragmentListener().onClose();
            }
        });
        mActionsSlidingMenu.setOnScrolledListener(new SlidingMenu.OnScrolledListener() {
            @Override
            public void onScrolled(float offset) {
                mDrawerToggle.onDrawerSlide(mActionsSlidingMenu, offset);
            }
        });

        if (UIUtils.hasHoneycomb()) {
            mActionsSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        } else {
            // get the window background
            final TypedArray a = getTheme().obtainStyledAttributes(new int[]{android.R.attr
                    .windowBackground});
            final int background = a.getResourceId(0, 0);
            a.recycle();
            // take the above view out of
            final ViewGroup contentParent = (ViewGroup) ((ViewGroup) findViewById(android.R.id
                    .content)).getChildAt(0);
            View content = contentParent.getChildAt(1);
            contentParent.removeView(content);
            contentParent.addView(mActionsSlidingMenu);
            mActionsSlidingMenu.setContent(content);
            if (content.getBackground() == null) {
                content.setBackgroundResource(background);
            }
        }
    }
}
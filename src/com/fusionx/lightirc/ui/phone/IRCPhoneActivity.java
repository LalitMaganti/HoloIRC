package com.fusionx.lightirc.ui.phone;

import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.ui.ActionsPagerFragment;
import com.fusionx.lightirc.ui.IRCActivity;
import com.fusionx.lightirc.ui.UserListFragment;
import com.fusionx.lightirc.ui.widget.ActionsSlidingMenu;
import com.fusionx.lightirc.ui.widget.DrawerToggle;
import com.fusionx.lightirc.util.UIUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class IRCPhoneActivity extends IRCActivity {
    @Override
    protected void setUpSlidingMenu() {
        mUserSlidingMenu = (SlidingMenu) findViewById(R.id.slidingmenulayout);
        mUserSlidingMenu.setContent(R.layout.view_pager_fragment);
        mUserSlidingMenu.setMenu(R.layout.sliding_menu_fragment_userlist);
        mUserSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mUserSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        mUserSlidingMenu.setTouchmodeMarginThreshold(10);
        mUserSlidingMenu.setMode(SlidingMenu.RIGHT);
        mUserSlidingMenu.setBehindWidthRes(R.dimen.server_channel_sliding_actions_menu_width);

        mUserListFragment = (UserListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userlist_fragment);

        mUserSlidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
            @Override
            public void onOpen() {
                mUserListFragment.onMenuOpened(getServer(false).getUserChannelInterface()
                        .getChannel(mIRCPagerFragment.getCurrentTitle()));
                onUserListDisplayed();
            }
        });
        mUserSlidingMenu.setOnCloseListener(new SlidingMenu.OnCloseListener() {
            @Override
            public void onClose() {
                getSupportActionBar().setSubtitle(getServer(false).getStatus());
                mUserListFragment.onClose();
            }
        });

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
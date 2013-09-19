package com.fusionx.lightirc.ui.tablet;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.ui.ActionsPagerFragment;
import com.fusionx.lightirc.ui.IRCActivity;
import com.fusionx.lightirc.ui.UserListFragment;
import com.fusionx.lightirc.ui.widget.ActionsSlidingMenu;
import com.fusionx.lightirc.ui.widget.DrawerToggle;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import static com.fusionx.lightirc.util.UIUtils.isLandscape;

public class IRCTabletActivity extends IRCActivity {
    @Override
    protected void setUpContent() {
        setContentView(R.layout.activity_irc);
    }

    @Override
    protected void setUpSlidingMenu() {
        mUserSlidingMenu = (SlidingMenu) findViewById(R.id.user_sliding_menu);
        mUserSlidingMenu.setContent(R.layout.view_pager_fragment);
        mUserSlidingMenu.setMenu(R.layout.sliding_menu_fragment_userlist);
        mUserSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mUserSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        mUserSlidingMenu.setTouchmodeMarginThreshold(10);
        mUserSlidingMenu.setMode(SlidingMenu.RIGHT);
        mUserSlidingMenu.setBehindWidth(R.dimen.user_menu_sliding_width);

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

        if (!isLandscape(this)) {
            mActionsSlidingMenu = new ActionsSlidingMenu(this);
        }
        mActionsPagerFragment = (ActionsPagerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.actions_fragment);

        if (!isLandscape(this)) {
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

            mActionsSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        } else {
            mActionsPagerFragment.getActionFragmentListener().onOpen();
        }
    }
}
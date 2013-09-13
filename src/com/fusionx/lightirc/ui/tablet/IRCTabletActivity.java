package com.fusionx.lightirc.ui.tablet;

import android.support.v4.app.FragmentTransaction;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.ui.ActionsPagerFragment;
import com.fusionx.lightirc.ui.IRCActivity;
import com.fusionx.lightirc.ui.UserListFragment;
import com.fusionx.lightirc.ui.widget.ActionsSlidingMenu;
import com.fusionx.lightirc.ui.widget.DrawerToggle;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class IRCTabletActivity extends IRCActivity {
    @Override
    protected void setUpSlidingMenu() {
        mUserListFragment = (UserListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userlist_fragment);

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mUserListFragment);
        ft.commit();

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

        mActionsSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
    }
}
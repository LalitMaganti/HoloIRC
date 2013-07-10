package com.fusionx.lightirc.ui;

import android.content.Context;
import com.fusionx.lightirc.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class UserListSlidingMenu extends LightSlidingMenu {
    public UserListSlidingMenu(Context context) {
        super(context);
        setMode(SlidingMenu.RIGHT);
        setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        setMenu(R.layout.slding_menu_fragment_user);
    }
}

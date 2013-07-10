package com.fusionx.lightirc.ui;

import android.content.Context;
import com.fusionx.lightirc.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class ActionsSlidingMenu extends LightSlidingMenu {
    public ActionsSlidingMenu(Context context) {
        super(context);
        setMode(SlidingMenu.LEFT);
        setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        setTouchmodeMarginThreshold(5);
        setMenu(R.layout.sliding_menu_fragment_actions);
        setBehindWidthRes(R.dimen.server_channel_sliding_actions_menu_width);
    }
}

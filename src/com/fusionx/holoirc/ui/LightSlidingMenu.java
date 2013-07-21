package com.fusionx.holoirc.ui;

import android.content.Context;
import com.fusionx.holoirc.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

class LightSlidingMenu extends SlidingMenu {
    LightSlidingMenu(Context context) {
        super(context);
        setShadowDrawable(R.drawable.shadow);
    }
}

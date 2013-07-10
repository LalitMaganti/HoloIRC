package com.fusionx.lightirc.ui;

import android.content.Context;
import com.fusionx.lightirc.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class LightSlidingMenu extends SlidingMenu {
    public LightSlidingMenu(Context context) {
        super(context);
        setShadowDrawable(R.drawable.shadow);
    }
}

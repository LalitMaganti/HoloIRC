/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fusionx.lightirc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.appcompat.R;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;

public class SlidingPaneToggleArrow implements SlidingPaneLayout.PanelSlideListener {
    private final ActionBarActivity mActivity;
    private final SlidingPaneLayout mPaneLayout;
    private DrawerArrowDrawable mSlider;

    public SlidingPaneToggleArrow(ActionBarActivity activity, SlidingPaneLayout paneLayout) {
        mActivity = activity;
        mPaneLayout = paneLayout;
        mSlider = new DrawerArrowDrawable(activity.getSupportActionBar().getThemedContext());
    }

    public void syncState() {
        if (mPaneLayout.isOpen()) {
            mSlider.setProgress(1);
        } else {
            mSlider.setProgress(0);
        }
        mActivity.getSupportActionBar().setHomeAsUpIndicator(mSlider);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            toggle();
            return true;
        }
        return false;
    }

    private void toggle() {
        if (mPaneLayout.isOpen()) {
            mPaneLayout.closePane();
        } else {
            mPaneLayout.openPane();
        }
    }

    @Override
    public void onPanelSlide(View view, float slideOffset) {
        mSlider.setProgress(Math.min(1f, Math.max(0, slideOffset)));
    }

    @Override
    public void onPanelOpened(View view) {
        mSlider.setProgress(1);
    }

    @Override
    public void onPanelClosed(View view) {
        mSlider.setProgress(0);
    }
}

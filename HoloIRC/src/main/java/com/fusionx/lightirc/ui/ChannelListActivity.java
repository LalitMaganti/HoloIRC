package com.fusionx.lightirc.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class ChannelListActivity extends ActionBarActivity {

    private ChannelListFragment mChannelListFragment;

    @Override
    public void onBackPressed() {
        mChannelListFragment.onSaveData();

        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());

        super.onCreate(savedInstanceState);

        mChannelListFragment = new ChannelListFragment();

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                mChannelListFragment).commit();
    }
}
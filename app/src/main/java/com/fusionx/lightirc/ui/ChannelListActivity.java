package com.fusionx.lightirc.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fusionx.lightirc.util.UIUtils;

public class ChannelListActivity extends AppCompatActivity {

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
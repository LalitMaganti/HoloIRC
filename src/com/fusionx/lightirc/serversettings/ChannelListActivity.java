package com.fusionx.lightirc.serversettings;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class ChannelListActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                new ListViewSettingsFragment()).commit();
    }
}

package com.fusionx.lightirc.ui;

import android.os.Bundle;

import com.fusionx.lightirc.interfaces.IServerSettings;

import org.holoeverywhere.app.Activity;

public class ChannelListActivity extends Activity implements IServerSettings {
    private String mFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFileName = getIntent().getStringExtra("filename");

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                new ChannelListFragment()).commit();
    }

    @Override
    public String getFileName() {
        return mFileName;
    }

    @Override
    public boolean canSaveChanges() {
        throw new IllegalArgumentException();
    }

    @Override
    public void setCanSaveChanges(boolean canSave) {
        throw new IllegalArgumentException();
    }
}
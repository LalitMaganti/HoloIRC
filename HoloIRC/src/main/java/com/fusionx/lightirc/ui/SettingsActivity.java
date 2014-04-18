package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.util.UIUtils;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt(this));
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.app_settings_headers, target);
        showAlertDialog();
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        return true;
    }

    private void showAlertDialog() {
        if (getIntent().getIntExtra("connectedServers", 0) != 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Modifying these settings while connected to server can cause " +
                    "unexpected behaviour - this is not a bug. It is strongly recommended that you "
                    + "close all connections before modifying these settings.").setTitle("Warning")
                    .setCancelable(false).setPositiveButton(getString(android.R.string.ok), null);
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }
}

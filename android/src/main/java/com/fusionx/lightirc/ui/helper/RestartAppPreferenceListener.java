package com.fusionx.lightirc.ui.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;
import android.support.v7.app.AlertDialog;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.Theme;
import com.fusionx.lightirc.ui.MainActivity;

import static com.fusionx.lightirc.misc.AppPreferences.getAppPreferences;

public class RestartAppPreferenceListener implements Preference.OnPreferenceChangeListener {

    private Context mContext;

    public RestartAppPreferenceListener(final Context context) {
        mContext = context;
    }

    @SuppressLint("PrivateResource")
    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        final AlertDialog.Builder build = new AlertDialog.Builder(mContext,
                getAppPreferences().getTheme() == Theme.DARK
                        ? android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog_Alert
                        : android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        final DialogInterface.OnClickListener listener = (dialogInterface, i) -> {
            final Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(MainActivity.CLEAR_CACHE, true);
            mContext.startActivity(intent);
        };

        build.setMessage(mContext.getString(R.string.appearance_settings_requires_restart))
                .setPositiveButton(mContext.getString(R.string.restart), listener)
                .show();
        return true;
    }
}

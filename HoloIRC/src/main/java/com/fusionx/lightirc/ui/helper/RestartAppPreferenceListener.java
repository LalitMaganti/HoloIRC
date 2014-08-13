package com.fusionx.lightirc.ui.helper;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.ui.MainActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;

public class RestartAppPreferenceListener implements Preference.OnPreferenceChangeListener {

    private Context mContext;

    public RestartAppPreferenceListener(final Context context) {
        mContext = context;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        final AlertDialog.Builder build = new AlertDialog.Builder(mContext);
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

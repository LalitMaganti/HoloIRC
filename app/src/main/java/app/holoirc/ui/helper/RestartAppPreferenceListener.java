package app.holoirc.ui.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;
import android.support.v7.app.AlertDialog;

import app.holoirc.R;
import app.holoirc.ui.MainActivity;

public class RestartAppPreferenceListener implements Preference.OnPreferenceChangeListener {

    private Context mContext;

    public RestartAppPreferenceListener(final Context context) {
        mContext = context;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        final DialogInterface.OnClickListener listener = (dialogInterface, i) -> {
            final Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(MainActivity.CLEAR_CACHE, true);
            mContext.startActivity(intent);
        };

        new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.appearance_settings_requires_restart))
                .setPositiveButton(mContext.getString(R.string.restart), listener)
                .show();
        return true;
    }
}

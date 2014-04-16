package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.misc.PreferenceConstants;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.ui.preferences.NumberPickerPreference;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.relay.ConnectionStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import de.greenrobot.event.EventBus;

class PreferenceHelpers {

    public static void setupNumberPicker(final PreferenceScreen screen) {
        final NumberPickerPreference numberPickerDialogPreference = (NumberPickerPreference)
                screen.findPreference(PreferenceConstants.PREF_RECONNECT_TRIES);
        numberPickerDialogPreference.setSummary(String.valueOf(numberPickerDialogPreference
                .getValue()));
        numberPickerDialogPreference.setOnPreferenceChangeListener(new Preference
                .OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(String.valueOf(newValue));
                return true;
            }
        });
    }

    public static void setupThemePreference(final PreferenceScreen screen,
            final Activity activity) {
        final ListPreference themePreference = (ListPreference) screen.findPreference
                (PreferenceConstants.FRAGMENT_SETTINGS_THEME);
        if (themePreference.getEntry() == null) {
            themePreference.setValue("1");
        }
        themePreference.setOnPreferenceChangeListener(new ThemeChangeListener(activity));
        themePreference.setSummary(themePreference.getEntry());
    }

    public static void setupAppVersionPreference(final PreferenceScreen screen,
            final Context context) {
        final Preference appVersionPreference = screen.findPreference(PreferenceConstants
                .PREF_APP_VERSION);

        if (appVersionPreference != null) {
            appVersionPreference.setSummary(MiscUtils.getAppVersion(context));
        }

        final Preference source = screen.findPreference(PreferenceConstants.PREF_SOURCE);
        if (source != null) {
            source.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            Intent browserIntent = new Intent("android.intent.action.VIEW",
                                    Uri.parse("http://github.com/tilal6991/HoloIRC"));
                            context.startActivity(browserIntent);
                            return true;
                        }
                    }
            );
        }
    }

    static class ThemeChangeListener implements Preference.OnPreferenceChangeListener {

        private final Context mContext;

        ThemeChangeListener(final Context context) {
            mContext = context;
        }

        @Override
        public boolean onPreferenceChange(final Preference preference, final Object o) {
            final AlertDialog.Builder build = new AlertDialog.Builder(mContext);
            build.setMessage(mContext.getString(R.string.appearance_settings_requires_restart))
                    .setPositiveButton(mContext.getString(R.string.restart),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final Intent service = new Intent(mContext, IRCService.class);
                                    mContext.bindService(service, mConnection, 0);
                                }
                            }
                    );
            build.show();
            return true;
        }

        // TODO - this is a hack - fix it
        private void fixCurrentBusSettings() {
            final OnConversationChanged event = EventBus.getDefault().getStickyEvent
                    (OnConversationChanged.class);
            if (event.conversation != null) {
                EventBus.getDefault().postSticky(new OnConversationChanged
                        (null, null));
                EventBus.getDefault().postSticky(new OnCurrentServerStatusChanged
                        (ConnectionStatus.DISCONNECTED));
            }
        }        private final ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName className, final IBinder binder) {
                final IRCService service = ((IRCService.IRCBinder) binder).getService();
                service.disconnectAll();

                fixCurrentBusSettings();

                mContext.unbindService(mConnection);
                final Intent intent = mContext.getPackageManager()
                        .getLaunchIntentForPackage(mContext.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }

            @Override
            public void onServiceDisconnected(final ComponentName name) {
            }
        };


    }
}
package com.fusionx.lightirc.util;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.ui.MainActivity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Set;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class NotificationUtils {

    public static final int NOTIFICATION_MENTION = 242;

    private static final Configuration sConfiguration;

    static {
        sConfiguration = new Configuration.Builder().setDuration(500).build();
    }

    public static void notifyInApp(final Activity activity, final OnChannelMentionEvent event) {
        final Set<String> inApp = AppPreferences.inAppNotification;

        if (inApp.contains(activity.getString(R.string.notification_value_visual))) {
            final String message = String.format("Mentioned in %s on %s", event.channelName,
                    event.serverName);
            final Crouton crouton = Crouton.makeText(activity, message, Style.INFO);
            crouton.setConfiguration(sConfiguration);
            crouton.show();
        }
    }

    public static void notifyOutOfApp(final Context context, final OnChannelMentionEvent event) {
        final Set<String> outApp = AppPreferences.outOfAppNotification;

        if (outApp.contains(context.getString(R.string.notification_value_visual))) {
            final NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // If we're here, the activity has not picked it up - fire off a notification
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_notification);
            builder.setContentTitle(context.getString(R.string.app_name));
            builder.setContentText(String.format("Mentioned in %s on %s",
                    event.channelName, event.serverName));
            builder.setAutoCancel(true);

            final Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent
                    .FLAG_ACTIVITY_SINGLE_TOP);
            resultIntent.putExtra("server_name", event.serverName);
            resultIntent.putExtra("channel_name", event.channelName);

            final PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                    NOTIFICATION_MENTION, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            notificationManager.notify(NOTIFICATION_MENTION, builder.build());
        }
    }
}
package com.fusionx.lightirc.util;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.ui.MainActivity;
import com.fusionx.relay.interfaces.Conversation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import java.util.Set;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;

public class NotificationUtils {

    public static final int NOTIFICATION_MENTION = 242;

    private static final String CANCEL_NOTIFICATION_ACTION = "com.fusionx.lightirc"
            + ".CANCEL_NOTIFICATION";

    private static final String RECEIVE_NOTIFICATION_ACTION = "com.fusionx.lightirc"
            + ".RECEIVE_NOTIFICATION";

    private static final Configuration sConfiguration;

    private static int sNotificationCount = 0;

    private static ResultReceiver sResultReceiver;

    private static DeleteReceiver sDeleteReceiver;

    static {
        sConfiguration = new Configuration.Builder().setDuration(500).build();
    }

    public static void notifyInApp(final Activity activity, final Conversation conversation) {
        final Set<String> inApp = AppPreferences.getAppPreferences()
                .getInAppNotificationSettings();

        if (AppPreferences.getAppPreferences().isInAppNotification()) {
            final String message = String.format("Mentioned in %s on %s", conversation.getId(),
                    conversation.getServer().getTitle());
            final Crouton crouton = Crouton.makeText(activity, message, Style.INFO);
            crouton.setConfiguration(sConfiguration);
            crouton.show();

            if (inApp.contains(activity.getString(R.string.notification_value_audio))) {
                final Uri notification = RingtoneManager.getDefaultUri(TYPE_NOTIFICATION);
                final Ringtone r = RingtoneManager.getRingtone(activity, notification);
                r.play();
            }

            if (inApp.contains(activity.getString(R.string.notification_value_vibrate))) {
                final Vibrator vibrator = (Vibrator) activity.getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(500);
            }
        }
    }

    public static void notifyOutOfApp(final Context context, final Conversation conversation) {
        if (!AppPreferences.getAppPreferences().isOutOfAppNotification()) {
            return;
        }

        registerBroadcastReceivers(context);

        final Set<String> outApp = AppPreferences.getAppPreferences()
                .getOutOfAppNotificationSettings();
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // If we're here, the activity has not picked it up - fire off a notification
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_notification);
        builder.setLargeIcon(icon);
        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setContentTitle(context.getString(R.string.app_name));
        final String text;
        if (sNotificationCount == 0) {
            text = String.format("Mentioned in %s on %s", conversation.getId(),
                    conversation.getServer().getId());
        } else {
            text = "You have been mentioned/queried multiple times";
        }
        builder.setContentText(text);
        builder.setTicker(text);
        builder.setAutoCancel(true);
        builder.setNumber(++sNotificationCount);

        final Intent intent = new Intent(CANCEL_NOTIFICATION_ACTION);
        final PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deleteIntent);

        if (outApp.contains(context.getString(R.string.notification_value_audio))) {
            final Uri notification = RingtoneManager.getDefaultUri(TYPE_NOTIFICATION);
            builder.setSound(notification);
        }
        if (outApp.contains(context.getString(R.string.notification_value_vibrate))) {
            builder.setVibrate(new long[]{0, 500});
        }
        if (outApp.contains(context.getString(R.string.notification_value_lights))) {
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
        }

        final Intent resultIntent = new Intent(RECEIVE_NOTIFICATION_ACTION);
        resultIntent.putExtra("server_name", conversation.getServer().getTitle());
        resultIntent.putExtra("channel_name", conversation.getId());
        final PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context,
                NOTIFICATION_MENTION, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        notificationManager.notify(NOTIFICATION_MENTION, builder.build());
    }

    private static void registerBroadcastReceivers(Context context) {
        if (sResultReceiver == null) {
            sResultReceiver = new ResultReceiver();
            context.registerReceiver(sResultReceiver,
                    new IntentFilter(RECEIVE_NOTIFICATION_ACTION));
        }
        if (sDeleteReceiver == null) {
            sDeleteReceiver = new DeleteReceiver();
            context.registerReceiver(sDeleteReceiver, new IntentFilter(CANCEL_NOTIFICATION_ACTION));
        }
    }

    public static class ResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            sNotificationCount = 0;
            final Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activityIntent.putExtra("server_name", intent.getStringExtra("server_name"));
            activityIntent.putExtra("channel_name", intent.getStringExtra("channel_name"));
            context.startActivity(activityIntent);
        }
    }

    public static class DeleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            sNotificationCount = 0;
            context.unregisterReceiver(this);
            sDeleteReceiver = null;
        }
    }
}
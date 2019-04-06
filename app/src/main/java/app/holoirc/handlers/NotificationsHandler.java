package app.holoirc.handlers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import app.holoirc.misc.NotificationChannels;

public class NotificationsHandler {

    private static NotificationManager notificationManager;

    public static void init(Context context) {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_LOW;

        // status update channel
        NotificationChannel statusUpdateChannel = new NotificationChannel(NotificationChannels.NOTIFICATION_STATUS_UPDATE, "Status Update", importance);
        statusUpdateChannel.enableVibration(true);

        NotificationChannel reconnectChannel = new NotificationChannel(NotificationChannels.NOTIFICATION_RECONNECT, "Reconnect", importance);
        reconnectChannel.enableVibration(true);

        notificationManager.createNotificationChannel(statusUpdateChannel);
        notificationManager.createNotificationChannel(reconnectChannel);
    }

}
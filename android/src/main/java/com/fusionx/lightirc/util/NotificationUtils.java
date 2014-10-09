package com.fusionx.lightirc.util;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.ui.MainActivity;
import com.fusionx.lightirc.view.Snackbar;

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
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.event.Event;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;

public class NotificationUtils {

    public static final int NOTIFICATION_MENTION = 242;

    private static final String CANCEL_NOTIFICATION_ACTION = "com.fusionx.lightirc"
            + ".CANCEL_NOTIFICATION";

    private static final String RECEIVE_NOTIFICATION_ACTION = "com.fusionx.lightirc"
            + ".RECEIVE_NOTIFICATION";

    private static final int MAX_NOTIFICATION_LINES = 6;

    private static int sNotificationCount = 0;
    private static List<Pair<String, CharSequence>> sNotificationMessages = new ArrayList<>();

    private static ResultReceiver sResultReceiver;

    private static DeleteReceiver sDeleteReceiver;

    public static void notifyInApp(final Snackbar snackbar, final Activity activity,
            final Conversation conversation) {
        final Set<String> inApp = AppPreferences.getAppPreferences()
                .getInAppNotificationSettings();

        if (AppPreferences.getAppPreferences().isInAppNotification()) {
            final String message = activity.getString(R.string.notification_mentioned_title,
                    conversation.getId(), conversation.getServer().getTitle());
            snackbar.display(message);

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

    public static void notifyOutOfApp(final Context context,
            final Conversation<? extends Event> conversation, final boolean channel) {
        if (!AppPreferences.getAppPreferences().isOutOfAppNotification()) {
            return;
        }

        registerBroadcastReceivers(context);

        final Server server = conversation.getServer();
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
        builder.setAutoCancel(true);
        builder.setNumber(++sNotificationCount);

        CharSequence message = getLatestMessageForConversation(context, conversation);
        if (message != null) {
            if (sNotificationMessages.size() >= MAX_NOTIFICATION_LINES) {
                sNotificationMessages.remove(0);
            }
            sNotificationMessages.add(Pair.create(server.getId(), message));
        }

        final String text;
        if (sNotificationCount == 1) {
            text = context.getString(R.string.notification_mentioned_title,
                    conversation.getId(), server.getId());
            if (message != null) {
                String title = context.getString(R.string.notification_mentioned_bigtext_title,
                        conversation.getId(), server.getId());
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)
                        .setBigContentTitle(title));
            }
        } else {
            text = context.getString(R.string.notification_mentioned_multi_title,
                    sNotificationCount);
            if (!sNotificationMessages.isEmpty()) {
                String serverId = sNotificationMessages.get(0).first;
                for (Pair<String, CharSequence> entry : sNotificationMessages) {
                    if (!TextUtils.equals(serverId, entry.first)) {
                        serverId = null;
                        break;
                    }
                }

                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                style.setBigContentTitle(text);
                for (Pair<String, CharSequence> entry : sNotificationMessages) {
                    if (serverId != null) {
                        // all messages are for the same server
                        style.addLine(entry.second);
                    } else {
                        // prepend server name
                        style.addLine(String.format("[%s] %s", entry.first, entry.second));
                    }
                }

                if (serverId != null) {
                    style.setSummaryText(serverId);
                } else {
                    style.setSummaryText(context.getString(R.string.app_name));
                }
                builder.setStyle(style);
            }
        }

        builder.setContentText(text);
        builder.setTicker(text);

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
        resultIntent.putExtra(channel ? "channel_name" : "query_nick", conversation.getId());

        final PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context,
                NOTIFICATION_MENTION, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        notificationManager.notify(NOTIFICATION_MENTION, builder.build());
    }

    public static void cancelMentionNotification(final Context context) {
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(NOTIFICATION_MENTION);
    }

    private static CharSequence getLatestMessageForConversation(
            Context context, Conversation<? extends Event> conversation) {
        List<? extends Event> events = conversation.getBuffer();
        if (events == null || events.isEmpty()) {
            return null;
        }
        EventCache cache = IRCService.getEventCache(conversation.getServer());
        CharSequence message = cache.get(events.get(events.size() - 1)).getMessage();
        if (message == null || !(message instanceof Spanned)) {
            return message;
        }

        // As not all colors look nice in the notification,
        // replace them with a neutral color
        Spannable spannable = (Spannable) message;
        Object[] colorSpans = spannable.getSpans(0, spannable.length(),
                ForegroundColorSpan.class);
        for (Object span : colorSpans) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            TextAppearanceSpan newSpan = new TextAppearanceSpan(context,
                    R.style.TextAppearance_StatusBar_EventContent_Emphasized);
            spannable.removeSpan(span);
            spannable.setSpan(newSpan, start, end, 0);
        }
        return spannable;
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
            sNotificationMessages.clear();
            final Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activityIntent.putExtra("server_name", intent.getStringExtra("server_name"));
            activityIntent.putExtra("channel_name", intent.getStringExtra("channel_name"));
            activityIntent.putExtra("query_nick", intent.getStringExtra("query_nick"));
            context.startActivity(activityIntent);
        }
    }

    public static class DeleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            sNotificationCount = 0;
            sNotificationMessages.clear();
            context.unregisterReceiver(this);
            sDeleteReceiver = null;
        }
    }
}
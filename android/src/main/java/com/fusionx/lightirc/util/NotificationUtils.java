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
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.TextAppearanceSpan;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.Nick;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.event.channel.ChannelWorldActionEvent;
import co.fusionx.relay.event.channel.ChannelWorldMessageEvent;
import co.fusionx.relay.event.query.QueryActionWorldEvent;
import co.fusionx.relay.event.query.QueryMessageWorldEvent;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;

public class NotificationUtils {

    private static final int NOTIFICATION_MENTION_BASE = 10000;
    private static final int NOTIFICATION_MENTION_SERVER_OFFSET = 1000;

    private static final String CANCEL_NOTIFICATION_ACTION = "com.fusionx.lightirc"
            + ".CANCEL_NOTIFICATION";

    private static final String RECEIVE_NOTIFICATION_ACTION = "com.fusionx.lightirc"
            + ".RECEIVE_NOTIFICATION";

    private static final int MAX_NOTIFICATIONS_PER_SERVER = NOTIFICATION_MENTION_SERVER_OFFSET - 2;

    private static class NotificationMessageInfo {
        final Nick user;
        final Conversation<? extends Event> conversation;
        final String message;
        final CharSequence messageWithPrependedNick;
        final boolean mention;
        final long timestamp;
        final int conversationBufferIndex;
        public NotificationMessageInfo(Context context, Nick user,
                Conversation<? extends Event> conversation, String message,
                boolean mention, long timestamp) {
            this.user = user;
            this.conversation = conversation;
            this.message = message;
            this.mention = mention;
            this.timestamp = timestamp;
            this.messageWithPrependedNick = user != null ?
                    prependHighlightedText(context, user.getNickAsString(), message) : message;

            int index;
            for (index = conversation.getBuffer().size() - 1; index >= 0; index--) {
                Event e = conversation.getBuffer().get(index);
                boolean eventMatches = mention
                        ? (e instanceof ChannelWorldMessageEvent || e instanceof ChannelWorldActionEvent)
                        : (e instanceof QueryMessageWorldEvent || e instanceof QueryActionWorldEvent);
                if (eventMatches && e.timestamp.toMillis(false) == timestamp) {
                    break;
                }
            }
            this.conversationBufferIndex = index;
        }
    }

    private static class NotificationServerInfo {
        int mentionCount;
        int queryCount;
        List<NotificationMessageInfo> messages;
        Map<Conversation, Integer> subNotificationIds;
        int nextSubNotificationId;

        public NotificationServerInfo() {
            mentionCount = 0;
            queryCount = 0;
            messages = new ArrayList<>();
            subNotificationIds = new HashMap<>();
            nextSubNotificationId = 1;
        }
        public int getNextSubNotificationId() {
            return nextSubNotificationId++;
        }
    }

    private static Map<String, NotificationServerInfo> sNotificationInfos = new HashMap<>();

    private static ResultReceiver sResultReceiver;

    private static DeleteReceiver sDeleteReceiver;

    public static void notifyInApp(final Snackbar snackbar, final Activity activity,
            final Conversation conversation, boolean channel) {
        final Set<String> inApp = AppPreferences.getAppPreferences()
                .getInAppNotificationSettings();

        if (AppPreferences.getAppPreferences().isInAppNotification()) {
            int messageResId = channel
                    ? R.string.notification_mentioned_title : R.string.notification_queried_title;
            final String message = activity.getString(messageResId,
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

    public static void notifyOutOfApp(final Context context, final String message,
            Nick user, final Conversation<? extends Event> conversation,
            final boolean channel, final long timestamp) {
        if (!AppPreferences.getAppPreferences().isOutOfAppNotification()) {
            return;
        }

        registerBroadcastReceivers(context);

        final Server server = conversation.getServer();
        final Set<String> outApp = AppPreferences.getAppPreferences()
                .getOutOfAppNotificationSettings();
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationServerInfo serverInfo = sNotificationInfos.get(server.getId());
        if (serverInfo == null) {
            serverInfo = new NotificationServerInfo();
            sNotificationInfos.put(server.getId(), serverInfo);
        }

        if (channel) {
            serverInfo.mentionCount++;
        } else {
            serverInfo.queryCount++;
        }

        NotificationMessageInfo messageInfo = null;
        if (message != null) {
            if (serverInfo.messages.size() >= MAX_NOTIFICATIONS_PER_SERVER) {
                serverInfo.messages.remove(0);
            }
            messageInfo = new NotificationMessageInfo(context,
                    user, conversation, message, channel, timestamp);
            serverInfo.messages.add(messageInfo);
        }

        int pos = 0;
        for (String serverId : sNotificationInfos.keySet()) {
            if (TextUtils.equals(serverId, server.getId())) {
                break;
            }
            pos++;
        }

        final int notificationIdBase =
                NOTIFICATION_MENTION_BASE + pos * NOTIFICATION_MENTION_SERVER_OFFSET;

        int totalNotificationCount = serverInfo.mentionCount + serverInfo.queryCount;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setAutoCancel(true);
        builder.setNumber(totalNotificationCount);
        builder.setColor(context.getResources().getColor(R.color.colorPrimary));
        builder.setCategory(NotificationCompat.CATEGORY_EMAIL);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setGroup(server.getId());
        builder.setGroupSummary(true);

        final Intent resultIntent = new Intent(RECEIVE_NOTIFICATION_ACTION);
        resultIntent.putExtra("server_name", conversation.getServer().getTitle());
        resultIntent.putExtra(channel ? "channel_name" : "query_nick", conversation.getId());

        final PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context,
                notificationIdBase, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        // First build the public version...
        builder.setContentText(buildNotificationContentTextWithCounts(context, serverInfo));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        final Notification publicVersion = builder.build();

        // ... and now the private one
        builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
        builder.setPublicVersion(publicVersion);

        final String text;
        if (totalNotificationCount == 1) {
            int titleResId = channel
                    ? R.string.notification_mentioned_title : R.string.notification_queried_title;
            text = context.getString(titleResId, conversation.getId(), server.getId());

            // For PMs, make sure to not include the sender's name in the
            // message (it's part of the title already)
            final CharSequence bigTextMessage = messageInfo != null && messageInfo.mention
                    ? messageInfo.messageWithPrependedNick : message;
            if (bigTextMessage != null) {
                String title = context.getString(R.string.notification_mentioned_bigtext_title,
                        conversation.getId(), server.getId());
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(ensureMinimumSize(bigTextMessage))
                        .setBigContentTitle(title));
            }
        } else {
            text = buildNotificationContentTextWithCounts(context, serverInfo);

            if (!serverInfo.messages.isEmpty()) {
                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                style.setBigContentTitle(text);
                for (NotificationMessageInfo entry : serverInfo.messages) {
                    style.addLine(entry.messageWithPrependedNick);
                }

                style.setSummaryText(server.getId());
                builder.setStyle(style);
            }
        }

        builder.setContentText(text);
        builder.setTicker(text);

        int defaults = 0;
        if (outApp.contains(context.getString(R.string.notification_value_audio))) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (outApp.contains(context.getString(R.string.notification_value_vibrate))) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (outApp.contains(context.getString(R.string.notification_value_lights))) {
            defaults |= Notification.DEFAULT_LIGHTS;
        }

        builder.setDefaults(defaults);

        final Intent intent = new Intent(CANCEL_NOTIFICATION_ACTION);
        final PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deleteIntent);

        notificationManager.notify(notificationIdBase, builder.build());

        Integer subNotifId = serverInfo.subNotificationIds.get(conversation);
        if (subNotifId == null) {
            subNotifId = serverInfo.getNextSubNotificationId();
            serverInfo.subNotificationIds.put(conversation, subNotifId);
        }

        SpannableStringBuilder convText = new SpannableStringBuilder();
        List<NotificationMessageInfo> convInfos = new ArrayList<>();
        for (NotificationMessageInfo info : serverInfo.messages) {
            if (info.conversation == conversation) {
                if (convText.length() == 0) {
                    convText.append("\n");
                }
                convText.append(info.mention
                        ? info.messageWithPrependedNick : info.message);
                convInfos.add(info);
            }
        }

        NotificationCompat.Builder convLogBuilder = new NotificationCompat.Builder(context);
        convLogBuilder.setStyle(new NotificationCompat.BigTextStyle(convLogBuilder)
                .bigText(buildConversationLogForWear(context, conversation, convInfos)));
        convLogBuilder.extend(new NotificationCompat.WearableExtender()
                .setStartScrollBottom(true));

        final NotificationCompat.Builder stackBuilder =
                new NotificationCompat.Builder(context);
        stackBuilder.setSmallIcon(R.drawable.ic_notification_small);
        stackBuilder.setContentTitle(context.getString(
                R.string.notification_mentioned_bigtext_title,
                conversation.getId(), server.getId()));
        stackBuilder.setContentText(convText);
        stackBuilder.setColor(context.getResources().getColor(R.color.colorPrimary));
        stackBuilder.setCategory(NotificationCompat.CATEGORY_EMAIL);
        stackBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        stackBuilder.setGroup(server.getId());
        stackBuilder.extend(new NotificationCompat.WearableExtender()
                .addPage(convLogBuilder.build()));

        notificationManager.notify(notificationIdBase + subNotifId, stackBuilder.build());
    }

    private static boolean shouldIncludeEventInConversationLog(Event e) {
        return e instanceof ChannelWorldMessageEvent
                || e instanceof ChannelWorldActionEvent
                || e instanceof QueryMessageWorldEvent
                || e instanceof QueryActionWorldEvent;
    }

    private static CharSequence buildConversationLogForWear(Context context,
            Conversation<? extends Event> conversation,
            List<NotificationMessageInfo> infos) {
        List<Event> loggedEvents = new ArrayList<>();
        List<? extends Event> buffer = conversation.getBuffer();

        for (int i = 0; i < infos.size(); i++) {
            NotificationMessageInfo info = infos.get(i);
            int lastBufferIndex = i == 0 ? -1 : infos.get(i - 1).conversationBufferIndex;
            int nextBufferIndex = i == (infos.size() - 1)
                    ? buffer.size() : infos.get(i + 1).conversationBufferIndex;

            loggedEvents.add(null);
            // TODO: indicate gaps between previous chunk and this one
            int insertPos = loggedEvents.size();

            // go back up to 20 messages
            for (int index = info.conversationBufferIndex - 1, remaining = 20;
                    index > lastBufferIndex && remaining > 0; index--) {
                Event e = buffer.get(index);
                if (shouldIncludeEventInConversationLog(e)) {
                    loggedEvents.add(insertPos, e);
                    remaining--;
                }
            }

            // go forward up to 20 messages
            for (int index = info.conversationBufferIndex, remaining = 20;
                 index < nextBufferIndex && remaining > 0; index++) {
                Event e = buffer.get(index);
                if (shouldIncludeEventInConversationLog(e)) {
                    loggedEvents.add(e);
                    remaining--;
                }
            }
        }

        EventCache cache = IRCService.getEventCache(conversation.getServer());
        SpannableStringBuilder convLog = new SpannableStringBuilder();
        java.text.DateFormat format = DateFormat.getTimeFormat(context);
        boolean logTimestamp = false;
        for (Event e : loggedEvents) {
            if (e == null) {
                logTimestamp = true;
                continue;
            }
            if (logTimestamp) {
                convLog.append(format.format(new Date(e.timestamp.toMillis(false))));
                convLog.append("\n");
                logTimestamp = false;
            }
            convLog.append(cache.get(e).getMessage());
            convLog.append("\n");
        }

        return convLog;
    }

    private static CharSequence ensureMinimumSize(CharSequence message) {
        String messageString = message.toString();
        if (messageString.length() >= 50 || messageString.indexOf('\n') >= 0) {
            return message;
        }
        if (message instanceof SpannableStringBuilder) {
            SpannableStringBuilder builder = (SpannableStringBuilder) message;
            builder.append("\n");
            return builder;
        }
        return messageString + "\n";
    }

    private static String buildNotificationContentTextWithCounts(Context context,
            NotificationServerInfo info) {
        if (info.queryCount > 0 && info.mentionCount > 0) {
            Resources res = context.getResources();
            String mentions = res.getQuantityString(R.plurals.mention,
                    info.mentionCount, info.mentionCount);
            String queries = res.getQuantityString(R.plurals.query,
                    info.queryCount, info.queryCount);
            return mentions + ", " + queries;
        } else if (info.mentionCount > 0) {
            return context.getString(R.string.notification_mentioned_multi_title,
                    info.mentionCount);
        } else {
            return context.getString(R.string.notification_queried_multi_title,
                    info.queryCount);
        }
    }

    public static void cancelMentionNotification(final Context context, final Server server) {
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int index = 0;
        for (Map.Entry<String, NotificationServerInfo> entry : sNotificationInfos.entrySet()) {
            if (server == null || server.getId().equals(entry.getKey())) {
                int baseId = NOTIFICATION_MENTION_BASE + index * NOTIFICATION_MENTION_SERVER_OFFSET;
                NotificationServerInfo info = entry.getValue();
                notificationManager.cancel(baseId);
                for (Integer subNotifId : info.subNotificationIds.values()) {
                    notificationManager.cancel(baseId + subNotifId);
                }
            }
            index++;
        }
        resetNotificationState();
    }

    private static CharSequence prependHighlightedText(Context context,
            String prefix, CharSequence message) {
        if (message == null || prefix == null) {
            return message;
        }

        TextAppearanceSpan highlightSpan = new TextAppearanceSpan(context,
                R.style.TextAppearance_StatusBar_EventContent_Emphasized);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(prefix);
        builder.setSpan(highlightSpan, 0, builder.length(), 0);
        builder.append(" ");
        builder.append(message);

        return builder;
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

    private static void resetNotificationState() {
        sNotificationInfos.clear();
    }

    public static class ResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            resetNotificationState();
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
            resetNotificationState();
            context.unregisterReceiver(this);
            sDeleteReceiver = null;
        }
    }
}

package app.holoirc.util;

import app.holoirc.R;
import app.holoirc.misc.AppPreferences;
import app.holoirc.misc.EventCache;
import app.holoirc.service.IRCService;
import app.holoirc.ui.MainActivity;
import app.holoirc.view.Snackbar;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.Nick;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.event.channel.ChannelActionEvent;
import co.fusionx.relay.event.channel.ChannelMessageEvent;
import co.fusionx.relay.event.channel.ChannelWorldActionEvent;
import co.fusionx.relay.event.channel.ChannelWorldMessageEvent;
import co.fusionx.relay.event.query.QueryActionSelfEvent;
import co.fusionx.relay.event.query.QueryActionWorldEvent;
import co.fusionx.relay.event.query.QueryMessageSelfEvent;
import co.fusionx.relay.event.query.QueryMessageWorldEvent;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;

public class NotificationUtils {

    private static final int NOTIFICATION_MENTION_BASE = 10000;
    private static final int NOTIFICATION_MENTION_SERVER_OFFSET = 1000;

    private static final String CANCEL_NOTIFICATION_ACTION =
            "app.holoirc.CANCEL_NOTIFICATION";
    private static final String VOICE_REPLY_NOTIFICATION_ACTION =
            "app.holoirc.VOICE_REPLY";

    private static final int MAX_NOTIFICATIONS_PER_SERVER = NOTIFICATION_MENTION_SERVER_OFFSET - 2;
    private static final int CONV_LOG_CONTEXT_ENTRIES = 10;

    private static class NotificationMessageInfo {
        final Context context;
        final Nick user;
        final Conversation<? extends Event> conversation;
        final String message;

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
            this.context = context.getApplicationContext();

            this.conversationBufferIndex = determineConversationBufferIndex();
        }
        private int determineConversationBufferIndex() {
            for (int index = conversation.getBuffer().size() - 1; index >= 0; index--) {
                Event e = conversation.getBuffer().get(index);
                boolean eventMatches = mention
                        ? (e instanceof ChannelWorldMessageEvent || e instanceof ChannelWorldActionEvent)
                        : (e instanceof QueryMessageWorldEvent || e instanceof QueryActionWorldEvent);
                if (eventMatches && e.timestamp.getTime() == timestamp) {
                    return index;
                }
            }
            return 0;
        }
        public CharSequence messageWithPrependedNick() {
            if (user != null) {
                return prependHighlightedText(context, user.getNickAsString(), message);
            }
            return message;
        }
    }

    private static class NotificationServerInfo {
        int mentionCount;
        int queryCount;
        final List<NotificationMessageInfo> messages;
        final Map<Conversation, Integer> subNotificationIds;
        final int notificationIdBase;
        int nextSubNotificationId;

        public NotificationServerInfo(int index) {
            mentionCount = 0;
            queryCount = 0;
            messages = new ArrayList<>();
            notificationIdBase =
                    NOTIFICATION_MENTION_BASE + index * NOTIFICATION_MENTION_SERVER_OFFSET;
            subNotificationIds = new HashMap<>();
            nextSubNotificationId = 1;
        }
        public int getNextSubNotificationId() {
            return nextSubNotificationId++;
        }
    }

    private static Map<String, NotificationServerInfo> sNotificationInfos = new HashMap<>();
    private static int sNextServerInfoIndex = 0;

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

        final Server server = conversation.getServer();
        final Set<String> outApp = AppPreferences.getAppPreferences()
                .getOutOfAppNotificationSettings();
        final NotificationManagerCompat nm = NotificationManagerCompat.from(context);

        NotificationServerInfo serverInfo = sNotificationInfos.get(server.getId());
        if (serverInfo == null) {
            serverInfo = new NotificationServerInfo(sNextServerInfoIndex++);
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

        int totalNotificationCount = serverInfo.mentionCount + serverInfo.queryCount;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setNumber(totalNotificationCount)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setCategory(NotificationCompat.CATEGORY_EMAIL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(server.getId())
                .setGroupSummary(true);

        final Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent.putExtra("server_name", conversation.getServer().getTitle());
        contentIntent.putExtra(channel ? "channel_name" : "query_nick", conversation.getId());

        final PendingIntent contentPendingIntent = PendingIntent.getActivity(context,
                serverInfo.notificationIdBase, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentPendingIntent);

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
                    ? messageInfo.messageWithPrependedNick() : message;
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
                    style.addLine(entry.messageWithPrependedNick());
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

        final Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(CANCEL_NOTIFICATION_ACTION);
        intent.putExtra("server_id", server.getId());
        final PendingIntent deleteIntent = PendingIntent.getBroadcast(context,
                serverInfo.notificationIdBase, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deleteIntent);

        nm.notify(serverInfo.notificationIdBase, builder.build());

        if (isWearAppInstalled(context)) {
            Integer subNotifId = serverInfo.subNotificationIds.get(conversation);
            if (subNotifId == null) {
                subNotifId = serverInfo.getNextSubNotificationId();
                serverInfo.subNotificationIds.put(conversation, subNotifId);
            }

            SpannableStringBuilder convText = new SpannableStringBuilder();
            List<NotificationMessageInfo> convInfos = new ArrayList<>();
            for (NotificationMessageInfo info : serverInfo.messages) {
                if (info.conversation != conversation) {
                    continue;
                }
                if (convText.length() != 0) {
                    convText.append("\n");
                }
                if (info.mention) {
                    int currentPos = convText.length();
                    convText.append(info.user.getNickAsString());
                    convText.setSpan(new StyleSpan(Typeface.ITALIC), currentPos, convText.length(),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    convText.append(" ");
                }
                convText.append(info.message);
                convInfos.add(info);
            }

            CharSequence conversationLog = buildConversationLogForWear(context,
                    conversation, convInfos);
            final Notification convLogPage = new NotificationCompat.Builder(context)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(conversationLog))
                    .extend(new NotificationCompat.WearableExtender().setStartScrollBottom(true))
                    .build();

            final String replyLabel = context.getString(R.string.notification_reply_wear_title,
                    conversation.getId());
            final RemoteInput remoteInput = new RemoteInput.Builder("reply")
                    .setLabel(replyLabel)
                    .build();
            final Intent replyIntent = new Intent(context, NotificationEventReceiver.class);
            replyIntent.setAction(VOICE_REPLY_NOTIFICATION_ACTION);
            replyIntent.putExtra("server_name", server.getTitle());
            replyIntent.putExtra("channel", channel);
            replyIntent.putExtra("conversation_id", conversation.getId());

            final PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context,
                    serverInfo.notificationIdBase + subNotifId, replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    R.drawable.ic_reply,
                    context.getString(R.string.notification_action_reply),
                    replyPendingIntent)
                    .addRemoteInput(remoteInput)
                    .build();

            final NotificationCompat.Builder stackBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setContentTitle(context.getString(
                            R.string.notification_mentioned_bigtext_title,
                            conversation.getId(), server.getId()))
                    .setContentText(convText)
                    .setColor(context.getResources().getColor(R.color.colorPrimary))
                    .setCategory(NotificationCompat.CATEGORY_EMAIL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setGroup(server.getId())
                    .extend(new NotificationCompat.WearableExtender()
                            .addPage(convLogPage)
                            .addAction(replyAction));

            nm.notify(serverInfo.notificationIdBase + subNotifId, stackBuilder.build());
        }
    }

    private static boolean shouldIncludeEventInConversationLog(Event e) {
        return e instanceof ChannelWorldMessageEvent
                || e instanceof ChannelMessageEvent
                || e instanceof ChannelWorldActionEvent
                || e instanceof ChannelActionEvent
                || e instanceof QueryMessageWorldEvent
                || e instanceof QueryMessageSelfEvent
                || e instanceof QueryActionWorldEvent
                || e instanceof QueryActionSelfEvent;
    }

    private static CharSequence buildConversationLogForWear(Context context,
            Conversation<? extends Event> conversation,
            List<NotificationMessageInfo> infos) {
        List<Object> logEntries = new ArrayList<>();
        List<? extends Event> buffer = conversation.getBuffer();
        int lastLoggedIndex = -1;

        for (int i = 0; i < infos.size(); i++) {
            NotificationMessageInfo info = infos.get(i);
            int lastBufferIndex = i == 0 ? -1 : infos.get(i - 1).conversationBufferIndex;
            int nextBufferIndex = i == (infos.size() - 1)
                    ? buffer.size() : infos.get(i + 1).conversationBufferIndex;

            int insertPos = logEntries.size();
            int skipped = 0;

            // go back
            for (int index = info.conversationBufferIndex - 1, remaining = CONV_LOG_CONTEXT_ENTRIES;
                    index > lastBufferIndex && index > lastLoggedIndex; index--) {
                Event e = buffer.get(index);
                if (shouldIncludeEventInConversationLog(e)) {
                    if (remaining > 0) {
                        logEntries.add(insertPos, e);
                        remaining--;
                    } else {
                        skipped++;
                    }
                }
            }

            if (skipped > 0) {
                logEntries.add(insertPos, new Integer(skipped));
            }

            // go forward
            for (int index = info.conversationBufferIndex, remaining = CONV_LOG_CONTEXT_ENTRIES;
                 index < nextBufferIndex && remaining > 0; index++) {
                Event e = buffer.get(index);
                if (shouldIncludeEventInConversationLog(e)) {
                    logEntries.add(e);
                    lastLoggedIndex = index;
                    remaining--;
                }
            }
        }

        EventCache cache = IRCService.getEventCache(conversation.getServer(), false);
        SpannableStringBuilder convLog = new SpannableStringBuilder();
        int skippedColor = context.getResources().getColor(R.color.light_grey);

        for (Object o : logEntries) {
            if (o instanceof Integer) {
                int skippedCount = (Integer) o;
                int startPos = convLog.length();
                convLog.append(context.getResources().getQuantityString(
                        R.plurals.skipped_entry_count, skippedCount, skippedCount));
                convLog.setSpan(new StyleSpan(Typeface.ITALIC), startPos, convLog.length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                convLog.setSpan(new ForegroundColorSpan(skippedColor), startPos, convLog.length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
                Event e = (Event) o;
                convLog.append(cache.get(e).getMessage());
            }
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
            SpannableStringBuilder builder = new SpannableStringBuilder(message);
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
        final NotificationManagerCompat nm = NotificationManagerCompat.from(context);

        for (Map.Entry<String, NotificationServerInfo> entry : sNotificationInfos.entrySet()) {
            if (server == null || server.getId().equals(entry.getKey())) {
                NotificationServerInfo info = entry.getValue();
                nm.cancel(info.notificationIdBase);
                for (Integer subNotifId : info.subNotificationIds.values()) {
                    nm.cancel(info.notificationIdBase + subNotifId);
                }
            }
        }
        if (server != null) {
            sNotificationInfos.remove(server.getId());
        } else {
            sNotificationInfos.clear();
        }
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

    private static boolean isWearAppInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.google.android.wearable.app", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static class NotificationEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();

            if (CANCEL_NOTIFICATION_ACTION.equals(action)) {
                String serverId = intent.getStringExtra("server_id");
                sNotificationInfos.remove(serverId);
            } else if (VOICE_REPLY_NOTIFICATION_ACTION.equals(action)) {
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput != null) {
                    boolean channel = intent.getBooleanExtra("channel", false);
                    Intent serviceIntent = new Intent(context, IRCService.class);
                    serviceIntent.setAction(IRCService.ADD_MESSAGE_INTENT);
                    serviceIntent.putExtra(IRCService.EXTRA_SERVER_NAME,
                            intent.getStringExtra("server_name"));
                    serviceIntent.putExtra(
                            channel ? IRCService.EXTRA_CHANNEL_NAME : IRCService.EXTRA_QUERY_NICK,
                            intent.getStringExtra("conversation_id"));
                    serviceIntent.putExtra(IRCService.EXTRA_MESSAGE,
                            remoteInput.getCharSequence("reply"));
                    context.startService(serviceIntent);
                }
            }
        }
    }
}

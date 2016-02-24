package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.model.NickColour;
import com.fusionx.lightirc.util.CrashUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import co.fusionx.relay.base.ChannelUser;
import co.fusionx.relay.base.Nick;
import co.fusionx.relay.dcc.chat.DCCChatWorldActionEvent;
import co.fusionx.relay.dcc.event.chat.DCCChatSelfActionEvent;
import co.fusionx.relay.dcc.event.chat.DCCChatSelfMessageEvent;
import co.fusionx.relay.dcc.event.chat.DCCChatStartedEvent;
import co.fusionx.relay.dcc.event.chat.DCCChatWorldMessageEvent;
import co.fusionx.relay.dcc.event.file.DCCFileGetStartedEvent;
import co.fusionx.relay.dcc.pending.DCCPendingChatConnection;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.event.channel.ChannelActionEvent;
import co.fusionx.relay.event.channel.ChannelConnectEvent;
import co.fusionx.relay.event.channel.ChannelDisconnectEvent;
import co.fusionx.relay.event.channel.ChannelInitialTopicEvent;
import co.fusionx.relay.event.channel.ChannelMessageEvent;
import co.fusionx.relay.event.channel.ChannelModeEvent;
import co.fusionx.relay.event.channel.ChannelNickChangeEvent;
import co.fusionx.relay.event.channel.ChannelNoticeEvent;
import co.fusionx.relay.event.channel.ChannelStopEvent;
import co.fusionx.relay.event.channel.ChannelTopicEvent;
import co.fusionx.relay.event.channel.ChannelUserLevelChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldActionEvent;
import co.fusionx.relay.event.channel.ChannelWorldJoinEvent;
import co.fusionx.relay.event.channel.ChannelWorldKickEvent;
import co.fusionx.relay.event.channel.ChannelWorldLevelChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldMessageEvent;
import co.fusionx.relay.event.channel.ChannelWorldNickChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldPartEvent;
import co.fusionx.relay.event.channel.ChannelWorldQuitEvent;
import co.fusionx.relay.event.channel.PartEvent;
import co.fusionx.relay.event.query.QueryActionSelfEvent;
import co.fusionx.relay.event.query.QueryActionWorldEvent;
import co.fusionx.relay.event.query.QueryConnectEvent;
import co.fusionx.relay.event.query.QueryDisconnectEvent;
import co.fusionx.relay.event.query.QueryMessageSelfEvent;
import co.fusionx.relay.event.query.QueryMessageWorldEvent;
import co.fusionx.relay.event.query.QueryOpenedEvent;
import co.fusionx.relay.event.query.QueryStopEvent;
import co.fusionx.relay.event.server.ConnectEvent;
import co.fusionx.relay.event.server.ConnectingEvent;
import co.fusionx.relay.event.server.DCCChatRequestEvent;
import co.fusionx.relay.event.server.DisconnectEvent;
import co.fusionx.relay.event.server.ErrorEvent;
import co.fusionx.relay.event.server.GenericServerEvent;
import co.fusionx.relay.event.server.InviteEvent;
import co.fusionx.relay.event.server.KickEvent;
import co.fusionx.relay.event.server.MotdEvent;
import co.fusionx.relay.event.server.NoticeEvent;
import co.fusionx.relay.event.server.ReconnectEvent;
import co.fusionx.relay.event.server.ServerNickChangeEvent;
import co.fusionx.relay.event.server.StopEvent;
import co.fusionx.relay.event.server.WallopsEvent;
import co.fusionx.relay.event.server.WhoisEvent;

/*
 * TODO - cleanup this entire class - it's a total mess
 */
public class IRCEventToStringConverter {

    private static IRCEventToStringConverter sConverter;

    private final EventConverter mEventConverter = new EventConverter();

    private final Map<Class, Method> mClassMethodMap = new HashMap<>();

    private final Context mContext;

    private IRCEventToStringConverter(final Context context) {
        mContext = context.getApplicationContext();

        buildReflectionCache();
    }

    public static IRCEventToStringConverter getConverter(final Context context) {
        if (sConverter == null) {
            sConverter = new IRCEventToStringConverter(context);
        }
        return sConverter;
    }

    private void buildReflectionCache() {
        for (final Method method : mEventConverter.getClass().getDeclaredMethods()) {
            mClassMethodMap.put(method.getParameterTypes()[0], method);
        }
    }

    public CharSequence formatTextWithStyle(final String format, final FormattedString... args) {
        final Formatter f = new Formatter();
        return f.format(format, args).getFormattedString();
    }

    public EventDecorator getEventDecorator(final Event event) {
        try {
            final Object result = mClassMethodMap.get(event.getClass()).invoke(mEventConverter,
                    event);
            return (EventDecorator) result;
        } catch (Exception e) {
            CrashUtils.logIssue("Dead event of type " + event.getClass() + " received");
            return new EventDecorator("");
        }
    }

    private CharSequence appendReasonIfNeeded(final CharSequence response, final String reason) {
        return TextUtils.isEmpty(reason)
                ? response
                : new SpannableStringBuilder(response).append(" ").append(
                        mContext.getString(R.string.parser_reason, reason));
    }

    private EventDecorator setupEvent(final CharSequence message) {
        return new EventDecorator(message);
    }

    private EventDecorator setupEvent(final CharSequence message, final boolean boldText) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(message);
        if (boldText) {
            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return setupEvent(builder);
    }

    private EventDecorator setupEvent(final CharSequence message, final Nick defaultNick) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(message);
        final NickColour colour = NickCache.getNickCache().get(defaultNick);
        builder.setSpan(new ForegroundColorSpan(colour.getColour()), 0, message.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return setupEvent(builder);
    }

    private EventDecorator setupEvent(final CharSequence message, final Nick defaultNick,
            final boolean boldText) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(message);
        final NickColour colour = NickCache.getNickCache().get(defaultNick);
        builder.setSpan(new ForegroundColorSpan(colour.getColour()), 0, message.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return setupEvent(builder, boldText);
    }

    private boolean shouldHighlightLine() {
        return AppPreferences.getAppPreferences().shouldHighlightLine();
    }

    private ForegroundColorSpan getColourForUser(final Nick nick) {
        return new ForegroundColorSpan(NickCache.getNickCache().get(nick).getColour());
    }

    private FormattedString getFormattedStringForUser(final ChannelUser user) {
        return getFormattedStringForNick(user.getNick());
    }

    private FormattedString getFormattedStringForNick(final Nick nick) {
        return new FormattedString(nick.getNickAsString(), getColourForUser(nick));
    }

    // Simple class for storing DCC nicks
    private static class DCCNick implements Nick {

        private final String mNick;

        private DCCNick(final String nick) {
            mNick = nick;
        }

        @Override
        public String getNickAsString() {
            return mNick;
        }

        @Override
        public String toString() {
            return mNick;
        }

        @Override
        public int hashCode() {
            return mNick.hashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof DCCNick)) {
                return false;
            }

            final DCCNick other = (DCCNick) o;
            return mNick.equals(other.getNickAsString());
        }
    }

    @SuppressWarnings("unused")
    private class EventConverter {

        // Messages
        public EventDecorator getInitialTopicMessage(final ChannelInitialTopicEvent event) {
            final String response = mContext.getString(R.string.parser_new_topic,
                    event.topic, event.setterNick);
            return setupEvent(response);
        }

        public EventDecorator getOnWhoisMessage(final WhoisEvent event) {
            return setupEvent(event.whoisMessage);
        }

        public EventDecorator getOnConnectingMessage(final ConnectingEvent event) {
            final String response = mContext.getString(R.string.parser_connecting);
            return setupEvent(response);
        }

        public EventDecorator getOnConnectedMessage(final ConnectEvent event) {
            final String response = mContext.getString(R.string.parser_connected, event.serverUrl);
            return setupEvent(response);
        }

        public EventDecorator getOnConnectedMessage(final ChannelConnectEvent event) {
            final String response = mContext.getString(R.string.parser_connected_generic);
            return setupEvent(response);
        }

        public EventDecorator getOnConnectedMessage(final QueryConnectEvent event) {
            final String response = mContext.getString(R.string.parser_connected_generic);
            return setupEvent(response);
        }

        public EventDecorator getJoinMessage(final ChannelWorldJoinEvent event) {
            final String response = mContext.getString(R.string.parser_joined_channel);
            if (shouldHighlightLine()) {
                return setupEvent(String.format(response, event.userNick), event.userNick);
            } else {
                return setupEvent(formatTextWithStyle(response,
                        getFormattedStringForUser(event.user)));
            }
        }

        public EventDecorator getModeChangedMessage(final ChannelUserLevelChangeEvent event) {
            final String response = mContext.getString(R.string.parser_mode_changed);
            if (shouldHighlightLine()) {
                final String nick = event.changingNick;
                final String formattedResponse = String.format(response, event.rawMode,
                        event.user.getNick(), nick);
                return setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString formattedChangingNick = event.changingUser
                        .transform(IRCEventToStringConverter.this::getFormattedStringForUser)
                        .or(new FormattedString(event.changingNick));
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.rawMode),
                        getFormattedStringForUser(event.user),
                        formattedChangingNick
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getModeChangedMessage(final ChannelWorldLevelChangeEvent event) {
            final String response = mContext.getString(R.string.parser_mode_changed);
            if (shouldHighlightLine()) {
                final String nick = event.changingNick;
                final String formattedResponse = String.format(response, event.rawMode,
                        event.user.getNick(), nick);
                return setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString formattedChangingNick = event.changingUser
                        .transform(IRCEventToStringConverter.this::getFormattedStringForUser)
                        .or(new FormattedString(event.changingNick));
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.rawMode),
                        getFormattedStringForUser(event.user),
                        formattedChangingNick
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getModeMessage(final ChannelModeEvent event) {
            final String response = mContext.getString(R.string.parser_mode_changed);
            if (shouldHighlightLine()) {
                final String nick = event.sendingNick;
                final String formattedResponse = String
                        .format(response, event.mode, event.recipient, nick);
                return setupEvent(formattedResponse);
            } else {
                final FormattedString formattedChangingNick = event.sendingUser
                        .transform(IRCEventToStringConverter.this::getFormattedStringForUser)
                        .or(new FormattedString(event.sendingNick));
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.mode),
                        new FormattedString(event.recipient),
                        formattedChangingNick
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }


        public EventDecorator getNickChangedMessage(final ChannelWorldNickChangeEvent event) {
            final String response = mContext.getString(R.string.parser_other_user_nick_change);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.oldNick,
                        event.userNick);
                return setupEvent(formattedResponse, event.userNick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.oldNick),
                        getFormattedStringForNick(event.userNick)
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getNickChaneMessage(final ChannelNickChangeEvent event) {
            final String response = mContext.getString(R.string.parser_appuser_nick_changed);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.oldNick,
                        event.newNick);
                return setupEvent(formattedResponse, event.newNick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.oldNick),
                        getFormattedStringForNick(event.newNick)
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getTopicChangedMessage(final ChannelTopicEvent event) {
            final String response = mContext.getString(R.string.parser_topic_changed);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.topic,
                        event.topicSetter.getNick());
                return setupEvent(formattedResponse, event.topicSetter.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.topic),
                        getFormattedStringForUser(event.topicSetter),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getUserKickedMessage(final ChannelWorldKickEvent event) {
            final String response = mContext.getString(R.string.parser_kicked_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String
                        .format(response, event.userNick, event.kickingNick == null ? event
                                .userNickString : event.kickingNick);
                return setupEvent(appendReasonIfNeeded(formattedResponse, event.reason),
                        event.userNick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.userNick),
                        event.kickingNick == null
                                ? new FormattedString(event.kickingNickString)
                                : getFormattedStringForNick(event.kickingNick)
                };
                return setupEvent(
                        appendReasonIfNeeded(formatTextWithStyle(response, formattedStrings),
                                event.reason)
                );
            }
        }

        public EventDecorator getServerKickEvent(final KickEvent event) {
            final String response = mContext.getString(R.string.parser_user_kicked_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.channel.getName(),
                        event.kickingNick);
                return setupEvent(appendReasonIfNeeded(formattedResponse, event.reason),
                        event.kickingNick);
            } else {
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.channel.getName()),
                        getFormattedStringForNick(event.kickingNick),
                };
                return setupEvent(
                        appendReasonIfNeeded(formatTextWithStyle(response, formattedStrings),
                                event.reason)
                );
            }
        }

        public EventDecorator getPartMessage(final ChannelWorldPartEvent event) {
            final String response = mContext.getString(R.string.parser_user_parted_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick);
                return setupEvent(appendReasonIfNeeded(formattedResponse, event.reason),
                        event.userNick);
            } else {
                return setupEvent(appendReasonIfNeeded(formatTextWithStyle(response,
                        getFormattedStringForNick(event.userNick)), event.reason));
            }
        }

        public EventDecorator getQuitMessage(final ChannelWorldQuitEvent event) {
            final String response = mContext.getString(R.string.parser_quit_server);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick);
                return setupEvent(appendReasonIfNeeded(formattedResponse, event.reason),
                        event.userNick);
            } else {
                return setupEvent(appendReasonIfNeeded(formatTextWithStyle(response,
                        getFormattedStringForNick(event.userNick)), event.reason));
            }
        }

        public EventDecorator getMessage(final ChannelWorldMessageEvent event) {
            final String response = mContext.getString(R.string.parser_message);

            // Get out clause for message events from ZNCs for example
            if (event.userNick == null) {
                final String formattedResponse = String.format(response, event.userNickString,
                        event.message);
                return setupEvent(formattedResponse, event.userMentioned);
            }

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick,
                        event.message);
                return setupEvent(formattedResponse, event.userNick, event.userMentioned);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.userNick),
                        new FormattedString(event.message)
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings),
                        event.userMentioned);
            }
        }

        public EventDecorator getMessage(final ChannelMessageEvent event) {
            final String response = mContext.getString(R.string.parser_message);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.user.getNick(),
                        event.message);
                return setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.message)
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getMessage(final QueryMessageSelfEvent event) {
            final String response = mContext.getString(R.string.parser_message);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.ourUser.getNick(),
                        event.message);
                return setupEvent(formattedResponse, event.ourUser.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.ourUser.getNick()),
                        new FormattedString(event.message)
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getMessage(final QueryMessageWorldEvent event) {
            final String response = mContext.getString(R.string.parser_message);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.user.getNick(),
                        event.message);
                return setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.message),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getNoticeMessage(final ChannelNoticeEvent event) {
            final String response = mContext.getString(R.string.parser_message);
            final String formattedResponse = String.format(response, event.originNick,
                    event.notice);
            return setupEvent(formattedResponse, true);
        }

        public EventDecorator getActionMessage(final ChannelActionEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.user.getNick(),
                        event.action);
                return setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.action),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getActionMessage(final ChannelWorldActionEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick,
                        event.action);
                return setupEvent(formattedResponse, event.userNick, true);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.action),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings), true);
            }
        }

        public EventDecorator getActionMessage(final QueryActionSelfEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.ourUser.getNick(),
                        event.action);
                return setupEvent(formattedResponse, event.ourUser.getNick(), true);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.ourUser.getNick()),
                        new FormattedString(event.action),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings), true);
            }
        }

        public EventDecorator getActionMessage(final QueryActionWorldEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.user.getNick(),
                        event.action);
                return setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.action),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getGenericServerMessage(final GenericServerEvent event) {
            return setupEvent(event.message);
        }

        public EventDecorator getMotdLine(final MotdEvent event) {
            return setupEvent(event.motdLine);
        }

        public EventDecorator getErrorMessage(final ErrorEvent errorEvent) {
            return setupEvent(errorEvent.line);
        }

        public EventDecorator getServerChangeMessage(final ServerNickChangeEvent event) {
            final String response = mContext.getString(R.string.parser_appuser_nick_changed,
                    event.oldNick, event.newNick);
            return setupEvent(response);
        }

        public EventDecorator getPrivateNoticeMessage(final NoticeEvent event) {
            final String response = mContext.getString(R.string.parser_message,
                    event.sendingNick, event.message);
            return setupEvent(response, true);
        }

        public EventDecorator getDisconnectEvent(final DisconnectEvent event) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            return setupEvent(String.format("%s (%s)", response, event.serverMessage));
        }

        public EventDecorator getDisconnectEvent(final ChannelDisconnectEvent event) {
            return setupEvent(event.message);
        }

        public EventDecorator getDisconnectEvent(final QueryDisconnectEvent event) {
            return setupEvent(event.message);
        }

        public EventDecorator getReconnectEvent(final ReconnectEvent event) {
            final String response = mContext.getString(R.string.parser_reconnect);
            return setupEvent(response);
        }

        public EventDecorator getNewPrivateMessageEvent(final QueryOpenedEvent event) {
            final String response = mContext.getString(R.string.parser_pm_opened);
            return setupEvent(response);
        }

        public EventDecorator getWallopsEvent(final WallopsEvent event) {
            final String response = mContext.getString(R.string.parser_message,
                    event.nick, event.message);
            return setupEvent(response);
        }

        public EventDecorator getStopEvent(final StopEvent event) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            return setupEvent(response);
        }

        public EventDecorator getStopEvent(final ChannelStopEvent event) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            return setupEvent(response);
        }

        public EventDecorator getStopEvent(final QueryStopEvent event) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            return setupEvent(response);
        }

        public EventDecorator getChannelPartEvent(final PartEvent event) {
            final String response = mContext.getString(R.string.parser_parted_channel);
            return setupEvent(response);
        }

        public EventDecorator getInviteEvent(final InviteEvent event) {
            final String response = mContext.getString(R.string.parser_invited);
            final String formattedResponse = String.format(response, event.channelName,
                    event.invitingUser);
            return setupEvent(formattedResponse, true);
        }

        // DCC chat events start
        public EventDecorator getDCCChatRequestedEvent(final DCCChatRequestEvent event) {
            final DCCPendingChatConnection connection = event.getPendingConnection();
            final String response = mContext.getString(R.string.parser_dcc_chat_requested);
            final String formattedResponse = String.format(response, connection.getDccRequestNick(),
                    connection.getIP(), connection.getPort());
            return setupEvent(formattedResponse, true);
        }

        public EventDecorator getDCCChatStartedEvent(final DCCChatStartedEvent event) {
            final String response = mContext.getString(R.string.parser_dcc_chat_opened);
            return setupEvent(response);
        }

        public EventDecorator getDCCChatEvent(final DCCChatSelfMessageEvent event) {
            final String response = mContext.getString(R.string.parser_message);
            final Nick nick = event.chatConversation.getServer().getUser().getNick();
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response,
                        nick, event.message);
                return setupEvent(formattedResponse, nick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(nick),
                        new FormattedString(event.message),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getActionMessage(final DCCChatSelfActionEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            final Nick nick = event.chatConversation.getServer().getUser().getNick();
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response,
                        nick, event.action);
                return setupEvent(formattedResponse, nick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(nick),
                        new FormattedString(event.action),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getDCCChatEvent(final DCCChatWorldMessageEvent event) {
            final String response = mContext.getString(R.string.parser_message);
            final Nick nick = new DCCNick(event.chatConversation.getId());
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response,
                        nick, event.message);
                return setupEvent(formattedResponse, nick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(nick),
                        new FormattedString(event.message),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator getActionMessage(final DCCChatWorldActionEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            final Nick nick = new DCCNick(event.chatConversation.getId());
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response,
                        nick, event.action);
                return setupEvent(formattedResponse, nick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(nick),
                        new FormattedString(event.action),
                };
                return setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        public EventDecorator get(final DCCFileGetStartedEvent event) {
            final int count = event.fileConversation.getFileConnections().size();
            return setupEvent(mContext.getString(R.string.parser_dcc_files_count, count));
        }
        // DCC chat events end
    }
}

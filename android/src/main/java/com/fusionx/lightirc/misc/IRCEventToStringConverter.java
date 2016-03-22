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
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.fusionx.relay.base.ChannelUser;
import co.fusionx.relay.base.FormatSpanInfo;
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
import co.fusionx.relay.event.server.RegisteringEvent;
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
    private final int[] mDarkColors;
    private final int[] mLightColors;

    private final Context mContext;

    private IRCEventToStringConverter(final Context context) {
        mContext = context.getApplicationContext();

        buildReflectionCache();
        mDarkColors = mContext.getResources().getIntArray(R.array.irc_colors_dark);
        mLightColors = mContext.getResources().getIntArray(R.array.irc_colors_light);
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

    public CharSequence formatTextWithStyleAndNickHighlight(final Nick nick, final String format,
            final FormattedString... args) {
        final Formatter f = new Formatter();
        if (nick != null && shouldHighlightLine()) {
            f.addGlobalSpan(getColourForUser(nick));
        }
        return f.format(format, args).getFormattedString();
    }

    public EventDecorator getEventDecorator(final Event event, final boolean forDarkBackground) {
        try {
            final Object result = mClassMethodMap.get(event.getClass()).invoke(mEventConverter,
                    event, forDarkBackground ? mDarkColors : mLightColors);
            return (EventDecorator) result;
        } catch (Exception e) {
            CrashUtils.logIssue("Dead event of type " + event.getClass() + " received");
            return new EventDecorator("");
        }
    }

    private CharSequence appendReasonIfNeeded(final CharSequence response, final String reason,
            final List<FormatSpanInfo> formats, final int[] colorPalette) {
        if (TextUtils.isEmpty(reason)) {
            return response;
        }

        FormattedString formattedReason = formatMessage(reason, formats, colorPalette);
        SpannableStringBuilder builder = new SpannableStringBuilder(response).append(" ");
        builder.append(formatTextWithStyle(mContext.getString(R.string.parser_reason),
                formattedReason));
        return builder;
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

    private int formatColorToColor(FormatSpanInfo.Color color, int[] palette) {
        if (color != null) {
            switch (color) {
                case WHITE: return palette[0];
                case BLACK: return palette[1];
                case BLUE: return palette[2];
                case GREEN: return palette[3];
                case RED: return palette[4];
                case BROWN: return palette[5];
                case PURPLE: return palette[6];
                case ORANGE: return palette[7];
                case YELLOW: return palette[8];
                case LIGHT_GREEN: return palette[9];
                case TEAL: return palette[10];
                case LIGHT_CYAN: return palette[11];
                case LIGHT_BLUE: return palette[12];
                case PINK: return palette[13];
                case GREY: return palette[14];
                case LIGHT_GREY: return palette[15];
            }
        }
        return 0;
    }

    private FormattedString formatNick(Nick nick, String nickString) {
        if (nick == null) {
            return new FormattedString(nickString);
        }
        return getFormattedStringForNick(nick);
    }

    private FormattedString formatMessage(String message,
            List<FormatSpanInfo> formats, int[] colorPalette) {
        FormattedString formattedMessage = new FormattedString(message);

        int formatCount = formats == null ? 0 : formats.size();
        for (int i = 0; i < formatCount; i++) {
            FormatSpanInfo span = formats.get(i);
            final int start = span.start, end = span.end;
            switch (span.format) {
                case BOLD:
                    formattedMessage.addSpan(new StyleSpan(Typeface.BOLD), start, end);
                    break;
                case ITALIC:
                    formattedMessage.addSpan(new StyleSpan(Typeface.ITALIC), start, end);
                    break;
                case UNDERLINED:
                    formattedMessage.addSpan(new UnderlineSpan(), start, end);
                    break;
                case COLOR: {
                    int fg = formatColorToColor(span.fgColor, colorPalette);
                    if (fg != 0) {
                        formattedMessage.addSpan(new ForegroundColorSpan(fg), start, end);
                    }
                    int bg = formatColorToColor(span.bgColor, colorPalette);
                    if (bg != 0) {
                        formattedMessage.addSpan(new BackgroundColorSpan(bg), start, end);
                    }
                    break;
                }
            }
        }

        return formattedMessage;
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
        public EventDecorator getInitialTopicMessage(final ChannelInitialTopicEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_new_topic,
                    event.topic, event.setterNick);
            return setupEvent(response);
        }

        public EventDecorator getOnWhoisMessage(final WhoisEvent event, int[] colorPalette) {
            return setupEvent(event.whoisMessage);
        }

        public EventDecorator getOnConnectingMessage(final ConnectingEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_connecting);
            return setupEvent(response);
        }

        public EventDecorator getOnRegisteringMessage(final RegisteringEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_registering);
            return setupEvent(response);
        }

        public EventDecorator getOnConnectedMessage(final ConnectEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_connected, event.serverUrl);
            return setupEvent(response);
        }

        public EventDecorator getOnConnectedMessage(final ChannelConnectEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_connected_generic);
            return setupEvent(response);
        }

        public EventDecorator getOnConnectedMessage(final QueryConnectEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_connected_generic);
            return setupEvent(response);
        }

        public EventDecorator getJoinMessage(final ChannelWorldJoinEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_joined_channel);
            if (shouldHighlightLine()) {
                return setupEvent(String.format(response, event.userNick), event.userNick);
            } else {
                return setupEvent(formatTextWithStyle(response,
                        getFormattedStringForUser(event.user)));
            }
        }

        public EventDecorator getModeChangedMessage(final ChannelUserLevelChangeEvent event,
                int[] colorPalette) {
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

        public EventDecorator getModeChangedMessage(final ChannelWorldLevelChangeEvent event,
                int[] colorPalette) {
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

        public EventDecorator getModeMessage(final ChannelModeEvent event, int[] colorPalette) {
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


        public EventDecorator getNickChangedMessage(final ChannelWorldNickChangeEvent event,
                int[] colorPalette) {
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

        public EventDecorator getNickChaneMessage(final ChannelNickChangeEvent event,
                int[] colorPalette) {
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

        public EventDecorator getTopicChangedMessage(final ChannelTopicEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_topic_changed);
            final FormattedString nick = formatNick(event.topicSetter.getNick(), null);
            final FormattedString topic = formatMessage(event.topic,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.topicSetter.getNick(),
                    response, topic, nick));
        }

        public EventDecorator getUserKickedMessage(final ChannelWorldKickEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_kicked_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String
                        .format(response, event.userNick, event.kickingNick == null ? event
                                .userNickString : event.kickingNick);
                return setupEvent(
                        appendReasonIfNeeded(formattedResponse, event.reason, null, colorPalette),
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
                                event.reason, null, colorPalette)
                );
            }
        }

        public EventDecorator getServerKickEvent(final KickEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_user_kicked_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.channel.getName(),
                        event.kickingNick);
                return setupEvent(
                        appendReasonIfNeeded(formattedResponse, event.reason, null, colorPalette),
                        event.kickingNick);
            } else {
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.channel.getName()),
                        getFormattedStringForNick(event.kickingNick),
                };
                return setupEvent(
                        appendReasonIfNeeded(formatTextWithStyle(response, formattedStrings),
                                event.reason, null, colorPalette)
                );
            }
        }

        public EventDecorator getPartMessage(final ChannelWorldPartEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_user_parted_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick);
                return setupEvent(
                        appendReasonIfNeeded(formattedResponse, event.reason,
                                event.formats, colorPalette),
                        event.userNick);
            } else {
                return setupEvent(appendReasonIfNeeded(formatTextWithStyle(response,
                        getFormattedStringForNick(event.userNick)),
                        event.reason, event.formats, colorPalette));
            }
        }

        public EventDecorator getQuitMessage(final ChannelWorldQuitEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_quit_server);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick);
                return setupEvent(
                        appendReasonIfNeeded(formattedResponse, event.reason,
                                event.formats, colorPalette),
                        event.userNick);
            } else {
                return setupEvent(appendReasonIfNeeded(formatTextWithStyle(response,
                        getFormattedStringForNick(event.userNick)),
                        event.reason, event.formats, colorPalette));
            }
        }

        public EventDecorator getMessage(final ChannelWorldMessageEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message);
            final FormattedString formattedNickString =
                    formatNick(event.userNick, event.userNickString);
            final FormattedString formattedMessage = formatMessage(event.message,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.userNick,
                    response, formattedNickString, formattedMessage), event.userMentioned);
        }

        public EventDecorator getMessage(final ChannelMessageEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message);
            final FormattedString nick = formatNick(event.user.getNick(), null);
            final FormattedString message = formatMessage(event.message,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.user.getNick(),
                    response, nick, message));
        }

        public EventDecorator getMessage(final QueryMessageSelfEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message);
            final FormattedString nick = formatNick(event.ourUser.getNick(), null);
            final FormattedString message = formatMessage(event.message,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.ourUser.getNick(),
                    response, nick, message));
        }

        public EventDecorator getMessage(final QueryMessageWorldEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message);
            final FormattedString nick = formatNick(event.user.getNick(), null);
            final FormattedString message = formatMessage(event.message,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.user.getNick(),
                    response, nick, message));
        }

        public EventDecorator getNoticeMessage(final ChannelNoticeEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message);
            final FormattedString nick = new FormattedString(event.originNick);
            final FormattedString notice = formatMessage(event.notice, event.formats, colorPalette);

            return setupEvent(formatTextWithStyle(response, nick, notice), true);
        }

        public EventDecorator getActionMessage(final ChannelActionEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_action);
            final FormattedString nick = formatNick(event.user.getNick(), null);
            final FormattedString action = formatMessage(event.action, event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.user.getNick(),
                    response, nick, action));
        }

        public EventDecorator getActionMessage(final ChannelWorldActionEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_action);
            final FormattedString nick = formatNick(event.userNick, event.userNickString);
            final FormattedString action = formatMessage(event.action, event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.userNick,
                    response, nick, action), true);
        }

        public EventDecorator getActionMessage(final QueryActionSelfEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_action);
            final FormattedString nick = formatNick(event.ourUser.getNick(), null);
            final FormattedString action = formatMessage(event.action, event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.ourUser.getNick(),
                    response, nick, action), true);
        }

        public EventDecorator getActionMessage(final QueryActionWorldEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_action);
            final FormattedString nick = formatNick(event.user.getNick(), null);
            final FormattedString action = formatMessage(event.action, event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(event.user.getNick(),
                    response, nick, action));
        }

        public EventDecorator getGenericServerMessage(final GenericServerEvent event,
                int[] colorPalette) {
            return setupEvent(event.message);
        }

        public EventDecorator getMotdLine(final MotdEvent event, int[] colorPalette) {
            return setupEvent(event.motdLine);
        }

        public EventDecorator getErrorMessage(final ErrorEvent errorEvent, int[] colorPalette) {
            return setupEvent(errorEvent.line);
        }

        public EventDecorator getServerChangeMessage(final ServerNickChangeEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_appuser_nick_changed,
                    event.oldNick, event.newNick);
            return setupEvent(response);
        }

        public EventDecorator getPrivateNoticeMessage(final NoticeEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message,
                    event.sendingNick, event.message);
            return setupEvent(response, true);
        }

        public EventDecorator getDisconnectEvent(final DisconnectEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            return setupEvent(String.format("%s (%s)", response, event.serverMessage));
        }

        public EventDecorator getDisconnectEvent(final ChannelDisconnectEvent event,
                int[] colorPalette) {
            return setupEvent(event.message);
        }

        public EventDecorator getDisconnectEvent(final QueryDisconnectEvent event,
                int[] colorPalette) {
            return setupEvent(event.message);
        }

        public EventDecorator getReconnectEvent(final ReconnectEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_reconnect);
            return setupEvent(response);
        }

        public EventDecorator getNewPrivateMessageEvent(final QueryOpenedEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_pm_opened);
            return setupEvent(response);
        }

        public EventDecorator getWallopsEvent(final WallopsEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message);
            final FormattedString nick = new FormattedString(event.nick);
            final FormattedString message = formatMessage(event.message,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyle(response, nick, message));
        }

        public EventDecorator getStopEvent(final StopEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            return setupEvent(response);
        }

        public EventDecorator getStopEvent(final ChannelStopEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            return setupEvent(response);
        }

        public EventDecorator getStopEvent(final QueryStopEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            return setupEvent(response);
        }

        public EventDecorator getChannelPartEvent(final PartEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_parted_channel);
            return setupEvent(response);
        }

        public EventDecorator getInviteEvent(final InviteEvent event, int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_invited);
            final String formattedResponse = String.format(response, event.channelName,
                    event.invitingUser);
            return setupEvent(formattedResponse, true);
        }

        // DCC chat events start
        public EventDecorator getDCCChatRequestedEvent(final DCCChatRequestEvent event,
                int[] colorPalette) {
            final DCCPendingChatConnection connection = event.getPendingConnection();
            final String response = mContext.getString(R.string.parser_dcc_chat_requested);
            final String formattedResponse = String.format(response, connection.getDccRequestNick(),
                    connection.getIP(), connection.getPort());
            return setupEvent(formattedResponse, true);
        }

        public EventDecorator getDCCChatStartedEvent(final DCCChatStartedEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_dcc_chat_opened);
            return setupEvent(response);
        }

        public EventDecorator getDCCChatEvent(final DCCChatSelfMessageEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message);
            final Nick nick = event.chatConversation.getServer().getUser().getNick();
            final FormattedString formattedNick = formatNick(nick, null);
            final FormattedString formattedMessage = formatMessage(event.message,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(nick,
                    response, formattedNick, formattedMessage));
        }

        public EventDecorator getActionMessage(final DCCChatSelfActionEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_action);
            final Nick nick = event.chatConversation.getServer().getUser().getNick();
            final FormattedString formattedNick = formatNick(nick, null);
            final FormattedString formattedAction = formatMessage(event.action,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(nick,
                    response, formattedNick, formattedAction));
        }

        public EventDecorator getDCCChatEvent(final DCCChatWorldMessageEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_message);
            final Nick nick = new DCCNick(event.chatConversation.getId());
            final FormattedString formattedNick = formatNick(nick, null);
            final FormattedString formattedMessage = formatMessage(event.message,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(nick,
                    response, formattedNick, formattedMessage));
        }

        public EventDecorator getActionMessage(final DCCChatWorldActionEvent event,
                int[] colorPalette) {
            final String response = mContext.getString(R.string.parser_action);
            final Nick nick = new DCCNick(event.chatConversation.getId());
            final FormattedString formattedNick = formatNick(nick, null);
            final FormattedString formattedMessage = formatMessage(event.action,
                    event.formats, colorPalette);

            return setupEvent(formatTextWithStyleAndNickHighlight(nick,
                    response, formattedNick, formattedMessage));
        }

        public EventDecorator get(final DCCFileGetStartedEvent event, int[] colorPalette) {
            final int count = event.fileConversation.getFileConnections().size();
            return setupEvent(mContext.getString(R.string.parser_dcc_files_count, count));
        }
        // DCC chat events end
    }
}

package com.fusionx.lightirc.util;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.FormattedString;
import com.fusionx.lightirc.misc.Formatter;
import com.fusionx.lightirc.misc.NickCache;
import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.model.NickColour;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.event.Event;
import com.fusionx.relay.event.channel.ActionEvent;
import com.fusionx.relay.event.channel.ChannelConnectEvent;
import com.fusionx.relay.event.channel.ChannelDisconnectEvent;
import com.fusionx.relay.event.channel.ChannelNoticeEvent;
import com.fusionx.relay.event.channel.InitialTopicEvent;
import com.fusionx.relay.event.channel.MessageEvent;
import com.fusionx.relay.event.channel.ModeEvent;
import com.fusionx.relay.event.channel.NickChangeEvent;
import com.fusionx.relay.event.channel.TopicEvent;
import com.fusionx.relay.event.channel.UserLevelChangeEvent;
import com.fusionx.relay.event.channel.WorldActionEvent;
import com.fusionx.relay.event.channel.WorldJoinEvent;
import com.fusionx.relay.event.channel.WorldKickEvent;
import com.fusionx.relay.event.channel.WorldLevelChangeEvent;
import com.fusionx.relay.event.channel.WorldMessageEvent;
import com.fusionx.relay.event.channel.WorldNickChangeEvent;
import com.fusionx.relay.event.channel.WorldPartEvent;
import com.fusionx.relay.event.channel.WorldQuitEvent;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.ConnectingEvent;
import com.fusionx.relay.event.server.DisconnectEvent;
import com.fusionx.relay.event.server.ErrorEvent;
import com.fusionx.relay.event.server.GenericServerEvent;
import com.fusionx.relay.event.server.KickEvent;
import com.fusionx.relay.event.server.MotdEvent;
import com.fusionx.relay.event.server.PrivateNoticeEvent;
import com.fusionx.relay.event.server.ReconnectEvent;
import com.fusionx.relay.event.server.ServerNickChangeEvent;
import com.fusionx.relay.event.server.StopEvent;
import com.fusionx.relay.event.server.WallopsEvent;
import com.fusionx.relay.event.server.WhoisEvent;
import com.fusionx.relay.event.user.PrivateActionEvent;
import com.fusionx.relay.event.user.PrivateMessageEvent;
import com.fusionx.relay.event.user.PrivateMessageOpenedEvent;
import com.fusionx.relay.event.user.UserConnectEvent;
import com.fusionx.relay.event.user.UserDisconnectEvent;
import com.fusionx.relay.event.user.WorldPrivateActionEvent;
import com.fusionx.relay.event.user.WorldPrivateMessageEvent;
import com.fusionx.relay.nick.Nick;
import com.squareup.otto.Bus;
import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;

/*
 * TODO - cleanup this entire class - it's a total mess
 */
public class MessageConversionUtils {

    private static MessageConversionUtils sConverter;

    private final Context mContext;

    private final Bus mConverter;

    private final Object mParser = new Object() {

        @Subscribe
        public void debugDeadEvent(final DeadEvent deadEvent) {
            Log.d("HoloIRC", "Dead event of type " + deadEvent.event.getClass() + " received");
        }

        // Messages
        @Subscribe
        public void getInitialTopicMessage(final InitialTopicEvent event) {
            final String response = mContext.getString(R.string.parser_new_topic);
            setupEvent(String.format(response, event.topic, event.setterNick));
        }

        @Subscribe
        public void getOnWhoisMessage(final WhoisEvent event) {
            setupEvent(event.whoisMessage);
        }

        @Subscribe
        public void getOnConnectingMessage(final ConnectingEvent event) {
            final String response = mContext.getString(R.string.parser_connecting);
            setupEvent(response);
        }

        @Subscribe
        public void getOnConnectedMessage(final ConnectEvent event) {
            final String response = mContext.getString(R.string.parser_connected);
            setupEvent(String.format(response, event.serverUrl));
        }

        @Subscribe
        public void getOnConnectedMessage(final ChannelConnectEvent event) {
            final String response = mContext.getString(R.string.parser_connected_generic);
            setupEvent(response);
        }

        @Subscribe
        public void getOnConnectedMessage(final UserConnectEvent event) {
            final String response = mContext.getString(R.string.parser_connected_generic);
            setupEvent(response);
        }

        @Subscribe
        public void getJoinMessage(final WorldJoinEvent event) {
            final String response = mContext.getString(R.string.parser_joined_channel);
            if (shouldHighlightLine()) {
                setupEvent(String.format(response, event.userNick), event.userNick);
            } else {
                setupEvent(formatTextWithStyle(response, getFormattedStringForUser(event.user)));
            }
        }

        @Subscribe
        public void getModeChangedMessage(final UserLevelChangeEvent event) {
            final String response = mContext.getString(R.string.parser_mode_changed);
            if (shouldHighlightLine()) {
                final String nick = event.changingUser == null ? event.changingNick : event
                        .changingUser.getNick().getNickAsString();
                final String formattedResponse = String.format(response, event.rawMode,
                        event.user.getNick(), nick);
                setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString formattedChangingNick = event.changingUser == null
                        ? new FormattedString(event.changingNick)
                        : getFormattedStringForUser(event.changingUser);
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.rawMode),
                        getFormattedStringForUser(event.user),
                        formattedChangingNick
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getModeChangedMessage(final WorldLevelChangeEvent event) {
            final String response = mContext.getString(R.string.parser_mode_changed);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.rawMode,
                        event.user.getNick(), event.changingUser.getNick());
                setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.rawMode),
                        getFormattedStringForUser(event.user),
                        getFormattedStringForUser(event.changingUser)
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getNickChangedMessage(final WorldNickChangeEvent event) {
            final String response = mContext.getString(R.string.parser_other_user_nick_change);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.oldNick,
                        event.userNick);
                setupEvent(formattedResponse, event.userNick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.oldNick),
                        getFormattedStringForNick(event.userNick)
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getNickChaneMessage(final NickChangeEvent event) {
            final String response = mContext.getString(R.string.parser_appuser_nick_changed);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.oldNick,
                        event.newNick);
                setupEvent(formattedResponse, event.newNick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.oldNick),
                        getFormattedStringForNick(event.newNick)
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getTopicChangedMessage(final TopicEvent event) {
            final String response = mContext.getString(R.string.parser_topic_changed);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.topic,
                        event.topicSetter.getNick());
                setupEvent(formattedResponse, event.topicSetter.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.topic),
                        getFormattedStringForUser(event.topicSetter),
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getUserKickedMessage(final WorldKickEvent event) {
            final String response = mContext.getString(R.string.parser_kicked_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String
                        .format(response, event.userNick, event.kickingNick == null ? event
                                .userNickString : event.kickingNick);
                setupEvent(appendReasonIfNeeded(formattedResponse, event.reason), event.userNick);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.userNick),
                        event.kickingNick == null
                                ? new FormattedString(event.kickingNickString)
                                : getFormattedStringForNick(event.kickingNick)
                };
                setupEvent(appendReasonIfNeeded(formatTextWithStyle(response, formattedStrings),
                        event.reason));
            }
        }

        @Subscribe
        public void getServerKickEvent(final KickEvent event) {
            final String response = mContext.getString(R.string.parser_user_kicked_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.channelName,
                        event.kickingNick);
                setupEvent(appendReasonIfNeeded(formattedResponse, event.reason),
                        event.kickingNick);
            } else {
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.channelName),
                        getFormattedStringForNick(event.kickingNick),
                };
                setupEvent(appendReasonIfNeeded(formatTextWithStyle(response, formattedStrings),
                        event.reason));
            }
        }

        @Subscribe
        public void getPartMessage(final WorldPartEvent event) {
            final String response = mContext.getString(R.string.parser_parted_channel);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick);
                setupEvent(appendReasonIfNeeded(formattedResponse, event.reason), event.userNick);
            } else {
                setupEvent(appendReasonIfNeeded(formatTextWithStyle(response,
                        getFormattedStringForNick(event.userNick)), event.reason));
            }
        }

        @Subscribe
        public void getQuitMessage(final WorldQuitEvent event) {
            final String response = mContext.getString(R.string.parser_quit_server);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick);
                setupEvent(appendReasonIfNeeded(formattedResponse, event.reason), event.userNick);
            } else {
                setupEvent(appendReasonIfNeeded(formatTextWithStyle(response,
                        getFormattedStringForNick(event.userNick)), event.reason));
            }
        }

        @Subscribe
        public void getMessage(final WorldMessageEvent event) {
            final String response = mContext.getString(R.string.parser_message);

            // Get out clause for message events from ZNCs for example
            if (event.userNick == null) {
                final String formattedResponse = String.format(response, event.userNickString,
                        event.message);
                setupEvent(formattedResponse, event.userMentioned);
                return;
            }

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick,
                        event.message);
                setupEvent(formattedResponse, event.userNick, event.userMentioned);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.userNick),
                        new FormattedString(event.message)
                };
                setupEvent(formatTextWithStyle(response, formattedStrings), event.userMentioned);
            }
        }

        @Subscribe
        public void getMessage(final MessageEvent event) {
            final String response = mContext.getString(R.string.parser_message);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.user.getNick(),
                        event.message);
                setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.message)
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getMessage(final PrivateMessageEvent event) {
            final String response = mContext.getString(R.string.parser_message);

            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.ourUser.getNick(),
                        event.message);
                setupEvent(formattedResponse, event.ourUser.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.message)
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getMessage(final WorldPrivateMessageEvent event) {
            final String response = mContext.getString(R.string.parser_message);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.user.getNick(),
                        event.message);
                setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.message),
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getNoticeMessage(final ChannelNoticeEvent event) {
            final String response = mContext.getString(R.string.parser_message);
            final String formattedResponse = String.format(response, event.originNick,
                    event.notice);
            setupEvent(formattedResponse, true);
        }

        @Subscribe
        public void getActionMessage(final ActionEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.user.getNick(),
                        event.action);
                setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.action),
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getActionMessage(final WorldActionEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.userNick,
                        event.action);
                setupEvent(formattedResponse, event.userNick, true);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.action),
                };
                setupEvent(formatTextWithStyle(response, formattedStrings), true);
            }
        }

        @Subscribe
        public void getActionMessage(final PrivateActionEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.ourUser.getNick(),
                        event.action);
                setupEvent(formattedResponse, event.ourUser.getNick(), true);
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.ourUser.getNick()),
                        new FormattedString(event.action),
                };
                setupEvent(formatTextWithStyle(response, formattedStrings), true);
            }
        }

        @Subscribe
        public void getActionMessage(final WorldPrivateActionEvent event) {
            final String response = mContext.getString(R.string.parser_action);
            if (shouldHighlightLine()) {
                final String formattedResponse = String.format(response, event.user.getNick(),
                        event.action);
                setupEvent(formattedResponse, event.user.getNick());
            } else {
                final FormattedString[] formattedStrings = {
                        getFormattedStringForNick(event.user.getNick()),
                        new FormattedString(event.action),
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getGenericServerMessage(final GenericServerEvent event) {
            setupEvent(event.message);
        }

        @Subscribe
        public void getMotdLine(final MotdEvent event) {
            setupEvent(event.motdLine);
        }

        @Subscribe
        public void getErrorMessage(final ErrorEvent errorEvent) {
            setupEvent(errorEvent.line);
        }

        @Subscribe
        public void getServerChangeMessage(final ServerNickChangeEvent event) {
            final String response = mContext.getString(R.string.parser_appuser_nick_changed);
            setupEvent(String.format(response, event.oldNick, event.newNick));
        }

        @Subscribe
        public void getPrivateNoticeMessage(final PrivateNoticeEvent event) {
            final String response = mContext.getString(R.string.parser_message);
            setupEvent(String.format(response, event.sendingNick, event.message), true);
        }

        @Subscribe
        public void getModeMessage(final ModeEvent event) {
            final String response = mContext.getString(R.string.parser_mode_changed);
            if (shouldHighlightLine()) {
                final String formattedResponse = String
                        .format(response, event.mode, event.recipient, event.sendingUser.getNick());
                setupEvent(formattedResponse);
            } else {
                final FormattedString[] formattedStrings = {
                        new FormattedString(event.mode),
                        new FormattedString(event.recipient),
                        getFormattedStringForNick(event.sendingUser.getNick()),
                };
                setupEvent(formatTextWithStyle(response, formattedStrings));
            }
        }

        @Subscribe
        public void getDisconnectEvent(final DisconnectEvent event) {
            setupEvent(event.serverMessage);
        }

        @Subscribe
        public void getDisconnectEvent(final ChannelDisconnectEvent event) {
            setupEvent(event.message);
        }

        @Subscribe
        public void getDisconnectEvent(final UserDisconnectEvent event) {
            setupEvent(event.message);
        }

        @Subscribe
        public void getReconnectEvent(final ReconnectEvent event) {
            final String response = mContext.getString(R.string.parser_reconnect);
            setupEvent(response);
        }

        @Subscribe
        public void getNewPrivateMessageEvent(final PrivateMessageOpenedEvent event) {
            final String response = mContext.getString(R.string.parser_pm_opened);
            setupEvent(response);
        }

        @Subscribe
        public void getWallopsEvent(final WallopsEvent event) {
            final String response = mContext.getString(R.string.parser_message);
            setupEvent(String.format(response, event.nick, event.message));
        }

        @Subscribe
        public void getWallopsEvent(final StopEvent event) {
            final String response = mContext.getString(R.string.disconnected_from_server);
            setupEvent(response);
        }
    };

    public CharSequence formatTextWithStyle(final String format, final FormattedString... args) {
        final Formatter f = new Formatter();
        return f.format(format, args).getFormattedString();
    }

    private EventDecorator mMessage;

    private MessageConversionUtils(final Context context) {
        mContext = context.getApplicationContext();
        mConverter = new Bus(ThreadEnforcer.ANY);
        mConverter.register(mParser);
    }

    public static MessageConversionUtils getConverter(final Context context) {
        if (sConverter == null) {
            sConverter = new MessageConversionUtils(context);
        }
        return sConverter;
    }

    public EventDecorator getEventDecorator(final Event event) {
        mMessage = null;

        // Actually fix the values
        mConverter.post(event);

        if (mMessage == null) {
            mMessage = new EventDecorator("");
        }
        return mMessage;
    }

    private CharSequence appendReasonIfNeeded(final CharSequence response, final String reason) {
        return TextUtils.isEmpty(reason)
                ? response
                : new SpannableStringBuilder(response).append(" ").append(String.format(mContext
                        .getString(R.string.parser_reason), reason));
    }

    private void setupEvent(final CharSequence message) {
        mMessage = new EventDecorator(message);
    }

    private void setupEvent(final CharSequence message, final boolean boldText) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(message);
        if (boldText) {
            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        setupEvent(builder);
    }

    private void setupEvent(final CharSequence message, final Nick defaultNick) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(message);
        final NickColour colour = NickCache.getNickCache().get(defaultNick);
        builder.setSpan(new ForegroundColorSpan(colour.getColour()), 0, message.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        setupEvent(builder);
    }

    private void setupEvent(final CharSequence message, final Nick defaultNick,
            final boolean boldText) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(message);
        final NickColour colour = NickCache.getNickCache().get(defaultNick);
        builder.setSpan(new ForegroundColorSpan(colour.getColour()), 0, message.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        setupEvent(builder, boldText);
    }

    private boolean shouldHighlightLine() {
        return AppPreferences.getAppPreferences().shouldHighlightLine();
    }

    private ForegroundColorSpan getColourForUser(final Nick nick) {
        return new ForegroundColorSpan(NickCache.getNickCache().get(nick).getColour());
    }

    private FormattedString getFormattedStringForUser(final WorldUser user) {
        return getFormattedStringForNick(user.getNick());
    }

    private FormattedString getFormattedStringForNick(final Nick nick) {
        return new FormattedString(nick.getNickAsString(), getColourForUser(nick));
    }
}

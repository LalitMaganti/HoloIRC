package com.fusionx.lightirc.communication;

import com.fusionx.androidirclibrary.interfaces.EventStringResponses;
import com.fusionx.lightirc.R;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;

public class EventResponses implements EventStringResponses {

    private final Context mContext;

    EventResponses(final Context context) {
        mContext = context;
    }

    // Status constants
    @Override
    public String getConnectedStatus() {
        return mContext.getString(R.string.status_connected);
    }

    @Override
    public String getDisconnectedStatus() {
        return mContext.getString(R.string.status_disconnected);
    }

    @Override
    public String getConnectingStatus() {
        return mContext.getString(R.string.status_connecting);
    }

    // Messages
    @Override
    public String getInitialTopicMessage(String topic, String topicSetter) {
        final String response = mContext.getString(R.string.parser_new_topic);
        return String.format(response, topic, topicSetter);
    }

    @Override
    public String getOnConnectedMessage(final String serverUrl) {
        final String response = mContext.getString(R.string.parser_connected);
        return String.format(response, serverUrl);
    }

    @Override
    public String getJoinMessage(final String nick) {
        final String response = mContext.getString(R.string.parser_joined_channel);
        return String.format(response, nick);
    }

    @Override
    public String getModeChangedMessage(final String mode, final String triggerNick,
            final String recipientNick) {
        final String response = mContext.getString(R.string.parser_mode_changed);
        return String.format(response, mode, triggerNick, recipientNick);
    }

    @Override
    public String getNickChangedMessage(String oldNick, String newNick, boolean isUser) {
        final String response = mContext.getString(isUser ?
                R.string.parser_appuser_nick_changed :
                R.string.parser_other_user_nick_change);
        return String.format(response, oldNick, newNick);
    }

    @Override
    public String getTopicChangedMessage(final String setterNick, final String oldTopic,
            final String newTopic) {
        final String response = mContext.getString(R.string.parser_topic_changed);
        return String.format(response, newTopic, setterNick);
    }

    @Override
    public String getUserKickedMessage(final String kickedUserNick, final String kickingUserNick,
            final String reason) {
        final String response = mContext.getString(R.string.parser_kicked_channel);
        final String formattedResponse = String.format(response, kickedUserNick, kickingUserNick);
        return appendReasonIfNeeded(formattedResponse, reason);
    }

    @Override
    public String getOnUserKickedMessage(final String name, final String nick,
            final String reason) {
        final String response = mContext.getString(R.string.parser_user_kicked_channel);
        final String formattedResponse = String.format(response, name, nick);
        return appendReasonIfNeeded(formattedResponse, reason);
    }

    @Override
    public String getPartMessage(String nick, String reason) {
        final String response = mContext.getString(R.string.parser_parted_channel);
        final String formattedResponse = String.format(response, nick);
        return appendReasonIfNeeded(formattedResponse, reason);
    }

    @Override
    public String getQuitMessage(String nick, String reason) {
        final String response = mContext.getString(R.string.parser_quit_server);
        final String formattedResponse = String.format(response, nick);
        return appendReasonIfNeeded(formattedResponse, reason);
    }

    private String appendReasonIfNeeded(final String response, final String reason) {
        return StringUtils.isEmpty(reason) ? response :
                response + " " + String.format(mContext.getString(R.string.parser_reason), reason);
    }

    @Override
    public String getMessage(final String sendingNick, final String rawMessage) {
        final String response = mContext.getString(R.string.parser_message);
        return String.format(response, sendingNick, rawMessage);
    }

    @Override
    public String getNoticeMessage(final String sendingUser, final String notice) {
        final String response = mContext.getString(R.string.parser_notice);
        return String.format(response, sendingUser, notice);
    }

    @Override
    public String getActionMessage(final String sendingNick, final String action) {
        final String response = mContext.getString(R.string.parser_action);
        return String.format(response, sendingNick, action);
    }

    // Errors
    @Override
    public String getNickInUserError() {
        return mContext.getString(R.string.error_nick_in_use);
    }
}

package com.fusionx.lightirc.communication;

import com.fusionx.androidirclibrary.interfaces.EventStringResponses;
import com.fusionx.lightirc.R;

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
    public String getMessage(String sendingNick, String rawMessage) {
        final String response = mContext.getString(R.string.parser_message);
        return String.format(response, sendingNick, rawMessage);
    }

    @Override
    public String getActionMessage(String sendingNick, String action) {
        final String response = mContext.getString(R.string.parser_action);
        return String.format(response, sendingNick, action);
    }

    // Errors
    @Override
    public String getNickInUserError() {
        return mContext.getString(R.string.error_nick_in_use);
    }
}

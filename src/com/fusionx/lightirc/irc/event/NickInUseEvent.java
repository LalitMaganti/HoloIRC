package com.fusionx.lightirc.irc.event;

import android.content.Context;

import com.fusionx.lightirc.R;

public class NickInUseEvent extends ServerEvent {
    public NickInUseEvent(Context context) {
        super(context.getString(R.string.parser_nick_in_use));
    }
}

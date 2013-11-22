package com.fusionx.lightirc.irc.event;

import com.fusionx.lightirc.R;

import android.content.Context;

public class NickInUseEvent extends ServerEvent {

    public NickInUseEvent(Context context) {
        super(context.getString(R.string.parser_nick_in_use));
    }
}

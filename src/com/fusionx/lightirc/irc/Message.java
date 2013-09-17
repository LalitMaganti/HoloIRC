package com.fusionx.lightirc.irc;

import android.text.Spanned;
import android.text.format.Time;

import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.util.ColourParserUtils;

public class Message {
    public final String timestamp;
    public final Spanned message;

    public Message(String message) {
        if(AppPreferences.timestamp) {
            final Time now = new Time();
            now.setToNow();
            this.timestamp = now.format("%H:%M: ");
        } else {
            this.timestamp = "";
        }
        this.message = ColourParserUtils.parseMarkup(message);
    }
}

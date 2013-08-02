package com.fusionx.ircinterface.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.fusionx.ircinterface.enums.CoreEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.ircinterface.writers.ServerWriter;

public class CoreListener extends BroadcastReceiver {
    private final ServerWriter mServerWriter;

    public CoreListener(final ServerWriter writer) {
        mServerWriter = writer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Event event = intent.getExtras().getParcelable("event");
        if (event != null && event.getType().equals(CoreEventType.Ping)) {
            mServerWriter.pongServer(event.getMessage()[0]);
        }
    }
}
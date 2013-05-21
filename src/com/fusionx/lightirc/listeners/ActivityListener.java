package com.fusionx.lightirc.listeners;

import android.os.Handler;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.IRCFragment;
import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.irc.LightChannel;
import com.fusionx.lightirc.misc.EventOutput;
import org.pircbotx.Channel;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;

import java.util.ArrayList;

public class ActivityListener extends IRCListener {
    private final ServerChannelActivity activity;
    private final IRCPagerAdapter mIRCPagerAdapter;

    public ActivityListener(ServerChannelActivity a, IRCPagerAdapter d) {
        activity = a;
        mIRCPagerAdapter = d;
    }

    @Override
    public void onEvent(final Event<LightBot> event) throws Exception {
        super.onEvent(event);
        if (event instanceof MotdEvent || event instanceof NoticeEvent) {
            final IRCFragment server = (IRCFragment) mIRCPagerAdapter.getItem(0);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server.writeToTextView(EventOutput.getOutputForEvent(event));
                }
            });
        }
    }

    @Override
    public void onMessage(final MessageEvent<LightBot> event) {
        sendMessage(event.getChannel().getName(), event);
    }

    @Override
    public void onAction(final ActionEvent<LightBot> event) {
        sendMessage(event.getChannel().getName(), event);
    }

    @Override
    public void onQuit(final QuitEvent<LightBot> event) {
        // Keep this code up to date with ChannelListener
        for (final Channel c : event.getUser().getChannels()) {
            if (event.getBot().getUserBot().getChannels().contains(c)) {
                sendMessage(c.getName(), event);
            }
        }
    }

    @Override
    public void onPart(final PartEvent<LightBot> event) {
        if (!event.getUser().getNick().equals(event.getBot().getNick())) {
            sendMessage(event.getChannel().getName(), event);
        }
    }

    @Override
    public void onNickChange(final NickChangeEvent<LightBot> event) {
        // Keep this code up to date with ChannelListener
        for (final Channel c : event.getBot().getUserBot().getChannels()) {
            ArrayList<String> set = ((LightChannel) c).getCleanUserNicks();
            if (set.contains(event.getOldNick()) || set.contains(event.getNewNick())) {
                sendMessage(c.getName(), event);
            }
        }
    }

    @Override
    public void onJoin(final JoinEvent<LightBot> event) {
        if (!((JoinEvent) event).getUser().getNick().equals(event.getBot().getNick())) {
            sendMessage(event.getChannel().getName(), event);
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.onNewChannelJoined(((JoinEvent) event).getChannel().getName(), ((JoinEvent) event).getUser().getNick(), EventOutput.getOutputForEvent(event));
                }
            });
        }
    }

    @Override
    public void onTopic(final TopicEvent<LightBot> event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final IRCFragment channel = mIRCPagerAdapter.getTab(((TopicEvent) event).getChannel().getName());
                        channel.writeToTextView(EventOutput.getOutputForEvent(event));
                    }
                }, 1500);
            }
        });
    }

    public void sendMessage(final String title, final Event event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final IRCFragment channel = mIRCPagerAdapter.getTab(title);
                channel.writeToTextView(EventOutput.getOutputForEvent(event));
            }
        });
    }
}
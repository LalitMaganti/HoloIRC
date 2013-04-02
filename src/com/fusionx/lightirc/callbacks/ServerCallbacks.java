package com.fusionx.lightirc.callbacks;

public interface ServerCallbacks {
	public void onServerWriteNeeded(String message);
	public void onNewChannelJoined(String channel, String nick, String buffer);
}
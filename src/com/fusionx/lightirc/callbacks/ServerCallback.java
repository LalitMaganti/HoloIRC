package com.fusionx.lightirc.callbacks;

public interface ServerCallback {
	public void writeToTextView(final String message);

	public void onNewChannelJoined(final String channel, final String nick,
			final String buffer);
}
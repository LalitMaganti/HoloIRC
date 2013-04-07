package com.fusionx.lightirc.callbacks;

public interface ChannelCallbacks {
	public void writeToTextView(String message);
	public void userListChanged(String newList[]);
}
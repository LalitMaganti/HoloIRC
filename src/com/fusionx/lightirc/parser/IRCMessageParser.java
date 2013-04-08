package com.fusionx.lightirc.parser;

import com.fusionx.lightirc.services.IRCService;

import android.content.BroadcastReceiver;

public abstract class IRCMessageParser extends BroadcastReceiver {
	private IRCService mService;

	public IRCService getService() {
		return mService;
	}

	public void setService(IRCService service) {
		mService = service;
	}
}

package com.fusionx.lightirc.listeners;

import org.pircbotx.PircBotX;

import com.fusionx.lightirc.runnables.ChannelLogRunnable;

import android.app.Activity;
import android.text.method.TextKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class ChannelEditTextKeyListener implements OnKeyListener {
	final PircBotX bo;
	final String channelName;
	final View rootView;
	final Activity a;
	
	public ChannelEditTextKeyListener(final PircBotX bot, final String channelName, final View rootView, final Activity a) {
		this.bo = bot;
		this.channelName = channelName;
		this.rootView = rootView;
		this.a = a;
	}
	
	@Override
	public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
		if ((event.getAction() == KeyEvent.ACTION_DOWN)
				&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
			
			EditText t = (EditText) v;
			
			// Not correct - need to parse this string
			bo.sendMessage(bo.getChannel(channelName), t.getText()
					.toString());
			
			a.runOnUiThread(
							new ChannelLogRunnable(bo.getNick() + ": " + t.getText().toString(), rootView));
			TextKeyListener.clear(t.getText());
			t.requestFocus();
			return true;
		}
		return false;
	}
}
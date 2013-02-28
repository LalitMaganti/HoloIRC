package com.fusionx.lightirc.fragments;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fusionx.lightirc.R;

public class ChannelFragment extends IRCFragment {
	public ChannelFragment() {
	}
	PircBotX bo;

	public ChannelFragment(PircBotX bot) {
		title = "#testingircandroid";
		bo = bot;
		final MessageListener e = new MessageListener();
		bo.getListenerManager().addListener(e);
	}

	class updateTextView implements Runnable {
		String line;

		public updateTextView(String s) {
			line = s;
		}

		@Override
		public void run() {
			final TextView textView = (TextView) getView().findViewById(
					R.id.textview);
			final ScrollView scrollView = (ScrollView) getView().findViewById(
					R.id.scrollview);
			textView.append(line + "\n");
			scrollView.fullScroll(View.FOCUS_DOWN);
		}
	}

	@SuppressWarnings("rawtypes")
	public class MessageListener extends ListenerAdapter implements Listener {
		@Override
		public void onMessage(MessageEvent event) throws Exception {
			String newLine = event.getUser().getNick() + ": " + event.getMessage();
			getActivity().runOnUiThread(new updateTextView(newLine));
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_irc_channel,
				container, false);
		final EditText textView = (EditText) rootView.findViewById(R.id.editText1);
		textView.setOnKeyListener(new OnKeyListener() {
			@Override
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		        	bo.sendMessage(bo.getChannel(title), textView.getText().toString());
					getActivity().runOnUiThread(new updateTextView(bo.getNick() + ": " + textView.getText().toString()));
		        	return true;
		        }
		        return false;
		    }
		});
		return rootView;
	}
}
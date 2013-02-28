package com.fusionx.lightirc.fragments;

import org.pircbotx.PircBotX;

import com.fusionx.lightirc.ServerChannelArea;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.listeners.ConnectionListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class ServerFragment extends IRCFragment {

	final String serverUrl = "irc.freenode.org";
	final String nick = "test1223";
	final PircBotX bot = new ServerBot();

	public ServerFragment() {
		title = "Freenode";
	}

	public class BackgroundConnector extends AsyncTask<PircBotX, Void, Void> {
		@Override
		protected Void doInBackground(final PircBotX... bot) {
			try {
				bot[0].connect(serverUrl);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	public class ServerBot extends PircBotX {
		Boolean log = true;
		@Override
		public void log(final String line) {
			Runnable d = new Runnable() {
				@Override
				public void run() {
					if (line.startsWith("<<<:") && log) {
						String modLine = line.substring(4);
						final TextView textView = (TextView) getView()
								.findViewById(R.id.textview);
						final ScrollView scrollView = (ScrollView) getView()
								.findViewById(R.id.scrollview);
						textView.append(modLine + "\n");
						scrollView.fullScroll(View.FOCUS_DOWN);
						if (modLine.contains("JOIN")) {
							((ServerChannelArea) (getActivity())).mSectionsPagerAdapter
									.addView(new ChannelFragment(bot));
							((ServerChannelArea) (getActivity())).addTab(1);
							log = false;
						}
					}
				}
			};
			getActivity().runOnUiThread(d);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		bot.setName(nick);
		bot.setAutoNickChange(true);

		final BackgroundConnector k = new BackgroundConnector();
		k.execute(bot, null, null);

		final ConnectionListener e = new ConnectionListener();
		bot.getListenerManager().addListener(e);

		final View rootView = inflater.inflate(R.layout.fragment_irc_channel,
				container, false);
		return rootView;
	}
}
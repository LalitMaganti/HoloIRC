package com.fusionx.lightirc.activity;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ServerObjectArrayAdapter;
import com.fusionx.lightirc.misc.ServerObject;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class MainServerListActivity extends ListActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_server);
		
		getSetServerList();

		setListAdapter(new ServerObjectArrayAdapter(this, getSetServerList()));
	}

	private ServerObject[] getSetServerList() {
		SharedPreferences settings = getSharedPreferences("main", 0);
		boolean firstRun = settings.getBoolean("firstrun", true);
		int noOfServers = settings.getInt("noOfServers", 0);
		ServerObject[] values;
		Editor e = settings.edit();
		
		if (firstRun || noOfServers == 0) {
			ServerObject freenode = new ServerObject();
			freenode.url = "irc.freenode.net";
			freenode.nick = "LightIRCUser";
			freenode.title = "Freenode";
			freenode.autoJoinChannels = new String[] {"#testingircandroid"};
			values = new ServerObject[] { freenode };
			noOfServers = 1;
			for (String s : freenode.toHashMap().keySet()) {
				e.putString("server_0_" + s, freenode.toHashMap().get(s));
			}
			e.putString("server_0_autoJoin_channel", "#testingircandroid");
			e.putInt("server_0_autoJoin_no", 1);
		} else {
			values = new ServerObject[noOfServers];
			for (int i = 0; i < noOfServers; i++) {
				values[i].url = settings.getString("server_" + i + "_url", "");
				values[i].userName = settings.getString("server_" + i + "_userName", "");
				values[i].nick = settings.getString("server_" + i + "_nick", "");
				values[i].serverPassword = settings.getString("server_" + i + "_serverPassword", "");
				values[i].title = settings.getString("server_" + i + "_title", "");
				
				String[] s = new String[settings.getInt("server_" + i + "_autoJoin_no", 0)];
				for (int j = 0; j < s.length; j++) {
					s[j] = settings.getString("server_" + i + "_autoJoin_channel_" + j, "");
				}
				values[i].autoJoinChannels = s;
			}
		}

		e.putBoolean("firstrun", true);
		e.putInt("noOfServers", noOfServers);
		e.commit();
		return values;
	}

	@Override
	protected void onListItemClick(final ListView l, final View v,
			final int position, final long id) {
		ServerObject server = (ServerObject) getListView().getItemAtPosition(
				position);
		Intent intent = new Intent(MainServerListActivity.this,
				ServerChannelActivity.class);
		intent.putExtra("serverObject", server);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main_server_list, menu);
		return true;
	}
}
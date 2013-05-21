/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.*;
import android.view.View.OnClickListener;
import com.fima.cardsui.views.CardUI;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.ServerSettingsActivity.BaseServerSettingFragment;
import com.fusionx.lightirc.irc.LightBuilder;
import com.fusionx.lightirc.misc.ServerCard;

import java.util.ArrayList;

public class MainServerListActivity extends Activity implements
        OnClickListener, ActionMode.Callback {
    public ArrayList<LightBuilder> actionModeItems = new ArrayList<LightBuilder>();
    public boolean actionModeStarted = false;
    public ActionMode mMode;
    private ArrayList<LightBuilder> values = null;

    private void connectToServer(LightBuilder builder) {
        final Intent intent = new Intent(MainServerListActivity.this,
                ServerChannelActivity.class);
        intent.putExtra("server", builder);
        startActivity(intent);
    }

    private void editServer(int position) {
        final LightBuilder server = actionModeItems.get(position);

        Intent intent = new Intent(MainServerListActivity.this,
                ServerSettingsActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                BaseServerSettingFragment.class.getName());
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        intent.putExtra("indexOfServer", position);
        intent.putExtra("server", server);
        startActivity(intent);
    }

    private void getSetServerList() {
        final SharedPreferences settings = getSharedPreferences("main", 0);
        final boolean firstRun = settings.getBoolean("firstrun", true);
        final int noOfServers = settings.getInt("noOfServers", 0);
        values = new ArrayList<LightBuilder>();

        if (firstRun) {
            final Editor e = settings.edit();
            LightBuilder freenode = new LightBuilder();

            freenode.setLogin("LightIRCUser");
            freenode.setName("LightIRCUser");
            freenode.setServerHostname("irc.freenode.net");
            freenode.setTitle("Freenode");
            freenode.addAutoJoinChannel("#testingircandroid");
            freenode.addAutoJoinChannel("#huawei-g300");
            values.add(freenode);

            for (String s : freenode.toHashMap().keySet()) {
                e.putString("server_0_" + s, freenode.toHashMap().get(s));
            }

            e.putBoolean("firstrun", false);
            e.putString("server_0_autoJoin_channel_0", "#testingircandroid");
            e.putString("server_0_autoJoin_channel_1", "#huawei-g300");
            e.putInt("server_0_autoJoin_no", 2);
            e.putInt("noOfServers", 1);
            e.commit();
        } else if (noOfServers != 0) {
            for (int i = 0; i < noOfServers; i++) {
                LightBuilder bot = new LightBuilder();
                bot.setServerHostname(settings.getString(
                        "server_" + i + "_url", ""));
                bot.setLogin(settings
                        .getString("server_" + i + "_userName", ""));
                bot.setName(settings.getString("server_" + i + "_nick", ""));

                bot.setServerPassword(settings.getString("server_" + i
                        + "_serverPassword", ""));
                bot.setTitle(settings.getString("server_" + i + "_title", ""));
                int noOfAutoJoinChannels = settings.getInt("server_" + i
                        + "_autoJoin_no", 0);

                for (int j = 0; j < noOfAutoJoinChannels; j++) {
                    String channel = settings.getString("server_" + i
                            + "_autoJoin_channel_" + j, "");
                    bot.addAutoJoinChannel(channel);
                }
                values.add(bot);
            }
        }

        if (!values.isEmpty()) {
            CardUI mCardView = (CardUI) findViewById(R.id.cardsview);
            mCardView.setSwipeable(false);
            for (LightBuilder bot : values) {
                ServerCard server = new ServerCard(bot.getTitle(),
                        "Not connected", bot);
                server.setOnClickListener(this);
                mCardView.addCard(server);
            }

            mCardView.refresh();
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                editServer(0);
                mode.finish();
                return true;
            case R.id.connect:
                connectToServer(actionModeItems.get(0));
                mode.finish();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        connectToServer((LightBuilder) v.getTag());
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_server);

        getSetServerList();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_server_long_press, menu);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode arg0) {
        actionModeItems.clear();
        actionModeStarted = false;
        mMode = null;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle("Selected 1 server");
        actionModeStarted = true;
        mMode = mode;
        return false;
    }

    @Override
    protected void onResume() {
        if (values == null) {
            getSetServerList();
        }
        super.onResume();
    }

    public void updateActionMode() {
        mMode.setTitle("Selected " + actionModeItems.size() + " servers");
        mMode.getMenu().getItem(0).setVisible(actionModeItems.size() == 1);
        mMode.getMenu().getItem(1).setVisible(actionModeItems.size() == 1);
    }
}
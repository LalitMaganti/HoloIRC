package com.fusionx.lightirc.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.uisubclasses.ChannelNamePromptDialog;
import org.pircbotx.PircBotX;

public class ServerChannelActionsFragment extends ListFragment implements AdapterView.OnItemClickListener {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String[] values = new String[]{"Join new channel", "Disconnect"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        final IRCService service = ((ServerChannelActivity) getActivity()).getService();
        final PircBotX bot = service.getBot(((ServerChannelActivity) getActivity()).getBuilder().getTitle());
        switch (i) {
            case 0:
                final ChannelNamePromptDialog dialog = new ChannelNamePromptDialog(getActivity()) {
                    @Override
                    public void onOkClicked(final DialogInterface dialog, String input) {
                        bot.sendIRC().joinChannel(input);
                    }
                };
                dialog.show();
                break;
            case 1:
                ((ServerChannelActivity) getActivity()).disconnect();
                break;
        }
    }
}
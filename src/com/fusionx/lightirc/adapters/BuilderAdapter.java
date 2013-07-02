package com.fusionx.lightirc.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.service.IRCService;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class BuilderAdapter extends ArrayAdapter<Configuration.Builder> {
    private final MainServerListActivity mActivity;
    private final IRCService mService;

    public BuilderAdapter(final IRCService service, final MainServerListActivity activity) {
        super(activity, android.R.layout.simple_list_item_1);
        mService = service;
        mActivity = activity;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            final LayoutInflater vi = mActivity.getLayoutInflater();
            v = vi.inflate(R.layout.item_server_card, parent, false);
        }

        final TextView tt = (TextView) v.findViewById(R.id.title);
        final TextView bt = (TextView) v.findViewById(R.id.description);
        if (tt != null) {
            tt.setText(getItem(position).getTitle());
        }
        if (bt != null) {
            final PircBotX bot = mService.getBot(getItem(position).getTitle());
            if (bot != null) {
                bt.setText(bot.getStatus());
            } else {
                bt.setText(mActivity.getString(R.string.status_disconnected));
            }
        }

        LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.overflow_menu);
        linearLayout.setTag(getItem(position));

        linearLayout = (LinearLayout) v.findViewById(R.id.contentLayout);
        linearLayout.setTag(getItem(position));

        return v;
    }
}
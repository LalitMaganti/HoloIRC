package com.fusionx.lightirc.ui.widget;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.ui.ServerPreferenceActivity;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class ServerCard implements ServerCardInterface, View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {

    private final Context mContext;

    private final ServerConfiguration.Builder mServer;

    private final ServerCardCallback mCallback;

    public ServerCard(final Context context,
            final ServerConfiguration.Builder server,
            final ServerCardCallback callback) {
        mContext = context;
        mServer = server;
        mCallback = callback;
    }

    @Override
    public String getTitle() {
        return mServer.getTitle();
    }

    @Override
    public View getView(final View convertView, final ViewGroup parent, final Server server) {
        View view;
        view = convertView != null ? convertView : LayoutInflater.from(mContext)
                .inflate(R.layout.item_server_card, parent, false);

        final View content = view.findViewById(R.id.card_server_content);
        final TextView title = (TextView) content.findViewById(R.id.card_server_title);
        final TextView description = (TextView) content.findViewById(R.id.card_server_description);
        if (title != null) {
            title.setText(mServer.getTitle());
        }
        if (description != null) {
            description.setText(server != null && server.getStatus() != null ? server.getStatus()
                    : mContext.getString(R.string.status_disconnected));
        }
        content.setOnClickListener(this);

        final View overflow = view.findViewById(R.id.card_server_overflow);
        overflow.setOnClickListener(overflowClickListener);

        return view;
    }

    @Override
    public void onCardDismiss() {
        final File folder = new File(SharedPreferencesUtils
                .getSharedPreferencesPath(mContext) + mServer.getFile() + ".xml");
        folder.delete();
    }

    @Override
    public void onCardEdit(final ArrayList<String> currentlyDisplayedCards) {
        final Intent intent = new Intent(mContext, ServerPreferenceActivity.class);

        intent.putExtra("file", mServer.getFile());
        intent.putExtra("server", mServer);
        intent.putStringArrayListExtra("list", currentlyDisplayedCards);
        mContext.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(mContext, UIUtils.getIRCActivity(mContext));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("server", mServer);
        mContext.startActivity(intent);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.activity_server_list_popup_edit:
                onCardEdit(mCallback.getServerTitles(this));
                break;
            case R.id.activity_server_list_popup_disconnect:
                mCallback.disconnectFromServer(this);
                break;
            case R.id.activity_server_list_popup_delete:
                mCallback.deleteServer(this);
                break;
            default:
                return false;
        }
        return true;
    }

    private final View.OnClickListener overflowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final PopupMenu popup = new PopupMenu(mContext, view);
            popup.inflate(R.menu.activity_server_list_popup);

            if (mCallback.isServerAvailable(mServer.getTitle())) {
                popup.getMenu().getItem(1).setEnabled(false);
                popup.getMenu().getItem(2).setEnabled(false);
            } else {
                popup.getMenu().getItem(0).setEnabled(false);
            }

            popup.setOnMenuItemClickListener(ServerCard.this);
            popup.show();
        }
    };

    public interface ServerCardCallback {

        public boolean isServerAvailable(final String title);

        public void deleteServer(final ServerCard card);

        public void disconnectFromServer(final ServerCard card);

        public ArrayList<String> getServerTitles(final ServerCard card);
    }
}

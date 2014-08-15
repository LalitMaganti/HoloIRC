package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnCurrentServerStatusChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.ConnectionStatus;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.UIUtils.resolveResourceIdFromAttr;

// TODO - rewrite this horribly written class
public class ActionsAdapter extends ArrayAdapter<String> implements StickyListHeadersAdapter {

    private final LayoutInflater mInflater;

    private final int mServerItemCount;

    private final String[] mChannelArray;

    private final String[] mUserArray;

    private ConnectionStatus mStatus = ConnectionStatus.DISCONNECTED;

    private FragmentType mFragmentType = FragmentType.SERVER;

    @SuppressWarnings("FieldCanBeLocal")
    private final Object mEventHandler = new Object() {
        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            mFragmentType = conversationChanged.fragmentType;
            if (conversationChanged.conversation != null) {
                mStatus = conversationChanged.conversation.getServer().getStatus();
            }
            notifyDataSetChanged();
        }

        @Subscribe
        public void onEvent(final OnCurrentServerStatusChanged statusChanged) {
            mStatus = statusChanged.status;
            notifyDataSetChanged();
        }
    };

    public ActionsAdapter(final Context context) {
        super(context, R.layout.default_listview_textview, new ArrayList<>(Arrays.asList
                (context.getResources().getStringArray(R.array.server_actions))));
        mInflater = LayoutInflater.from(context);
        mServerItemCount = super.getCount();
        mChannelArray = context.getResources().getStringArray(R.array.channel_actions);
        mUserArray = context.getResources().getStringArray(R.array.user_actions);

        getBus().registerSticky(mEventHandler);
    }

    @Override
    public View getHeaderView(final int i, final View convertView, final ViewGroup viewGroup) {
        final TextView otherHeader = (TextView) (convertView == null
                ? mInflater.inflate(R.layout.sliding_menu_header, viewGroup, false)
                : convertView);

        if (i == 0 && convertView == null) {
            otherHeader.setText(getContext().getString(R.string.server));
        } else if (i == getRealServerCount()) {
            otherHeader.setText(mFragmentType == FragmentType.CHANNEL ? getContext()
                    .getString(R.string.channel) : getContext().getString(R.string.user));
        }
        return otherHeader;
    }

    @Override
    public long getHeaderId(int i) {
        return i < getRealServerCount() ? 0 : 1;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(final int position) {
        return position != 0 && position != 1 && position != 3 && position < getRealServerCount()
                || isConnected();
    }

    @Override
    public int getCount() {
        if (mFragmentType == FragmentType.SERVER) {
            return getRealServerCount();
        } else if (mFragmentType == FragmentType.CHANNEL) {
            return getRealServerCount() + mChannelArray.length;
        } else {
            return getRealServerCount() + mUserArray.length;
        }
    }

    @Override
    public String getItem(int position) {
        if (position < getRealServerCount()) {
            return super.getItem(position);
        } else if (mFragmentType == FragmentType.CHANNEL) {
            return mChannelArray[getCount() - 1 - position];
        } else if (mFragmentType == FragmentType.USER) {
            return mUserArray[getCount() - 1 - position];
        } else {
            return "";
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) mInflater.inflate(R.layout.default_listview_textview, parent, false);
            UIUtils.setRobotoLight(getContext(), row);
        }

        if (position == 4) {
            // TODO - improve UX by making this more precise to the specific status of the
            // connection
            row.setText(isConnected() ? getItem(position) : getContext().getString(R.string
                    .action_close_server));
        } else {
            row.setText(getItem(position));
        }

        if (!isEnabled(position)) {
            row.setTextColor(Color.GRAY);
        } else {
            final int resId = resolveResourceIdFromAttr(getContext(), R.attr.default_text_colour);
            final int colour = UIUtils.getColourFromResource(getContext(), resId);
            row.setTextColor(colour);
        }

        return row;
    }

    boolean isConnected() {
        return mStatus == ConnectionStatus.CONNECTED;
    }

    boolean isDisconnected() {
        return mStatus == ConnectionStatus.DISCONNECTED;
    }

    private int getRealServerCount() {
        return mServerItemCount - (isDisconnected() ? 0 : 1);
    }
}
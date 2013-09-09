package com.fusionx.lightirc.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.util.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;

import lombok.Getter;

public class ActionsAdapter extends ArrayAdapter<String> implements StickyListHeadersAdapter {
    private final LayoutInflater inflater;
    protected Context mContext;
    private final int mServerItemCount;

    @Getter
    private boolean connected = false;
    private FragmentTypeEnum mFragmentType = FragmentTypeEnum.Server;

    private final String[] channelArray;
    private final String[] userArray;

    public ActionsAdapter(final Context context) {
        super(context, R.layout.default_listview_textview, new ArrayList<>(Arrays.asList
                (context.getResources().getStringArray(R.array.server_actions))));
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mServerItemCount = super.getCount();
        channelArray = context.getResources().getStringArray(R.array.channel_actions);
        userArray = context.getResources().getStringArray(R.array.user_actions);
    }

    @Override
    public View getHeaderView(int i, View convertView, ViewGroup viewGroup) {
        final TextView otherHeader = (TextView) (convertView == null ? inflater.inflate(R.layout
                .sliding_menu_header, null, false) : convertView);
        if (i == 0 && convertView == null) {
            otherHeader.setText(getContext().getString(R.string.server));
        } else if (i == mServerItemCount) {
            otherHeader.setText(mFragmentType == FragmentTypeEnum.Channel ? getContext()
                    .getString(R.string.channel) : getContext().getString(R.string.user));
        }
        return otherHeader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) inflater.inflate(R.layout.default_listview_textview, parent, false);
        }
        UIUtils.setRobotoLight(getContext(), row);
        if (position == 2) {
            row.setText(connected ? "Disconnect" : "Close");
        } else {
            row.setText(getItem(position));
        }

        if (!isEnabled(position)) {
            row.setTextColor(Color.GRAY);
        } else {
            row.setTextColor(UIUtils.getThemedTextColor(getContext()));
        }

        return row;
    }

    @Override
    public long getHeaderId(int i) {
        return i < mServerItemCount ? 0 : 1;
    }

    public void setFragmentType(FragmentTypeEnum fragmentType) {
        if (mFragmentType != fragmentType) {
            mFragmentType = fragmentType;
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public String getItem(int position) {
        if (position < mServerItemCount) {
            return super.getItem(position);
        } else if (mFragmentType == FragmentTypeEnum.Channel) {
            return channelArray[getCount() - position - 1];
        } else if (mFragmentType == FragmentTypeEnum.User) {
            return userArray[getCount() - position - 1];
        } else {
            return "";
        }
    }

    @Override
    public int getCount() {
        if (mFragmentType == FragmentTypeEnum.Server) {
            return mServerItemCount;
        } else if (mFragmentType == FragmentTypeEnum.Channel) {
            return mServerItemCount + channelArray.length;
        } else {
            return mServerItemCount + userArray.length;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return !((position == 0) || (position == 1)) || connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
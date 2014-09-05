package com.fusionx.lightirc.ui;

import com.google.common.collect.FluentIterable;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.NickCache;
import com.fusionx.lightirc.util.UIUtils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.ChannelUser;
import co.fusionx.relay.base.Nick;
import co.fusionx.relay.constants.UserLevel;
import co.fusionx.relay.function.FluentIterables;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private final Context mContext;

    private final LayoutInflater mInflater;

    private final Channel mChannel;

    private final List<Pair<Nick, UserLevel>> mUsers;

    private final UserListComparator mComparator;

    public UserListAdapter(final Context context, final Channel channel) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mChannel = channel;

        mComparator = new UserListComparator();
        mUsers = new ArrayList<>();

        FluentIterable.from(channel.getUsers())
                .transform(u -> new Pair<>(u.getNick(), u.getChannelPrivileges(channel)))
                .copyInto(mUsers);
        Collections.sort(mUsers, mComparator);
    }

    public Pair<Nick, UserLevel> getItem(final int position) {
        return mUsers.get(position);
    }

    @Override
    public UserViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = mInflater.inflate(R.layout.default_listview_textview, parent, false);
        final UserViewHolder userViewHolder = new UserViewHolder(view);
        UIUtils.setRobotoLight(mContext, userViewHolder.textView);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {
        final Pair<Nick, UserLevel> user = getItem(position);
        final UserLevel level = user.second;
        final char prefix = level == null ? '\0' : level.getPrefix();
        final SpannableStringBuilder builder = new SpannableStringBuilder(
                prefix + user.first.getNickAsString());
        final ForegroundColorSpan span = new ForegroundColorSpan(
                NickCache.getNickCache().get(user.first).getColour());
        builder.setSpan(span, 0, builder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        holder.textView.setText(builder);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void addUser(final ChannelUser user) {
        if (user == null) {
            return;
        }

        final Pair<Nick, UserLevel> pair = new Pair<>(user.getNick(),
                user.getChannelPrivileges(mChannel));
        addPair(pair);
    }

    public void removeUser(final ChannelUser user, final UserLevel level) {
        if (user == null) {
            return;
        }

        final Pair<Nick, UserLevel> pair = new Pair<>(user.getNick(), level);
        removePair(pair);
    }

    public void changeNick(final ChannelUser user, final Nick oldNick, final Nick newNick) {
        if (user == null) {
            return;
        }

        final UserLevel level = user.getChannelPrivileges(mChannel);

        final Pair<Nick, UserLevel> oldPair = new Pair<>(oldNick, level);
        removePair(oldPair);

        final Pair<Nick, UserLevel> newPair = new Pair<>(newNick, level);
        addPair(newPair);
    }

    public void changeMode(final ChannelUser user, final UserLevel oldLevel,
            final UserLevel newLevel) {
        if (user == null) {
            return;
        }

        final Nick nick = user.getNick();

        final Pair<Nick, UserLevel> oldPair = new Pair<>(nick, oldLevel);
        removePair(oldPair);

        final Pair<Nick, UserLevel> newPair = new Pair<>(nick, newLevel);
        addPair(newPair);
    }

    private void addPair(final Pair<Nick, UserLevel> pair) {
        final int index = Collections.binarySearch(mUsers, pair, mComparator);
        if (index < 0) {
            final int actual = ~index;
            mUsers.add(actual, pair);
            notifyItemInserted(actual);
        } else {
            // TODO - this is invalid
            Log.e("HoloIRC", "Invalid");
        }
    }

    private void removePair(final Pair<Nick, UserLevel> pair) {
        final int index = Collections.binarySearch(mUsers, pair, mComparator);
        if (index < 0) {
            // TODO - this is invalid
            Log.e("HoloIRC", "Invalid");
        } else {
            mUsers.remove(index);
            notifyItemRemoved(index);
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public UserViewHolder(final View itemView) {
            super(itemView);

            textView = (TextView) itemView;
        }
    }

    private class UserListComparator implements Comparator<Pair<Nick, UserLevel>> {

        @Override
        public int compare(final Pair<Nick, UserLevel> lhs, final Pair<Nick, UserLevel> rhs) {
            final UserLevel firstUserMode = lhs.second;
            final UserLevel secondUserMode = rhs.second;

            /**
             * Code for compatibility with objects being removed
             */
            if (firstUserMode == null && secondUserMode == null) {
                return 0;
            } else if (firstUserMode == null) {
                return -1;
            } else if (secondUserMode == null) {
                return 1;
            }

            if (firstUserMode.equals(secondUserMode)) {
                final String firstRemoved = lhs.first.getNickAsString();
                final String secondRemoved = rhs.first.getNickAsString();

                return firstRemoved.compareToIgnoreCase(secondRemoved);
            } else if (firstUserMode.ordinal() > secondUserMode.ordinal()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
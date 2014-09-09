package com.fusionx.lightirc.ui;

import com.google.common.collect.FluentIterable;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.NickCache;
import com.fusionx.lightirc.util.UIUtils;

import org.lucasr.twowayview.TwoWayView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.fusionx.relay.conversation.Channel;
import co.fusionx.relay.core.ChannelUser;
import co.fusionx.relay.core.Nick;
import co.fusionx.relay.constants.UserLevel;

public class UserListAdapter extends TwoWayView.Adapter<UserListAdapter.UserViewHolder> {

    private final Context mContext;

    private final LayoutInflater mInflater;

    private final Channel mChannel;

    private final List<Pair<Nick, UserLevel>> mUsers;

    private final UserListComparator mComparator;

    private SparseArray<Section> mSections = new SparseArray<>();

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

        updateSections();
    }

    private void updateSections() {
        mSections.clear();

        if (mUsers.isEmpty()) {
            return;
        }
        final UserLevel[] values = UserLevel.values();

        // Position stores the position in the lists
        int position = 0;
        int offset = 0;
        for (final UserLevel level : values) {
            // If we have at least one user of with at a level then we need a header
            if (mUsers.get(position).second == level) {
                // Add the header
                final Section section = new Section(position, level);
                // Account for the previously added headers
                section.sectionedPosition = section.firstPosition + offset;
                mSections.append(section.sectionedPosition, section);
                // Increment the header count for the next header
                ++offset;

                // Consume the users with the same level
                do {
                    position++;
                } while (position < mUsers.size() && mUsers.get(position).second == level);
                if (position >= mUsers.size()) {
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public Pair<Nick, UserLevel> getItem(final int position) {
        return mUsers.get(position);
    }

    @Override
    public UserViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = mInflater.inflate(viewType == 0
                ? R.layout.sliding_menu_header
                : R.layout.default_listview_textview, parent, false);
        final UserViewHolder userViewHolder = new UserViewHolder(view);
        UIUtils.setRobotoLight(mContext, userViewHolder.textView);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {
        if (isSectionHeaderPosition(position)) {
            final Section section = mSections.get(position);
            holder.textView.setText("Prefix: " + section.level.getPrefix());
        } else {
            final Pair<Nick, UserLevel> user = getItem(sectionedPositionToPosition(position));
            final UserLevel level = user.second;
            final char prefix = level == null ? '\0' : level.getPrefix();
            final SpannableStringBuilder builder = new SpannableStringBuilder(
                    prefix + user.first.getNickAsString());
            final ForegroundColorSpan span = new ForegroundColorSpan(
                    NickCache.getNickCache().get(user.first).getColour());
            builder.setSpan(span, 0, builder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            holder.textView.setText(builder);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size() + mSections.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return isSectionHeaderPosition(position) ? 0 : 1;
    }

    public void addUser(final ChannelUser user, final UserLevel level) {
        if (user == null) {
            return;
        }

        final Pair<Nick, UserLevel> pair = new Pair<>(user.getNick(), level);
        final int index = insertPair(pair);
        notifyInsertOrUpdateHeaders(index, pair.second);
    }

    public void removeUser(final ChannelUser user, final UserLevel level) {
        if (user == null) {
            return;
        }

        final Pair<Nick, UserLevel> pair = new Pair<>(user.getNick(), level);
        final int index = removePair(pair);
        notifyRemoveOrUpdateHeaders(index, pair.second);
    }

    public void changeNick(final ChannelUser user, final Nick oldNick, final Nick newNick) {
        if (user == null) {
            return;
        }

        final UserLevel level = user.getChannelPrivileges(mChannel);

        final Pair<Nick, UserLevel> oldPair = new Pair<>(oldNick, level);
        final int removed = removePair(oldPair);
        notifyItemRemoved(positionToSectionedPosition(removed));

        final Pair<Nick, UserLevel> newPair = new Pair<>(newNick, level);
        final int inserted = insertPair(newPair);
        notifyItemInserted(positionToSectionedPosition(inserted));
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
        insertPair(newPair);

        updateSections();
    }

    private int insertPair(final Pair<Nick, UserLevel> pair) {
        final int index = Collections.binarySearch(mUsers, pair, mComparator);
        if (index >= 0) {
            // TODO - this is invalid
            Log.e("HoloIRC", "Invalid");
        } else {
            final int actual = ~index;
            mUsers.add(actual, pair);
            return actual;
        }
        return -1;
    }

    private int removePair(final Pair<Nick, UserLevel> pair) {
        final int index = Collections.binarySearch(mUsers, pair, mComparator);
        if (index < 0) {
            // TODO - this is invalid
            Log.e("HoloIRC", "Invalid");
        } else {
            mUsers.remove(index);
            return index;
        }
        return -1;
    }

    private void notifyInsertOrUpdateHeaders(final int index, final UserLevel level) {
        if (getLastSection().level.ordinal() != level.ordinal()) {
            updateSections();
        } else {
            notifyItemInserted(positionToSectionedPosition(index));
        }
    }

    private void notifyRemoveOrUpdateHeaders(final int index, final UserLevel level) {
        final Section last = getLastSection();
        if (last.level.ordinal() > level.ordinal() || last.firstPosition >= mUsers.size()) {
            updateSections();
        } else {
            notifyItemRemoved(positionToSectionedPosition(index));
        }
    }

    private Section getLastSection() {
        return mSections.valueAt(mSections.size() - 1);
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }

    protected static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public UserViewHolder(final View itemView) {
            super(itemView);

            textView = (TextView) itemView;
        }
    }

    private static class UserListComparator implements Comparator<Pair<Nick, UserLevel>> {

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

    private static class Section {

        int firstPosition;

        int sectionedPosition;

        UserLevel level;

        public Section(int firstPosition, UserLevel level) {
            this.firstPosition = firstPosition;
            this.level = level;
        }
    }
}
/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.ui;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.FragmentType;

import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.Collection;
import java.util.List;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.ChannelUser;
import co.fusionx.relay.base.Nick;
import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.function.FluentIterables;
import co.fusionx.relay.misc.IRCUserComparator;
import co.fusionx.relay.parser.user.UserInputParser;
import co.fusionx.relay.util.IRCUtils;
import co.fusionx.relay.util.Utils;

import static com.fusionx.lightirc.util.UIUtils.findById;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

public final class ChannelFragment extends IRCFragment<ChannelEvent>
        implements PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener, TextWatcher {

    private ImageButton mAutoButton;

    private PopupMenu mPopupMenu;

    private boolean mIsPopupShown;

    public void onMentionMultipleUsers(final List<ChannelUser> users) {
        final String text = String.valueOf(mMessageBox.getText());
        final String total = FluentIterable.from(users)
                .transform(ChannelUser::getNick)
                .transform(Nick::getNickAsString)
                .join(Joiner.on(": ")) + ": " + text;

        mMessageBox.clearComposingText();
        mMessageBox.append(total);
    }

    @Override
    public void onDismiss(final PopupMenu popupMenu) {
        mIsPopupShown = false;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        final String nick = menuItem.getTitle().toString();
        changeLastWord(nick);

        mIsPopupShown = false;
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(final Editable s) {
        mAutoButton.setEnabled(Utils.isNotEmpty(s));
    }

    @Override
    public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflate.inflate(R.layout.fragment_channel, container, false);
    }

    // Subscription methods
    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final ChannelEvent event) {
        final int position = mLayoutManager.findLastCompletelyVisibleItemPosition();
        mMessageAdapter.add(event);
        if (position == mMessageAdapter.getItemCount() - 2) {
            mRecyclerView.scrollToPosition(mMessageAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAutoButton = findById(view, R.id.auto_complete_button);
        mAutoButton.setOnClickListener(new QuickUserMentionListener());

        mAutoButton.setEnabled(Utils.isNotEmpty(mMessageBox.getText()));
        mMessageBox.addTextChangedListener(this);
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseChannelMessage(getChannel(), message);
    }

    @Override
    public boolean isValid() {
        return mConversation.isValid();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.CHANNEL;
    }

    @Override
    protected List<ChannelEvent> getAdapterData() {
        return (List<ChannelEvent>) getChannel().getBuffer();
    }

    private Channel getChannel() {
        return (Channel) mConversation;
    }

    private void changeLastWord(final String newWord) {
        final String message = mMessageBox.getText().toString();
        final List<String> list = IRCUtils.splitRawLine(message, false);
        list.set(list.size() - 1, newWord);
        mMessageBox.setText("");
        mMessageBox.append(IRCUtils.concatenateStringList(list) + ": ");
    }

    private class QuickUserMentionListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            if (mIsPopupShown) {
                mPopupMenu.dismiss();
                return;
            }

            // TODO - this needs to be synchronized properly
            final Collection<? extends ChannelUser> users = getChannel().getUsers();
            final String message = mMessageBox.getText().toString();
            final String finalWord = Iterables
                    .getLast(IRCUtils.splitRawLine(message, false));
            final ImmutableList<? extends ChannelUser> sortedList = FluentIterable.from(users)
                    .filter(user -> startsWithIgnoreCase(user.getNick().getNickAsString(),
                            finalWord))
                    .toSortedList(new IRCUserComparator(getChannel()));

            if (sortedList.size() == 1) {
                changeLastWord(Iterables.getLast(sortedList).getNick().getNickAsString());
            } else if (sortedList.size() > 1) {
                if (mPopupMenu == null) {
                    mPopupMenu = new PopupMenu(getActivity(), mAutoButton);
                    mPopupMenu.setOnDismissListener(ChannelFragment.this);
                    mPopupMenu.setOnMenuItemClickListener(ChannelFragment.this);
                }
                final Menu innerMenu = mPopupMenu.getMenu();
                innerMenu.clear();

                FluentIterables.forEach(FluentIterable.from(sortedList)
                        .transform(ChannelUser::getNick)
                        .transform(Nick::getNickAsString), innerMenu::add);
                mPopupMenu.show();
            }
        }
    }
}
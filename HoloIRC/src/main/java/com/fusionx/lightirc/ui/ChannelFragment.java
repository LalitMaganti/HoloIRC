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

import com.google.common.collect.Iterables;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.EventUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.event.channel.ChannelEvent;
import com.fusionx.relay.misc.IRCUserComparator;
import com.fusionx.relay.parser.UserInputParser;
import com.fusionx.relay.util.IRCUtils;
import com.fusionx.relay.util.Utils;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.fusionx.lightirc.util.UIUtils.findById;

public final class ChannelFragment extends IRCFragment<ChannelEvent> implements PopupMenu
        .OnMenuItemClickListener, PopupMenu.OnDismissListener, TextWatcher {

    private ImageButton mAutoButton;

    private PopupMenu mPopupMenu;

    private boolean mIsPopupShown;

    public void onMentionMultipleUsers(final List<WorldUser> users) {
        final StringBuilder builder = new StringBuilder();
        final String text = String.valueOf(mMessageBox.getText());
        for (final WorldUser userNick : users) {
            builder.append(userNick.getNick()).append(": ");
        }
        builder.append(text);

        mMessageBox.clearComposingText();
        mMessageBox.append(builder.toString());
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

    // Subscription methods
    @Subscribe
    public void onEventMainThread(final ChannelEvent event) {
        if (!event.channelName.equals(mTitle)) {
            return;
        }

        mMessageAdapter.add(event);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAutoButton = findById(view, R.id.auto_complete_button);
        mAutoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsPopupShown) {
                    mPopupMenu.dismiss();
                } else {
                    // TODO - this needs to be synchronized properly
                    final Collection<WorldUser> users = getChannel().getUsers();
                    final List<WorldUser> sortedList = new ArrayList<>(users.size());
                    final String message = mMessageBox.getText().toString();
                    final String finalWord = Iterables
                            .getLast(IRCUtils.splitRawLine(message, false));
                    for (final WorldUser user : users) {
                        if (StringUtils.startsWithIgnoreCase(user.getNick(), finalWord)) {
                            sortedList.add(user);
                        }
                    }

                    if (sortedList.size() == 1) {
                        changeLastWord(Iterables.getLast(sortedList).getNick());
                    } else if (sortedList.size() > 1) {
                        if (mPopupMenu == null) {
                            mPopupMenu = new PopupMenu(getActivity(), mAutoButton);
                            mPopupMenu.setOnDismissListener(ChannelFragment.this);
                            mPopupMenu.setOnMenuItemClickListener(ChannelFragment.this);
                        }
                        final Menu innerMenu = mPopupMenu.getMenu();
                        innerMenu.clear();

                        Collections.sort(sortedList, new IRCUserComparator(getChannel()));
                        for (final WorldUser user : sortedList) {
                            innerMenu.add(user.getNick());
                        }
                        mPopupMenu.show();
                    }
                }
            }
        });

        mAutoButton.setEnabled(Utils.isNotEmpty(mMessageBox.getText()));
        mMessageBox.addTextChangedListener(this);
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseChannelMessage(mConversation.getServer(), mTitle, message);
    }

    @Override
    public FragmentType getType() {
        return FragmentType.CHANNEL;
    }

    @Override
    protected View createView(final ViewGroup container, final LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    @Override
    protected List<ChannelEvent> getAdapterData() {
        return getChannel().getBuffer();
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
}
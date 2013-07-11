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

package com.fusionx.lightirc.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.interfaces.CommonIRCListenerInterface;
import com.fusionx.lightirc.misc.Utils;

import java.util.ArrayList;
import java.util.Set;

public class UserListFragment extends ListFragment implements AbsListView.MultiChoiceModeListener,
        AdapterView.OnItemClickListener {
    private boolean modeStarted = false;

    private UserListListenerInterface mListener;
    private CommonIRCListenerInterface mCommonListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (UserListListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UserListListenerInterface");
        }
        try {
            mCommonListener = (CommonIRCListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CommonIRCListenerInterface");
        }
    }

    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        final UserListAdapter adapter = new UserListAdapter(inflater.getContext(), new ArrayList<String>());
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void userListUpdate(final String channelName) {
        final UserListAdapter adapter = ((UserListAdapter) getListAdapter());
        adapter.clear();

        final ArrayList<String> userList = mListener.getUserList(channelName);
        if (userList != null) {
            adapter.addAll(userList);
            adapter.sort();
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(this);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id, final boolean checked) {
        mode.invalidate();

        final UserListAdapter adapter = (UserListAdapter) getListView().getAdapter();

        if (checked) {
            adapter.addSelection(position);
        } else {
            adapter.removeSelection(position);
        }

        int selectedItemCount = getListView().getCheckedItemCount();

        if (selectedItemCount != 0) {
            final String quantityString = getResources().getQuantityString(R.plurals.user_selection,
                    selectedItemCount, selectedItemCount);

            mode.setTitle(quantityString);

            mode.getMenu().getItem(1).setVisible(selectedItemCount == 1);
        }
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        final Set<String> selectedItems = ((UserListAdapter) getListView().getAdapter()).getSelectedItems();
        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                mListener.onUserMention(selectedItems);
                mode.finish();
                mCommonListener.closeAllSlidingMenus();
                return true;
            case R.id.fragment_userlist_cab_pm:
                final String nick = Utils.stripPrefixFromNick(String
                        .valueOf(Html.fromHtml((String) selectedItems.toArray()[0])));

                if (!mListener.isNickUsers(nick)) {
                    mCommonListener.onCreatePMFragment(nick);
                    mCommonListener.closeAllSlidingMenus();
                    mode.finish();
                } else {
                    final AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
                    build.setTitle(getActivity()
                            .getString(R.string.user_list_not_possible)).setMessage(getActivity()
                            .getString(R.string.user_list_pm_self_not_possible))
                            .setPositiveButton(getActivity()
                                    .getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    build.show();
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        final MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.fragment_userlist_cab, menu);

        modeStarted = true;

        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        final UserListAdapter adapter = (UserListAdapter) getListView().getAdapter();
        adapter.clearSelection();

        modeStarted = false;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        return false;
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        if (!modeStarted) {
            getActivity().startActionMode(this);
        }

        final UserListAdapter adapter = (UserListAdapter) getListView().getAdapter();

        final boolean checked = adapter.getSelectedItems().contains(adapter.getItem(i));
        getListView().setItemChecked(i, !checked);
    }

    public interface UserListListenerInterface {
        public void onUserMention(final Set<String> users);

        public boolean isNickUsers(final String nick);

        public ArrayList<String> getUserList(final String channelName);
    }
}
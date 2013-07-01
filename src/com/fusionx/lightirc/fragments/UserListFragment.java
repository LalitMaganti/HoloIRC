package com.fusionx.lightirc.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.adapters.UserListAdapter;

import java.util.ArrayList;
import java.util.Set;

public class UserListFragment extends ListFragment
        implements AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener {
    private boolean modeStarted;

    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        UserListAdapter adapter = new UserListAdapter(inflater.getContext(),
                new ArrayList<String>());
        setListAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(this);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemCheckedStateChanged(final ActionMode mode, final int position,
                                          final long id, final boolean checked) {
        mode.invalidate();
        if (checked) {
            ((UserListAdapter) getListView().getAdapter()).addSelection(position);
        } else {
            ((UserListAdapter) getListView().getAdapter()).removeSelection(position);
        }
        int selectedItemCount = ((UserListAdapter) getListView().getAdapter()).getSelectedItems().size();

        if (selectedItemCount != 0) {
            final String quantityString = getResources().getQuantityString(R.plurals.user_selectioon,
                    selectedItemCount, selectedItemCount);

            mode.setTitle(quantityString);
        }
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        final Set<String> positions = ((UserListAdapter) getListView().getAdapter()).getSelectedItems();

        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                ((ServerChannelActivity) getActivity()).userListMention(positions);
                mode.finish();
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.fragment_userlist_cab, menu);

        modeStarted = true;

        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        ((UserListAdapter) getListView().getAdapter()).clearSelection();
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
        boolean checked = ((UserListAdapter) getListView().getAdapter()).getSelectedItems()
                .contains(((UserListAdapter) getListView().getAdapter()).getItem(i));
        getListView().setItemChecked(i, !checked);
    }
}
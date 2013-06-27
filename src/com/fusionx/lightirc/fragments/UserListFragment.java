package com.fusionx.lightirc.fragments;

import android.content.res.Resources;
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
    private UserListAdapter adapter;
    private boolean modeStarted;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        adapter = new UserListAdapter(inflater.getContext(),
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
    public void onItemCheckedStateChanged(ActionMode mode, int position,
                                          long id, boolean checked) {
        mode.invalidate();

        final Resources res = getResources();
        final String quantityString = res.getQuantityString(R.plurals.user_selectioon,
                getListView().getCheckedItemCount());

        mode.setTitle(quantityString);
        if (checked) {
            adapter.addSelection(position);
        } else {
            adapter.removeSelection(position);
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final Set<String> positions = adapter.getSelectedItems();

        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                ((ServerChannelActivity) getActivity()).userListMention(positions);
                mode.finish();
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.fragment_userlist_cab, menu);

        modeStarted = true;

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.clearSelection();
        modeStarted = false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!modeStarted) {
            getActivity().startActionMode(this);
        }
        boolean checked = adapter.getSelectedItems().contains(adapter.getItem(i));
        getListView().setItemChecked(i, !checked);
    }
}
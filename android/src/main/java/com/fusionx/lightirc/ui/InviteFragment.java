package com.fusionx.lightirc.ui;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.service.ServiceEventInterceptor;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.lightirc.util.UIUtils;
import co.fusionx.relay.event.server.InviteEvent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collection;
import java.util.Set;

import static com.fusionx.lightirc.util.UIUtils.findById;

public class InviteFragment extends ListFragment implements AbsListView.MultiChoiceModeListener,
        AdapterView.OnItemClickListener {

    private InviteAdapter mInviteAdapter;

    private Callbacks mCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.default_list_view, container, false);
        final ListView listView = findById(view, android.R.id.list);

        final TextView otherHeader = (TextView) inflater.inflate(R.layout.sliding_menu_header,
                null, false);
        otherHeader.setText("Invites");
        listView.addHeaderView(otherHeader);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mInviteAdapter = new InviteAdapter(getActivity(),
                mCallbacks.getEventInterceptor().getInviteEvents());
        setListAdapter(mInviteAdapter);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(this);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
            boolean checked) {
        mode.invalidate();
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        final MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.fragment_invite_cab, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        final int checkedItemCount = getListView().getCheckedItemPositions().size();
        mode.setTitle(checkedItemCount + getActivity().getString(R.string.items_checked));
        mode.getMenu().getItem(0).setVisible(checkedItemCount > 0);

        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_invite_cab_accept:
                mCallbacks.joinMultipleChannels(getInviteEventsFromPositions());
                // Fall through intentional
            case R.id.fragment_invite_cab_clear:
                removeInvitesFromAdapterAndService(getInviteEventsFromPositions());
                mode.finish();
                return true;
        }
        return false;
    }

    private void removeInvitesFromAdapterAndService(final Collection<InviteEvent>
            inviteEventsFromPositions) {
        mCallbacks.getEventInterceptor().getInviteEvents().removeAll(inviteEventsFromPositions);
        mInviteAdapter = new InviteAdapter(getActivity(), mCallbacks.getEventInterceptor()
                .getInviteEvents());
        setListAdapter(mInviteAdapter);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        final boolean checked = getListView().getCheckedItemPositions().get(position);
        getListView().setItemChecked(position, !checked);
    }

    // Index needs to be decreased by one because the header is counted as an item
    private Collection<InviteEvent> getInviteEventsFromPositions() {
        final Collection<Integer> positions = UIUtils.getCheckedPositions(getListView());
        return FluentIterable.from(positions)
                .transform(pos -> mInviteAdapter.getItem(pos - 1))
                .toList();
    }

    public interface Callbacks {

        public void joinMultipleChannels(Collection<InviteEvent> inviteEventsFromPositions);

        public ServiceEventInterceptor getEventInterceptor();
    }

    private static class InviteAdapter extends ArrayAdapter<InviteEvent> {

        private final LayoutInflater mLayoutInflater;

        public InviteAdapter(final Context context, final Set<InviteEvent> objects) {
            super(context, R.layout.default_listview_textview, ImmutableList.copyOf(objects));

            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView view = (TextView) (convertView != null
                    ? convertView : mLayoutInflater.inflate(R.layout.default_listview_textview,
                    parent, false));
            view.setText(getItem(position).channelName);
            return view;
        }
    }
}
package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.lightirc.ui.dialogbuilder.DialogBuilder;
import com.fusionx.lightirc.util.UIUtils;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;

import android.annotation.SuppressLint;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.fusionx.relay.base.Conversation;

import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.UIUtils.findById;

public class IgnoreListFragment extends ListFragment implements OnDismissCallback,
        AbsListView.MultiChoiceModeListener, AbsListView.OnItemClickListener {

    private Conversation mConversation;

    private final Object mEventHandler = new Object() {
        @Subscribe
        public void onConversationChanged(final OnConversationChanged conversationChanged) {
            mConversation = conversationChanged.conversation;
        }
    };

    private BuilderDatabaseSource mDatabaseSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseSource = new BuilderDatabaseSource(getActivity());
        mDatabaseSource.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDatabaseSource.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.default_list_view, container, false);
        final ListView listView = findById(view, android.R.id.list);

        @SuppressLint("InflateParams")
        final TextView otherHeader = (TextView) inflater.inflate(R.layout.sliding_menu_header,
                null, false);
        otherHeader.setText("Ignore List");
        listView.addHeaderView(otherHeader);

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBus().registerSticky(mEventHandler);

        final List<String> arrayList = new ArrayList<>(mDatabaseSource.getIgnoreListByName(
                mConversation.getServer().getTitle()));
        final BaseCollectionAdapter<String> ignoreAdapter = new BaseCollectionAdapter<>
                (getActivity(), R.layout.default_listview_textview, arrayList);
        final DecoratedIgnoreListAdapter listAdapter = new DecoratedIgnoreListAdapter
                (ignoreAdapter, this);

        setListAdapter(listAdapter);
        getListAdapter().setAbsListView(getListView());
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setOnItemClickListener(this);
        getListView().setMultiChoiceModeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getBus().unregister(mEventHandler);
    }

    @Override
    public DecoratedIgnoreListAdapter getListAdapter() {
        return (DecoratedIgnoreListAdapter) super.getListAdapter();
    }

    @Override
    public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
        for (final int position : reverseSortedPositions) {
            getIgnoreAdapter().remove(getIgnoreAdapter().getItem(position - 1));
        }
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
        inflater.inflate(R.menu.fragment_ignore_cab, menu);
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
            case R.id.fragment_ignore_cab_remove:
                getListAdapter().animateDismiss(UIUtils.getCheckedPositions(getListView()));
                mode.finish();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    public void addIgnoredUser() {
        final IgnoreListDialogBuilder builder = new IgnoreListDialogBuilder();
        builder.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        final boolean checked = getListView().getCheckedItemPositions().get(position);
        getListView().setItemChecked(position, !checked);
    }

    public void saveIgnoreList() {
        final List<String> list = getIgnoreAdapter().getListOfItems();
        mDatabaseSource.updateIgnoreList(mConversation.getServer().getTitle(), list);
        mConversation.getServer().getUserChannelInterface().updateIgnoreList(list);
    }

    private BaseCollectionAdapter<String> getIgnoreAdapter() {
        return (BaseCollectionAdapter) getListAdapter().getDecoratedBaseAdapter();
    }

    public class IgnoreListDialogBuilder extends DialogBuilder {

        public IgnoreListDialogBuilder() {
            super(getActivity(), getActivity().getString(R.string.ignore_nick_title),
                    getActivity().getString(R.string.ignore_nick_description), "");
        }

        @Override
        public void onOkClicked(final String input) {
            getIgnoreAdapter().add(input);
        }
    }
}
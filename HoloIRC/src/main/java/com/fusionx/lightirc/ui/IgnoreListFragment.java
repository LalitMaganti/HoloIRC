package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.BaseCollectionAdapter;
import com.fusionx.lightirc.adapters.DecoratedIgnoreListAdapter;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.lightirc.ui.dialogbuilder.DialogBuilder;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.interfaces.Conversation;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;

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

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class IgnoreListFragment extends ListFragment implements OnDismissCallback,
        AbsListView.MultiChoiceModeListener, AbsListView.OnItemClickListener {

    private Conversation mConversation;

    private final Object mEventHandler = new Object() {
        @SuppressWarnings("unused")
        public void onEvent(final OnConversationChanged conversationChanged) {
            mConversation = conversationChanged.conversation;
        }
    };

    private ActionMode mActionMode;

    private BuilderDatabaseSource mDatabaseSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseSource = new BuilderDatabaseSource(getActivity().getApplicationContext());
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
        return inflater.inflate(R.layout.default_list_view, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventBus.getDefault().registerSticky(mEventHandler);

        final List<String> arrayList = new ArrayList<>(mDatabaseSource.getIgnoreListByName(
                mConversation.getServer().getTitle()));
        final BaseCollectionAdapter<String> ignoreAdapter = new BaseCollectionAdapter<>
                (getActivity(), R.layout.default_listview_textview, arrayList);
        final DecoratedIgnoreListAdapter listAdapter = new DecoratedIgnoreListAdapter
                (ignoreAdapter, this);

        setListAdapter(listAdapter);
        getListAdapter().setAbsListView(getListView());
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        getListView().setOnItemClickListener(this);

        getListView().startActionMode(this);
    }

    @Override
    public DecoratedIgnoreListAdapter getListAdapter() {
        return (DecoratedIgnoreListAdapter) super.getListAdapter();
    }

    @Override
    public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
        for (final int position : reverseSortedPositions) {
            getIgnoreAdapter().remove(getIgnoreAdapter().getItem(position));
        }
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
            boolean checked) {
        final int checkedItemCount = getListView().getCheckedItemPositions().size();

        if (checkedItemCount != 0) {
            mode.setTitle(checkedItemCount + getActivity().getString(R.string.items_checked));
        } else {
            mode.setTitle(getActivity().getString(R.string.ignore_list));
        }
        mode.getMenu().getItem(0).setVisible(checkedItemCount == 0);
        mode.getMenu().getItem(1).setVisible(checkedItemCount > 0);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        mActionMode = actionMode;

        // Inflate a menu resource providing context menu items
        final MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.ignore_list_cab, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle(getActivity().getString(R.string.ignore_list));
        mode.getMenu().getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ignore_list_cab_add:
                final IgnoreListDialogBuilder builder = new IgnoreListDialogBuilder();
                builder.show();
                return true;
            case R.id.ignore_list_cab_remove:
                getListAdapter().animateDismiss(UIUtils.getCheckedPositions(getListView()));
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;

        final IgnoreListCallback callback = FragmentUtils.getParent(IgnoreListFragment.this,
                IgnoreListCallback.class);
        final List<String> list = getIgnoreAdapter().getListOfItems();
        mDatabaseSource.updateIgnoreList(mConversation.getServer().getTitle(), list);

        callback.switchToIRCActionFragment();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        final boolean checked = getListView().getCheckedItemPositions().get(position);
        getListView().setItemChecked(position, !checked);
    }

    public void finishActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private BaseCollectionAdapter<String> getIgnoreAdapter() {
        return (BaseCollectionAdapter) getListAdapter().getDecoratedBaseAdapter();
    }

    public interface IgnoreListCallback {

        public void switchToIRCActionFragment();
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
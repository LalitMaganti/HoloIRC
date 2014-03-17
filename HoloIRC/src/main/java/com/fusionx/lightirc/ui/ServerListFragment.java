package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ExpandableServerListAdapter;
import com.fusionx.lightirc.communication.NewIRCService;
import com.fusionx.lightirc.loader.ServiceLoader;
import com.fusionx.lightirc.model.WrappedServerListItem;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.lightirc.util.MultiSelectionUtils;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.DisconnectEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.interfaces.SubServerObject;
import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gnu.trove.set.hash.THashSet;

import static butterknife.ButterKnife.findById;

public class ServerListFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<NewIRCService>, ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnChildClickListener, ExpandableServerListAdapter.Callback {

    private final THashSet<ServerEventHandler> mEventHandlers = new THashSet<>();

    private int mLastGroup = -1;

    private NewIRCService mService;

    private Callback mCallback;

    private ExpandableListView mListView;

    private ExpandableServerListAdapter mListAdapter;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallback = (Callback) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (final ServerEventHandler handler : mEventHandlers) {
            handler.unregister();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.expandable_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRetainInstance(true);

        mListView = findById(view, R.id.server_list);
        mListView.setGroupIndicator(null);

        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            private Handler mHandler;

            private Dialog mDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mHandler = new Handler();
                mDialog = new Dialog(getActivity());
            }

            @Override
            protected Void doInBackground(Void... params) {
                final List<File> fileList = SharedPreferencesUtils.getOldServers(getActivity());
                if (!fileList.isEmpty()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.setCancelable(false);
                            mDialog.setCanceledOnTouchOutside(false);
                            mDialog.setTitle("Please wait...");
                            mDialog.show();
                        }
                    });
                    SharedPreferencesUtils.migrateToDatabase(fileList, getActivity());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.cancel();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // This can happen when rotation has occurred - the view has been redrawn but the
                // comms layer should be constant
                if (mService != null) {
                    mListAdapter.setListView(mListView);

                    mListView.setAdapter(mListAdapter);
                    mListView.setOnGroupClickListener(ServerListFragment.this);
                    mListView.setOnChildClickListener(ServerListFragment.this);
                } else {
                    getLoaderManager().initLoader(1, null, ServerListFragment.this);
                }
            }
        };
        asyncTask.execute();

        mListener.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListener.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListener.onSaveInstanceState(outState);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        mListener.setMenuVisibility(menuVisible);
    }

    @Override
    public Loader<NewIRCService> onCreateLoader(final int i, final Bundle bundle) {
        return new ServiceLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<NewIRCService> ircBinderLoader,
            final NewIRCService service) {
        mService = service;
        refreshServers();

        mListView.setOnGroupClickListener(this);
        mListView.setOnChildClickListener(this);
    }

    void refreshServers() {
        final List<WrappedServerListItem> listItems = new ArrayList<>();
        final BuilderDatabaseSource source = new BuilderDatabaseSource(getActivity());

        source.open();
        for (final ServerConfiguration.Builder builder : source.getAllBuilders()) {
            listItems.add(new WrappedServerListItem(builder, mService.getServerIfExists(builder)));
        }
        source.close();

        mListAdapter = new ExpandableServerListAdapter(getActivity(), listItems, mListView, this);
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public void onLoaderReset(final Loader<NewIRCService> listLoader) {
    }

    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
            final int groupPosition, final long id) {
        if (mListener.mMultiSelectionController.isActionModeStarted()) {
            mListener.mMultiSelectionController
                    .onItemClick(parent, v, mListView.getFlatListPosition(ExpandableListView
                            .getPackedPositionForGroup(groupPosition)), id);
            return true;
        }

        mLastGroup = groupPosition;

        final WrappedServerListItem item = mListAdapter.getGroup(groupPosition);
        if (item.getServer() == null) {
            item.setServer(mService.connectToServer(item.getBuilder()));
            mEventHandlers.add(new ServerEventHandler(item.getServer(), groupPosition));
        }
        mCallback.onServerClicked(item.getServer());
        return true;
    }

    @Override
    public boolean onChildClick(final ExpandableListView parent, final View v,
            final int groupPosition, final int childPosition, final long id) {
        if (mListener.mMultiSelectionController.isActionModeStarted()) {
            mListener.mMultiSelectionController
                    .onItemClick(parent, v, mListView.getFlatListPosition(ExpandableListView
                            .getPackedPositionForChild(groupPosition, childPosition)), id);
            return true;
        }

        mLastGroup = groupPosition;
        mCallback.onSubServerClicked(mListAdapter.getChild(groupPosition, childPosition));

        return true;
    }

    public Server onActivityRestored() {
        if (mLastGroup != -1) {
            return mListAdapter.getGroup(mLastGroup).getServer();
        }
        return null;
    }

    @Override
    public void onEditServer(final WrappedServerListItem builder) {
        final Intent intent = new Intent(getActivity(), ServerPreferenceActivity.class);
        intent.putExtra(ServerPreferenceActivity.NEW_SERVER, false);
        intent.putExtra(ServerPreferenceActivity.SERVER, builder.getBuilder());
        startActivity(intent);
    }

    public interface Callback {

        public void onServerClicked(final Server server);

        public void onSubServerClicked(final SubServerObject object);

        public void onServerDisconnected(final Server server);
    }

    public class ServerEventHandler {

        private final int mServerIndex;

        private final Server mServer;

        public ServerEventHandler(final Server server, final int serverIndex) {
            mServer = server;
            mServerIndex = serverIndex;

            server.getServerEventBus().register(this);
        }

        @Subscribe
        public void onJoin(final JoinEvent event) throws InterruptedException {
            final Channel channel = mServer.getUserChannelInterface().getChannel(event
                    .channelName);
            mListAdapter.getGroup(mServerIndex).getServerObjects().add(channel);
            mListView.setAdapter(mListAdapter);
            mListView.expandGroup(mServerIndex);
        }

        @Subscribe
        public void onConnect(final ConnectEvent event) {
            mListView.invalidateViews();
        }

        @Subscribe
        public void onDisconnect(final DisconnectEvent event) {
            mLastGroup = -1;

            unregister();
            mCallback.onServerDisconnected(mServer);
        }

        public void unregister() {
            mServer.getServerEventBus().unregister(this);
        }
    }

    private final MultiChoiceFragmentListener mListener = new MultiChoiceFragmentListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater()
                    .inflate(R.menu.activity_server_list_popup, menu);
            return true;
        }

        @Override
        protected void attachSelectionController() {
            mMultiSelectionController = MultiSelectionUtils
                    .attachMultiSelectionController(mListView,
                            (ActionBarActivity) getActivity(), mListener, false);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.activity_server_list_popup_disconnect:
                    return false;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
        }

        @Override
        protected ListAdapter getRealAdapter() {
            return mListView.getAdapter();
        }

        @Override
        protected SparseBooleanArray getCheckedItemPositions() {
            return mListView.getCheckedItemPositions();
        }
    };
}
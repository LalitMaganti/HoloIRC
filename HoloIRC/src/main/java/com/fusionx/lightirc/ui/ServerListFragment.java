package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ExpandableServerListAdapter;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.loader.ServerWrapperLoader;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.model.ServerWrapper;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.util.EventUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.Conversation;
import com.fusionx.relay.QueryUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.channel.ChannelEvent;
import com.fusionx.relay.event.query.QueryEvent;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.DisconnectEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.KickEvent;
import com.fusionx.relay.event.server.NewPrivateMessageEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.server.PrivateMessageClosedEvent;
import com.fusionx.relay.event.server.StopEvent;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.set.hash.THashSet;

import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.UIUtils.findById;
import static com.fusionx.lightirc.util.UIUtils.getCheckedPositions;

public class ServerListFragment extends Fragment implements ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnChildClickListener, AbsListView.MultiChoiceModeListener {

    private final THashSet<ServerEventHandler> mEventHandlers = new THashSet<>();

    private final EventHandler mEventHandler = new EventHandler();

    // Callbacks
    private Callback mCallback;

    // IRC
    private IRCService mService;

    // Expandable ListView
    private ExpandableListView mListView;

    private ExpandableServerListAdapter mListAdapter;

    // Action mode
    private ActionMode mActionMode;

    private int mSelectionType = ExpandableListView.PACKED_POSITION_TYPE_NULL;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallback = (Callback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBus().register(mEventHandler);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.expandable_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle bundle) {
        super.onViewCreated(view, bundle);

        mListView = findById(view, R.id.server_list);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setEmptyView(findById(view, android.R.id.empty));

        mListView.setMultiChoiceModeListener(this);

        mListView.setOnGroupClickListener(this);
        mListView.setOnChildClickListener(this);

        if (bundle == null) {
            return;
        }
        // If we are restoring a savedInstanceSate then a ServiceConnection is already present -
        // ask for that to be returned
        final IRCService service = mCallback.getService();
        // The service could be null if the rotation was done so quickly after start that a
        // connection to the service has not been established - in that case our callback is
        // still to come
        if (service != null) {
            onServiceConnected(service);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        for (final ServerEventHandler handler : mEventHandlers) {
            handler.register();
        }
        if (mListAdapter != null) {
            mListAdapter.checkAndRemoveInvalidConversations();
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        for (final ServerEventHandler handler : mEventHandlers) {
            handler.unregister();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getBus().unregister(mEventHandler);
    }

    public void refreshServers() {
        refreshServers(null);
    }

    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
            final int groupPosition, final long id) {
        return onServerClick(groupPosition);
    }

    public void onPanelClosed() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public boolean onChildClick(final ExpandableListView parent, final View v,
            final int groupPosition, final int childPosition, final long id) {
        if (mActionMode != null && mSelectionType == ExpandableListView
                .PACKED_POSITION_TYPE_CHILD) {
            int flatPosition = parent.getFlatListPosition(ExpandableListView
                    .getPackedPositionForChild(groupPosition, childPosition));
            parent.setItemChecked(flatPosition, !parent.isItemChecked(flatPosition));
            return true;
        } else if (mActionMode != null) {
            return true;
        }

        final Conversation conversation = mListAdapter.getChild(groupPosition, childPosition);
        mCallback.onSubServerClicked(conversation);

        return true;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (!checked) {
            mListView.getCheckedItemPositions().delete(position);
        }
        final int count = mListView.getCheckedItemCount();
        final boolean singleItemChecked = count == 1;

        if (checked && singleItemChecked) {
            mSelectionType = ExpandableListView.getPackedPositionType(mListView
                    .getExpandableListPosition(position));
        }

        mode.invalidate();
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mode.getMenuInflater().inflate(R.menu.activity_server_list_popup, menu);
        mActionMode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        if (mSelectionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            final int count = mListView.getCheckedItemCount();
            final boolean connected = count > 0 && getFirstCheckedItem().isServerAvailable();

            boolean allConnected = connected;
            boolean deleteEnabled = !connected;
            for (int i = 1; i < count && (deleteEnabled || allConnected); i++) {
                final int pos = mListView.getCheckedItemPositions().keyAt(i);
                final ServerWrapper listItem = (ServerWrapper) mListView
                        .getItemAtPosition(pos);
                final boolean available = listItem.isServerAvailable();
                allConnected = allConnected && available;
                deleteEnabled = deleteEnabled && !available;
            }
            menu.findItem(R.id.activity_server_list_popup_disconnect).setVisible(allConnected);
            menu.findItem(R.id.activity_server_list_popup_delete).setVisible(deleteEnabled);

            final boolean singleItemChecked = count == 1;
            menu.findItem(R.id.activity_server_list_popup_edit).setVisible(singleItemChecked &&
                    !connected);
        } else {
            menu.findItem(R.id.activity_server_list_popup_edit).setVisible(false);
            menu.findItem(R.id.activity_server_list_popup_delete).setVisible(false);
            menu.findItem(R.id.activity_server_list_popup_disconnect).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final ServerWrapper listItem = getFirstCheckedItem();

        switch (item.getItemId()) {
            case R.id.activity_server_list_popup_disconnect:
                disconnectFromServer(getCheckedPositions(mListView));
                break;
            case R.id.activity_server_list_popup_delete:
                deleteServer(getCheckedPositions(mListView));
                break;
            case R.id.activity_server_list_popup_edit:
                editServer(listItem);
                break;
        }

        mode.finish();
        return true;
    }

    public void onServiceConnected(final IRCService service) {
        mService = service;

        refreshServers();
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        mActionMode = null;
    }

    void editServer(final ServerWrapper builder) {
        final Intent intent = new Intent(getActivity(), ServerPreferenceActivity.class);
        intent.putExtra(ServerPreferenceActivity.NEW_SERVER, false);
        intent.putExtra(ServerPreferenceActivity.SERVER, builder.getBuilder());
        getActivity().startActivityForResult(intent, MainActivity.SERVER_SETTINGS);
    }

    private void disconnectFromServer(final List<Integer> checkedPositions) {
        for (final Integer checkedPosition : checkedPositions) {
            mService.requestConnectionStoppage(mListAdapter.getGroup(checkedPosition).getServer());
        }
    }

    private boolean onServerClick(final int groupPosition) {
        if (mActionMode != null && mSelectionType == ExpandableListView
                .PACKED_POSITION_TYPE_GROUP) {
            int flatPosition = mListView.getFlatListPosition(ExpandableListView
                    .getPackedPositionForGroup(groupPosition));
            mListView.setItemChecked(flatPosition, !mListView.isItemChecked(flatPosition));
            return true;
        } else if (mActionMode != null) {
            return true;
        }

        final ServerWrapper item = mListAdapter.getGroup(groupPosition);
        if (item.getServer() == null) {
            item.setServer(mService.requestConnectionToServer(item.getBuilder(),
                    item.getIgnoreList()));
            mEventHandlers.add(new ServerEventHandler(item.getServer(), groupPosition));
        }
        mCallback.onServerClicked(item.getServer());

        return true;
    }

    private void deleteServer(final List<Integer> checkedPositions) {
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            private BuilderDatabaseSource source;

            @Override
            protected void onPreExecute() {
                source = new BuilderDatabaseSource(getActivity());
                source.open();
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (final int group : checkedPositions) {
                    final ServerWrapper listItem = mListAdapter.getGroup(group);
                    source.removeServer(listItem.getBuilder().getId());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                source.close();
                refreshServers();
            }
        };
        asyncTask.execute();
    }

    private ServerWrapper getFirstCheckedItem() {
        if (mListView.getCheckedItemCount() == 0) {
            return null;
        }

        final int position = mListView.getCheckedItemPositions().keyAt(0);
        return (ServerWrapper) mListView.getItemAtPosition(position);
    }

    private void refreshServers(final Runnable runnable) {
        final LoaderManager.LoaderCallbacks<ArrayList<ServerWrapper>> callbacks = new LoaderManager
                .LoaderCallbacks<ArrayList<ServerWrapper>>() {
            @Override
            public Loader<ArrayList<ServerWrapper>> onCreateLoader(int id, Bundle args) {
                return new ServerWrapperLoader(getActivity(), mService);
            }

            @Override
            public void onLoadFinished(final Loader<ArrayList<ServerWrapper>> loader,
                    final ArrayList<ServerWrapper> listItems) {
                mListAdapter = new ExpandableServerListAdapter(getActivity(), listItems,
                        mListView, mService);
                mListView.setAdapter(mListAdapter);

                final TextView textView = findById(getView(), R.id.empty_text_view);
                textView.setText("No servers found :(\nClick + to add one");

                findById(getView(), R.id.progress_bar_empty).setVisibility(View.GONE);

                for (final ServerEventHandler handler : mEventHandlers) {
                    handler.unregister();
                }
                for (int i = 0, listItemsSize = listItems.size(); i < listItemsSize; i++) {
                    ServerWrapper wrapper = listItems.get(i);
                    if (wrapper.getServer() != null) {
                        mEventHandlers.add(new ServerEventHandler(wrapper.getServer(), i));
                    }
                    // Expand all the groups - TODO - fix this properly
                    mListView.expandGroup(i);
                }

                // Run any code that is meant to be run after the new servers are in place
                if (runnable != null) {
                    final Handler handler = new Handler();
                    handler.post(runnable);
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<ServerWrapper>> loader) {
            }
        };

        getLoaderManager().restartLoader(21, null, callbacks);
    }

    public interface Callback {

        public void onServerClicked(final Server server);

        public void onSubServerClicked(final Conversation object);

        public void onServerStopCompleted(final Server server);

        public IRCService getService();

        public void onPart(final String serverName, final PartEvent event);

        public boolean onKick(final String serverName, final KickEvent event);

        public void onPrivateMessageClosed();
    }

    public class ServerEventHandler {

        private final int mServerIndex;

        private final Server mServer;

        public ServerEventHandler(final Server server, final int serverIndex) {
            mServer = server;
            mServerIndex = serverIndex;

            server.getServerEventBus().register(this, 50);
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final NewPrivateMessageEvent event)
                throws InterruptedException {
            final QueryUser user = event.user;
            mListAdapter.getGroup(mServerIndex).addServerObject(user);
            mListAdapter.notifyDataSetChanged();
            mListView.expandGroup(mServerIndex);
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final JoinEvent event) throws InterruptedException {
            final Channel channel = event.channel;
            mListAdapter.getGroup(mServerIndex).addServerObject(channel);
            mListAdapter.notifyDataSetChanged();
            mListView.expandGroup(mServerIndex);
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final PartEvent event) throws InterruptedException {
            mListAdapter.getGroup(mServerIndex).removeServerObject(event.channelName);
            mListView.setAdapter(mListAdapter);
            mListView.expandGroup(mServerIndex);
            mCallback.onPart(mServer.getTitle(), event);
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final KickEvent event) throws InterruptedException {
            mListAdapter.getGroup(mServerIndex).removeServerObject(event.channelName);
            mListView.setAdapter(mListAdapter);
            mListView.expandGroup(mServerIndex);

            final boolean switchToServer = mCallback.onKick(mServer.getTitle(), event);
            if (switchToServer) {
                onServerClick(mServerIndex);
            }
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final ChannelEvent event) {
            if (EventUtils.shouldStoreEvent(event)
                    && mServer.getStatus() != ConnectionStatus.DISCONNECTED) {
                mListView.invalidateViews();
            }
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final PrivateMessageClosedEvent event) {
            mListAdapter.getGroup(mServerIndex).removeServerObject(
                    event.privateMessageNick.getNickAsString());
            mListView.setAdapter(mListAdapter);
            mListView.expandGroup(mServerIndex);
            mCallback.onPrivateMessageClosed();
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final QueryEvent event) {
            if (mServer.getStatus() != ConnectionStatus.DISCONNECTED) {
                mListView.invalidateViews();
            }
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final ConnectEvent event) {
            mListView.invalidateViews();
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final DisconnectEvent event) {
            mListView.invalidateViews();
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final StopEvent event) {
            refreshServers(() -> {
                unregister();
                mEventHandlers.remove(ServerEventHandler.this);
                mCallback.onServerStopCompleted(mServer);
            });
        }

        public void register() {
            mServer.getServerEventBus().register(this);
        }

        public void unregister() {
            mServer.getServerEventBus().unregister(this);
        }
    }

    private class EventHandler {

        @Subscribe
        public void onConversationChanged(final OnConversationChanged event) {
            if (event.conversation != null) {
                if (event.fragmentType == FragmentType.SERVER) {
                    mService.getEventHelper(event.conversation.getServer()).clearMessagePriority();
                } else {
                    mService.getEventHelper(event.conversation.getServer()).clearMessagePriority
                            (event.conversation);
                }
            }
            mListView.invalidateViews();
        }

        // Make sure the events look up to date if the full line highlight pref is changed
        @Subscribe
        public void onPreferencesChanged(final OnPreferencesChangedEvent event) {
            mListView.invalidateViews();
        }
    }
}

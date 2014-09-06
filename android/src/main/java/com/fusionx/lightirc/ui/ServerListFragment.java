package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.event.ServerStopRequestedEvent;
import com.fusionx.lightirc.loader.ServerWrapperLoader;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.model.ServerConversationContainer;
import com.fusionx.lightirc.model.db.ServerDatabase;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.service.ServiceEventInterceptor;
import com.fusionx.lightirc.util.EventUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.QueryUser;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.dcc.event.chat.DCCChatEvent;
import co.fusionx.relay.dcc.event.chat.DCCChatStartedEvent;
import co.fusionx.relay.dcc.event.file.DCCFileConversationStartedEvent;
import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.event.channel.PartEvent;
import co.fusionx.relay.event.query.QueryClosedEvent;
import co.fusionx.relay.event.query.QueryEvent;
import co.fusionx.relay.event.server.ConnectEvent;
import co.fusionx.relay.event.server.DisconnectEvent;
import co.fusionx.relay.event.server.JoinEvent;
import co.fusionx.relay.event.server.KickEvent;
import co.fusionx.relay.event.server.NewPrivateMessageEvent;

import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.UIUtils.getCheckedPositions;

public class ServerListFragment extends Fragment implements ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnChildClickListener, AbsListView.MultiChoiceModeListener {

    private final EventHandler mEventHandler = new EventHandler();

    private final Map<Server, ServerEventHandler> mEventHandlers = new HashMap<>();

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

        mListView = (ExpandableListView) view.findViewById(R.id.server_list);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setEmptyView((View) view.findViewById(android.R.id.empty));

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

        for (final ServerEventHandler handler : mEventHandlers.values()) {
            handler.register();
        }
        if (mListAdapter == null) {
            return;
        }
        mListAdapter.refreshConversations();
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

        for (final ServerEventHandler handler : mEventHandlers.values()) {
            handler.unregister();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getBus().unregister(mEventHandler);
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
                final ServerConversationContainer listItem = (ServerConversationContainer) mListView
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
        final ServerConversationContainer listItem = getFirstCheckedItem();

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

    void editServer(final ServerConversationContainer builder) {
        final Intent intent = new Intent(getActivity(), ServerPreferenceActivity.class);
        intent.putExtra(ServerPreferenceActivity.NEW_SERVER, false);
        intent.putExtra(ServerPreferenceActivity.SERVER, builder.getBuilder());
        getActivity().startActivityForResult(intent, MainActivity.SERVER_SETTINGS);
    }

    private void disconnectFromServer(final List<Integer> checkedPositions) {
        for (final Integer checkedPosition : checkedPositions) {
            final long packed = mListView.getExpandableListPosition(checkedPosition);
            final int group = ExpandableListView.getPackedPositionGroup(packed);
            mService.requestConnectionStoppage(mListAdapter.getGroup(group).getServer());
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

        final ServerConversationContainer item = mListAdapter.getGroup(groupPosition);
        if (item.getServer() == null) {
            item.setServer(mService.requestConnectionToServer(item.getBuilder()));
            mEventHandlers.put(item.getServer(), new ServerEventHandler(item, groupPosition));
        }
        mCallback.onServerClicked(item.getServer());

        return true;
    }

    private void deleteServer(final List<Integer> checkedPositions) {
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            private ServerDatabase source;

            @Override
            protected void onPreExecute() {
                source = ServerDatabase.getInstance(getActivity());
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (final int checkedPosition : checkedPositions) {
                    final long packed = mListView.getExpandableListPosition(checkedPosition);
                    final int group = ExpandableListView.getPackedPositionGroup(packed);

                    final ServerConversationContainer listItem = mListAdapter.getGroup(group);
                    source.removeServer(listItem.getBuilder().getId());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                refreshServers();
            }
        };
        asyncTask.execute();
    }

    private ServerConversationContainer getFirstCheckedItem() {
        if (mListView.getCheckedItemCount() == 0) {
            return null;
        }

        final int position = mListView.getCheckedItemPositions().keyAt(0);
        return (ServerConversationContainer) mListView.getItemAtPosition(position);
    }

    void refreshServers() {
        final LoaderManager.LoaderCallbacks<ArrayList<ServerConversationContainer>> callbacks
                = new LoaderManager
                .LoaderCallbacks<ArrayList<ServerConversationContainer>>() {
            @Override
            public Loader<ArrayList<ServerConversationContainer>> onCreateLoader(int id,
                    Bundle args) {
                return new ServerWrapperLoader(getActivity(), mService);
            }

            @Override
            public void onLoadFinished(final Loader<ArrayList<ServerConversationContainer>> loader,
                    final ArrayList<ServerConversationContainer> listItems) {
                mListAdapter = new ExpandableServerListAdapter(getActivity(), listItems,
                        mListView, mService);
                mListView.setAdapter(mListAdapter);

                final TextView textView = (TextView) getView().findViewById(R.id.empty_text_view);
                textView.setText("No servers found :(\nClick + to add one");

                ((View) getView().findViewById(R.id.progress_bar_empty)).setVisibility(View.GONE);

                for (final ServerEventHandler eventHandler : mEventHandlers.values()) {
                    eventHandler.unregister();
                }
                mEventHandlers.clear();

                for (int i = 0, listItemsSize = listItems.size(); i < listItemsSize; i++) {
                    ServerConversationContainer wrapper = listItems.get(i);
                    if (wrapper.getServer() != null) {
                        mEventHandlers.put(wrapper.getServer(), new ServerEventHandler(wrapper, i));
                    }
                    // Expand all the groups - TODO - fix this properly
                    mListView.expandGroup(i);
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<ServerConversationContainer>> loader) {
            }
        };

        getLoaderManager().restartLoader(21, null, callbacks);
    }

    public interface Callback {

        public void onServerClicked(final Server server);

        public void onSubServerClicked(final Conversation object);

        public void onServerStopped(final Server server);

        public IRCService getService();

        public void onPart(final String serverName, final PartEvent event);

        public boolean onKick(final Server server, final KickEvent event);

        public void onPrivateMessageClosed(final QueryUser queryUser);
    }

    public class ServerEventHandler {

        private final int mServerIndex;

        private final Server mServer;

        private final ServerConversationContainer mServerConversationContainer;

        public ServerEventHandler(final ServerConversationContainer wrapper,
                final int serverIndex) {
            mServer = wrapper.getServer();
            mServerIndex = serverIndex;
            mServerConversationContainer = wrapper;

            register();
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final NewPrivateMessageEvent event)
                throws InterruptedException {
            mServerConversationContainer.addConversation(event.user);
            mListView.setAdapter(mListAdapter);

            mListView.expandGroup(mServerIndex);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final JoinEvent event) throws InterruptedException {
            mServerConversationContainer.addConversation(event.channel);
            mListView.setAdapter(mListAdapter);

            mListView.expandGroup(mServerIndex);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final PartEvent event) throws InterruptedException {
            mServerConversationContainer.removeConversation(event.channel);
            mListView.setAdapter(mListAdapter);

            mListView.expandGroup(mServerIndex);
            mCallback.onPart(mServer.getTitle(), event);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final KickEvent event) throws InterruptedException {
            mServerConversationContainer.removeConversation(event.channel);
            mListView.setAdapter(mListAdapter);

            mListView.expandGroup(mServerIndex);
            final boolean switchToServer = mCallback.onKick(mServer, event);
            if (switchToServer) {
                onServerClick(mServerIndex);
            }
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final ChannelEvent event) {
            if (EventUtils.shouldStoreEvent(event)
                    && mServer.getStatus() != ConnectionStatus.DISCONNECTED) {
                mListView.invalidateViews();
            }
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final QueryClosedEvent event) {
            mServerConversationContainer.removeConversation(event.user);
            mListView.setAdapter(mListAdapter);

            mListView.expandGroup(mServerIndex);
            mCallback.onPrivateMessageClosed(event.user);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final QueryEvent event) {
            if (mServer.getStatus() != ConnectionStatus.DISCONNECTED) {
                mListView.invalidateViews();
            }
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final ConnectEvent event) {
            mListView.invalidateViews();
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final DisconnectEvent event) {
            mListView.invalidateViews();
        }

        // DCC Events
        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final DCCChatStartedEvent event) {
            mServerConversationContainer.addConversation(event.chatConversation);
            mListView.setAdapter(mListAdapter);

            mListView.expandGroup(mServerIndex);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final DCCChatEvent event) {
            if (EventUtils.shouldStoreEvent(event)
                    && mServer.getStatus() != ConnectionStatus.DISCONNECTED) {
                mListView.invalidateViews();
            }
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final DCCFileConversationStartedEvent event) {
            mServerConversationContainer.addConversation(event.fileConversation);
            mListView.setAdapter(mListAdapter);

            mListView.expandGroup(mServerIndex);
        }

        public void register() {
            mServer.getServerWideBus().register(this, 50);
        }

        public void unregister() {
            mServer.getServerWideBus().unregister(this);
        }
    }

    private class EventHandler {

        @Subscribe
        public void onConversationChanged(final OnConversationChanged event) {
            if (event.conversation != null) {
                final ServiceEventInterceptor eventInterceptor =
                        mService.getEventHelper(event.conversation.getServer());
                if (event.fragmentType == FragmentType.SERVER) {
                    eventInterceptor.clearMessagePriority();
                } else {
                    eventInterceptor.clearMessagePriority(event.conversation);
                }
            }
            mListView.invalidateViews();
        }

        // Make sure the events look up to date if the full line highlight pref is changed
        @Subscribe
        public void onPreferencesChanged(final OnPreferencesChangedEvent event) {
            mListView.invalidateViews();
        }

        @Subscribe
        public void onStopEvent(final ServerStopRequestedEvent event) {
            mListAdapter.removeServer(event.server);
            mCallback.onServerStopped(event.server);

            final ServerEventHandler eventHandler = mEventHandlers.remove(event.server);
            eventHandler.unregister();
        }
    }
}

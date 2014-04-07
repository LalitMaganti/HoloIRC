package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ExpandableServerListAdapter;
import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.loader.ServerWrapperLoader;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.model.ServerWrapper;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.relay.Channel;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.NewPrivateMessage;
import com.fusionx.relay.event.channel.ChannelEvent;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.DisconnectEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.server.ServerEvent;
import com.fusionx.relay.event.user.UserEvent;
import com.fusionx.relay.interfaces.Conversation;

import android.app.Activity;
import android.content.Intent;
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

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import gnu.trove.set.hash.THashSet;

import static com.fusionx.lightirc.util.UIUtils.findById;

public class ServerListFragment extends Fragment implements ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnChildClickListener, AbsListView.MultiChoiceModeListener {

    private final THashSet<ServerEventHandler> mEventHandlers = new THashSet<>();

    private final Object mEventHandler = new Object() {
        @SuppressWarnings("unused")
        public void onEventMainThread(final OnConversationChanged event) {
            if (event.conversation != null) {
                if (event.fragmentType == FragmentType.SERVER) {
                    mService.getEventHelper(event.conversation.getId()).clearMessagePriority();
                } else {
                    mService.getEventHelper(event.conversation.getServer().getId())
                            .clearMessagePriority(event.conversation);
                }
                mListView.invalidateViews();
            }
        }
    };

    // Callbacks
    private Callback mCallback;

    // IRC
    private IRCService mService;

    // Expandable ListView
    private ExpandableListView mListView;

    private ExpandableServerListAdapter mListAdapter;

    // Action mode
    private boolean mActionModeStarted;

    private int mSelectionType = ExpandableListView.PACKED_POSITION_TYPE_NULL;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallback = (Callback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(mEventHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(mEventHandler);

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
    public void onViewCreated(final View view, final Bundle bundle) {
        super.onViewCreated(view, bundle);

        mListView = findById(view, R.id.server_list);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        mListView.setMultiChoiceModeListener(this);

        mListView.setOnGroupClickListener(this);
        mListView.setOnChildClickListener(this);

        // If we are restoring a savedInstanceSate then a ServiceConnection is already present -
        // ask for that to be returned
        if (bundle != null) {
            final IRCService service = mCallback.getService();
            // The service could be null if the rotation was done so quickly after start that a
            // connection to the service has not been established - in that case our callback is
            // still to come
            if (service != null) {
                onServiceConnected(service);
            }
        }
    }

    public void refreshServers() {
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
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<ServerWrapper>> loader) {
            }
        };

        getLoaderManager().restartLoader(21, null, callbacks);
    }

    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
            final int groupPosition, final long id) {
        if (mActionModeStarted && mSelectionType == ExpandableListView
                .PACKED_POSITION_TYPE_GROUP) {
            int flatPosition = parent.getFlatListPosition(ExpandableListView
                    .getPackedPositionForGroup(groupPosition));
            parent.setItemChecked(flatPosition, !parent.isItemChecked(flatPosition));
            return true;
        } else if (mActionModeStarted) {
            return true;
        }

        final ServerWrapper item = mListAdapter.getGroup(groupPosition);
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
        if (mActionModeStarted && mSelectionType == ExpandableListView
                .PACKED_POSITION_TYPE_CHILD) {
            int flatPosition = parent.getFlatListPosition(ExpandableListView
                    .getPackedPositionForChild(groupPosition, childPosition));
            parent.setItemChecked(flatPosition, !parent.isItemChecked(flatPosition));
            return true;
        } else if (mActionModeStarted) {
            return true;
        }

        final Conversation conversation = mListAdapter.getChild(groupPosition, childPosition);
        mCallback.onSubServerClicked(conversation);

        return true;
    }

    public void onEditServer(final ServerWrapper builder) {
        final Intent intent = new Intent(getActivity(), ServerPreferenceActivity.class);
        intent.putExtra(ServerPreferenceActivity.NEW_SERVER, false);
        intent.putExtra(ServerPreferenceActivity.SERVER, builder.getBuilder());
        getActivity().startActivityForResult(intent, MainActivity.SERVER_SETTINGS);
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
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.activity_server_list_popup, menu);
        mActionModeStarted = true;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        if (mSelectionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            final int count = mListView.getCheckedItemCount();
            final boolean singleItemChecked = count == 1;
            final boolean connected = count > 0 && getFirstCheckedItem().isConnected();

            menu.findItem(R.id.activity_server_list_popup_edit).setVisible(singleItemChecked &&
                    !connected);

            boolean deleteEnabled = !connected;
            for (int i = 1; i < count && deleteEnabled; i++) {
                final int pos = mListView.getCheckedItemPositions().keyAt(i);
                final ServerWrapper listItem = (ServerWrapper) mListView
                        .getItemAtPosition(pos);
                deleteEnabled = !listItem.isConnected();
            }
            menu.findItem(R.id.activity_server_list_popup_delete).setVisible(deleteEnabled);
        } else {
            menu.findItem(R.id.activity_server_list_popup_edit).setVisible(false);
            menu.findItem(R.id.activity_server_list_popup_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final ServerWrapper listItem = getFirstCheckedItem();

        switch (item.getItemId()) {
            case R.id.activity_server_list_popup_delete:
                // TODO - AsyncTask this
                final BuilderDatabaseSource source = new BuilderDatabaseSource(getActivity());
                source.open();
                source.removeServer(listItem.getBuilder().getId());
                source.close();
                refreshServers();
                break;
            case R.id.activity_server_list_popup_edit:
                onEditServer(listItem);
        }

        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionModeStarted = false;
    }

    private ServerWrapper getFirstCheckedItem() {
        if (mListView.getCheckedItemCount() == 0) {
            return null;
        }

        final int position = mListView.getCheckedItemPositions().keyAt(0);
        return (ServerWrapper) mListView.getItemAtPosition(position);
    }

    public void onServiceConnected(final IRCService service) {
        mService = service;

        refreshServers();
    }

    public interface Callback {

        public void onServerClicked(final Server server);

        public void onSubServerClicked(final Conversation object);

        public void onServerDisconnected(final Server server);

        public IRCService getService();

        public void onPart(final String serverName, final PartEvent event);

        public void disconnectFromServer(Server server);
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
        public void onEventMainThread(final NewPrivateMessage event) throws InterruptedException {
            final PrivateMessageUser user = mServer.getUserChannelInterface()
                    .getPrivateMessageUser(event.nick);
            mListAdapter.getGroup(mServerIndex).addServerObject(user);
            mListAdapter.notifyDataSetChanged();
            mListView.expandGroup(mServerIndex);
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final JoinEvent event) throws InterruptedException {
            final Channel channel = mServer.getUserChannelInterface().getChannel(event
                    .channelName);
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
        public void onEventMainThread(final ChannelEvent event) {
            if (!ChannelFragment.sClasses.contains(event.getClass())) {
                mListView.invalidateViews();
            }
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final UserEvent event) {
            mListView.invalidateViews();
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final ServerEvent event) {
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final ConnectEvent event) {
            mListView.invalidateViews();
        }

        @SuppressWarnings("unused")
        public void onEventMainThread(final DisconnectEvent event) {
            if (event.userSent) {
                refreshServers();

                unregister();
                mEventHandlers.remove(this);
                mCallback.onServerDisconnected(mServer);
            } else {
                // TODO - check what needs to be done here
                mListView.invalidateViews();
            }
        }

        public void unregister() {
            mServer.getServerEventBus().unregister(this);
        }
    }
}
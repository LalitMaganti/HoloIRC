package com.fusionx.lightirc.ui;

import com.google.common.base.Optional;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.SessionStopRequestedEvent;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.loader.SessionContainerLoader;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.model.SessionContainer;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.fusionx.relay.core.SessionStatus;
import co.fusionx.relay.conversation.Conversation;
import co.fusionx.relay.core.Session;
import co.fusionx.relay.conversation.QueryUser;
import co.fusionx.relay.dcc.event.chat.DCCChatEvent;
import co.fusionx.relay.dcc.event.chat.DCCChatStartedEvent;
import co.fusionx.relay.dcc.event.file.DCCFileConversationStartedEvent;
import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.event.channel.PartEvent;
import co.fusionx.relay.event.query.QueryClosedEvent;
import co.fusionx.relay.event.query.QueryEvent;
import co.fusionx.relay.event.server.JoinEvent;
import co.fusionx.relay.event.server.KickEvent;
import co.fusionx.relay.event.server.NewPrivateMessageEvent;
import co.fusionx.relay.event.server.StatusChangeEvent;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class SessionOverviewFragment extends Fragment {

    private final EventHandler mEventHandler = new EventHandler();

    private final Map<Session, ServerEventHandler> mEventHandlers = new HashMap<>();

    private Callback mCallback;

    private RecyclerView mRecyclerView;

    private SessionOverviewAdapter mAdapter;

    private IRCService mService;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallback = (Callback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SessionOverviewAdapter(getActivity(), v -> {
            final int position = mRecyclerView.getChildPosition(v);

            int groupPosition = mAdapter.getGroupPosition(position);
            onServerClick(groupPosition);
        }, v -> {
            final int position = mRecyclerView.getChildPosition(v);

            int groupPosition = mAdapter.getGroupPosition(position);
            int childPosition = mAdapter.getChildPosition(position, groupPosition);
            onConversationClicked(groupPosition, childPosition);
        });
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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.server_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        // mRecyclerView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        // mRecyclerView.setEmptyView(view.findViewById(android.R.id.empty));

        // mRecyclerView.setMultiChoiceModeListener(this);

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
        if (mAdapter == null) {
            return;
        }
        // mAdapter.refreshConversations();
        mAdapter.notifyDataSetChanged();
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

    /*
    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
            final int groupPosition, final long id) {
                    /*if (mActionMode != null && mSelectionType == ExpandableListView
                .PACKED_POSITION_TYPE_GROUP) {
            int flatPosition = mRecyclerView.getFlatListPosition(ExpandableListView
                    .getPackedPositionForGroup(groupPosition));
            mRecyclerView.setItemChecked(flatPosition, !mRecyclerView.isItemChecked(flatPosition));
            return true;
        } else if (mActionMode != null) {
            return true;
        }

}

    public void onPanelClosed() {
        /*if (mActionMode != null) {
            mActionMode.finish();
        }
    }
*/
    /*
    @Override
    public boolean onChildClick(final ExpandableListView parent, final View v,
            final int groupPosition, final int childPosition, final long id) {
        /*if (mActionMode != null && mSelectionType == ExpandableListView
                .PACKED_POSITION_TYPE_CHILD) {
            int flatPosition = parent.getFlatListPosition(ExpandableListView
                    .getPackedPositionForChild(groupPosition, childPosition));
            parent.setItemChecked(flatPosition, !parent.isItemChecked(flatPosition));
            return true;
        } else if (mActionMode != null) {
            return true;
        }

        return true;
    }*/

    public void onConversationClicked(final int groupPosition, final int childPosition) {
        final SessionContainer container = mAdapter.getGroup(groupPosition);
        final Conversation conversation = container.getConversation(childPosition);
        mCallback.onConversationClicked(container.getSession(), conversation);
    }

    /*
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        /*if (!checked) {
            mRecyclerView.getCheckedItemPositions().delete(position);
        }
        final int count = mRecyclerView.getCheckedItemCount();
        final boolean singleItemChecked = count == 1;

        if (checked && singleItemChecked) {
            mSelectionType = ExpandableListView.getPackedPositionType(mRecyclerView
                    .getExpandableListPosition(position));
        }

        mode.invalidate();
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        /* mode.getMenuInflater().inflate(R.menu.activity_server_list_popup, menu);
        mActionMode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        /*if (mSelectionType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            final int count = mRecyclerView.getCheckedItemCount();
            final boolean connected = count > 0 && getFirstCheckedItem().isServerAvailable();

            boolean allConnected = connected;
            boolean deleteEnabled = !connected;
            for (int i = 1; i < count && (deleteEnabled || allConnected); i++) {
                final int pos = mRecyclerView.getCheckedItemPositions().keyAt(i);
                final ConnectionContainer listItem = (ConnectionContainer) mRecyclerView
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
        /*final ConnectionContainer listItem = getFirstCheckedItem();

        switch (item.getItemId()) {
            case R.id.activity_server_list_popup_disconnect:
                disconnectFromServer(getCheckedPositions(mRecyclerView));
                break;
            case R.id.activity_server_list_popup_delete:
                deleteServer(getCheckedPositions(mRecyclerView));
                break;
            case R.id.activity_server_list_popup_edit:
                editServer(listItem);
                break;
        }
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        // mActionMode = null;
    }*/

    public void onServiceConnected(final IRCService service) {
        mService = service;

        refreshServers();
    }

    void editServer(final SessionContainer builder) {
        final Intent intent = new Intent(getActivity(), ServerPreferenceActivity.class);
        intent.putExtra(ServerPreferenceActivity.NEW_SERVER, false);
        intent.putExtra(ServerPreferenceActivity.SERVER, builder.getBuilder());
        getActivity().startActivityForResult(intent, MainActivity.SERVER_SETTINGS);
    }

    private void disconnectFromServer(final List<Integer> checkedPositions) {
        /*for (final Integer checkedPosition : checkedPositions) {
            final long packed = mRecyclerView.getExpandableListPosition(checkedPosition);
            final int group = ExpandableListView.getPackedPositionGroup(packed);
            mService.requestConnectionStoppage(mAdapter.getGroup(group).getConnection());
        }*/
    }

    private boolean onServerClick(final int groupPosition) {
        final SessionContainer item = mAdapter.getGroup(groupPosition);
        if (item.getSession() == null) {
            final Session session = mService.requestConnectionToServer(item.getBuilder());
            item.setSession(Optional.fromNullable(session));

            mEventHandlers.put(session, new ServerEventHandler(item, groupPosition));
        }
        mCallback.onConversationClicked(item.getSession(), item.getSession().getServer());

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
                /*for (final int checkedPosition : checkedPositions) {
                    final long packed = mRecyclerView.getExpandableListPosition(checkedPosition);
                    final int group = ExpandableListView.getPackedPositionGroup(packed);

                    final ConnectionContainer listItem = mAdapter.getGroup(group);
                    source.removeServer(listItem.getBuilder().getId());
                }*/
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                refreshServers();
            }
        };
        asyncTask.execute();
    }

    /*private ConnectionContainer getFirstCheckedItem() {
        if (mRecyclerView.getCheckedItemCount() == 0) {
            return null;
        }

        final int position = mRecyclerView.getCheckedItemPositions().keyAt(0);
        return (ConnectionContainer) mRecyclerView.getItemAtPosition(position);
    }*/

    void refreshServers() {
        final LoaderManager.LoaderCallbacks<ArrayList<SessionContainer>> callbacks
                = new LoaderManager.LoaderCallbacks<ArrayList<SessionContainer>>() {
            @Override
            public Loader<ArrayList<SessionContainer>> onCreateLoader(int id,
                    Bundle args) {
                return new SessionContainerLoader(getActivity());
            }

            @Override
            public void onLoadFinished(final Loader<ArrayList<SessionContainer>> loader,
                    final ArrayList<SessionContainer> listItems) {
                mAdapter.clear();
                mAdapter.addAll(listItems);

                final TextView textView = (TextView) getView().findViewById(R.id.empty_text_view);
                textView.setText("No servers found :(\nClick + to add one");

                getView().findViewById(R.id.progress_bar_empty).setVisibility(View.GONE);

                for (final ServerEventHandler eventHandler : mEventHandlers.values()) {
                    eventHandler.unregister();
                }
                mEventHandlers.clear();

                for (int i = 0, listItemsSize = listItems.size(); i < listItemsSize; i++) {
                    SessionContainer wrapper = listItems.get(i);
                    if (wrapper.getSession() != null) {
                        mEventHandlers.put(wrapper.getSession(),
                                new ServerEventHandler(wrapper, i));
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<SessionContainer>> loader) {
            }
        };

        getLoaderManager().restartLoader(21, null, callbacks);
    }

    public interface Callback {

        public void onConversationClicked(final Session connection,
                final Conversation conversation);

        public void onServerStopped(final Session server);

        public IRCService getService();

        public void onPart(final Session connection, final PartEvent event);

        public boolean onKick(final Session connection, final KickEvent event);

        public void onPrivateMessageClosed(final QueryUser queryUser);
    }

    public class ServerEventHandler {

        private final int mGroupPosition;

        private final Session mSession;

        private final SessionContainer mSessionContainer;

        public ServerEventHandler(final SessionContainer container, final int groupPosition) {
            mSession = container.getSession();
            mGroupPosition = groupPosition;
            mSessionContainer = container;

            register();
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final NewPrivateMessageEvent event) {
            mAdapter.addChild(mGroupPosition, event.user);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final JoinEvent event) {
            mAdapter.addChild(mGroupPosition, event.channel);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final PartEvent event) {
            mAdapter.removeChild(mGroupPosition, event.conversation);
            mCallback.onPart(mSession, event);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final KickEvent event) {
            mAdapter.removeChild(mGroupPosition, event.channel);

            final boolean switchToServer = mCallback.onKick(mSession, event);
            if (switchToServer) {
                onServerClick(mGroupPosition);
            }
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final ChannelEvent event) {
            if (EventUtils.shouldStoreEvent(event)
                    && mSession.getStatus() != SessionStatus.DISCONNECTED) {
                final int index = mSessionContainer.indexOf(event.conversation);
                mAdapter.notifyChildChanged(mGroupPosition, index);
            }
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final QueryClosedEvent event) {
            mAdapter.removeChild(mGroupPosition, event.conversation);
            mCallback.onPrivateMessageClosed(event.conversation);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final QueryEvent event) {
            if (mSession.getStatus() != SessionStatus.DISCONNECTED) {
                final int index = mSessionContainer.indexOf(event.conversation);
                mAdapter.notifyChildChanged(mGroupPosition, index);
            }
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final StatusChangeEvent event) {
            mAdapter.notifyItemChanged(mGroupPosition);
        }

        // DCC Events
        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final DCCChatStartedEvent event) {
            mAdapter.addChild(mGroupPosition, event.conversation);
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final DCCChatEvent event) {
            if (EventUtils.shouldStoreEvent(event)
                    && mSession.getStatus() != SessionStatus.DISCONNECTED) {
                final int index = mSessionContainer.indexOf(event.conversation);
                mAdapter.notifyChildChanged(mGroupPosition, index);
            }
        }

        @Subscribe(threadType = ThreadType.MAIN)
        public void onEventMainThread(final DCCFileConversationStartedEvent event) {
            mAdapter.addChild(mGroupPosition, event.conversation);
        }

        public void register() {
            mSession.registerForEvents(this, 50);
        }

        public void unregister() {
            mSession.unregisterFromEvents(this);
        }

        public int getGroupPosition() {
            return mGroupPosition;
        }

        public SessionContainer getSessionContainer() {
            return mSessionContainer;
        }
    }

    private class EventHandler {

        @Subscribe
        public void onConversationChanged(final OnConversationChanged event) {
            if (event.conversation != null) {
                final ServiceEventInterceptor eventInterceptor = IRCService
                        .getEventHelper(event.session);
                if (event.fragmentType == FragmentType.SERVER) {
                    eventInterceptor.clearMessagePriority();
                } else {
                    eventInterceptor.clearMessagePriority(event.conversation);
                }

                final ServerEventHandler eventHandler = mEventHandlers.get(event.session);
                final int groupPosition = eventHandler.getGroupPosition();
                final int childPosition = eventHandler.getSessionContainer()
                        .indexOf(event.conversation);
                mAdapter.notifyChildChanged(groupPosition, childPosition);
            }
        }

        // Make sure the events look up to date if the full line highlight pref is changed
        @Subscribe
        public void onPreferencesChanged(final OnPreferencesChangedEvent event) {
            mAdapter.notifyDataSetChanged();
        }

        @Subscribe
        public void onStopEvent(final SessionStopRequestedEvent event) {
            final ServerEventHandler eventHandler = mEventHandlers.remove(event.session);
            eventHandler.unregister();

            final int groupPosition = eventHandler.getGroupPosition();
            mAdapter.removeGroup(groupPosition);

            mCallback.onServerStopped(event.session);
        }
    }
}

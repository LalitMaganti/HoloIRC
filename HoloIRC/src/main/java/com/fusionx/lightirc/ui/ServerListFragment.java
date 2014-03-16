package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ExpandableServerListAdapter;
import com.fusionx.lightirc.communication.NewIRCService;
import com.fusionx.lightirc.loader.AbstractLoader;
import com.fusionx.lightirc.loader.ServiceLoader;
import com.fusionx.lightirc.model.WrappedServerListItem;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gnu.trove.set.hash.THashSet;

import static butterknife.ButterKnife.findById;

public class ServerListFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<NewIRCService>, ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnChildClickListener, ExpandableServerListAdapter.Callback {

    private int mLastGroup = -1;

    private THashSet<ServerEventHandler> mEventHandlers = new THashSet<>();

    private NewIRCService mService;

    private Callback mCallback;

    private ExpandableListView mServerList;

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

        mServerList = findById(view, R.id.server_list);
        mServerList.setGroupIndicator(null);
        mServerList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Object>() {
            @Override
            public Loader<Object> onCreateLoader(int i, Bundle bundle) {
                final Handler handler = new Handler();
                final Dialog dialog = new Dialog(getActivity());
                return new AbstractLoader<Object>(getActivity()) {
                    @Override
                    public Object loadInBackground() {
                        final List<File> fileList = SharedPreferencesUtils.getOldServers
                                (getActivity());
                        if (!fileList.isEmpty()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.setTitle("Please wait...");
                                    dialog.show();
                                }
                            });
                            SharedPreferencesUtils.migrateToDatabase(fileList, getActivity());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.cancel();
                                }
                            });
                        }
                        return null;
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Object> objectLoader, Object o) {
                // This can happen when rotation has occurred - the view has been redrawn but the
                // comms layer should be constant
                if (mService != null) {
                    mServerList.setAdapter(mListAdapter);
                    mServerList.setOnGroupClickListener(ServerListFragment.this);
                    mServerList.setOnChildClickListener(ServerListFragment.this);
                } else {
                    getLoaderManager().initLoader(1, null, ServerListFragment.this);
                }
            }

            @Override
            public void onLoaderReset(Loader<Object> objectLoader) {
            }
        });
    }

    @Override
    public Loader<NewIRCService> onCreateLoader(final int i, final Bundle bundle) {
        return new ServiceLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<NewIRCService> ircBinderLoader,
            final NewIRCService service) {
        mService = service;

        final List<WrappedServerListItem> listItems = new ArrayList<>();

        final BuilderDatabaseSource source = new BuilderDatabaseSource(getActivity());

        source.open();
        for (final ServerConfiguration.Builder builder : source.getAllBuilders()) {
            listItems.add(new WrappedServerListItem(builder, service.getServerIfExists(builder)));
        }
        source.close();

        mListAdapter = new ExpandableServerListAdapter(getActivity(), listItems, mServerList,
                this);

        mServerList.setAdapter(mListAdapter);
        mServerList.setOnGroupClickListener(this);
        mServerList.setOnChildClickListener(this);
    }

    @Override
    public void onLoaderReset(final Loader<NewIRCService> listLoader) {
    }

    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
            final int groupPosition, final long id) {
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
            mServerList.setAdapter(mListAdapter);
            mServerList.expandGroup(mServerIndex);
        }

        @Subscribe
        public void onConnect(final ConnectEvent event) {
            mServerList.invalidateViews();
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
}
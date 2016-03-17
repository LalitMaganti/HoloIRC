package com.fusionx.lightirc.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnServiceConnectionStateChanged;
import com.fusionx.lightirc.misc.Theme;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.service.ServiceEventInterceptor;

import java.util.Collection;
import java.util.Collections;

import co.fusionx.relay.base.Server;
import co.fusionx.relay.event.server.InviteEvent;

import static com.fusionx.lightirc.misc.AppPreferences.getAppPreferences;
import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class InviteFragment extends AppCompatDialogFragment {
    private EventHandler mEventHandler = new EventHandler();

    private ServiceEventInterceptor mInterceptor;

    private RecyclerView mRecyclerView;

    private InviteAdapter mAdapter;

    public static InviteFragment createInstance() {
        return new InviteFragment();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_FRAME, getAppPreferences().getTheme() == Theme.DARK
                ? android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog
                : android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle bundle) {
        View v = inflater.inflate(R.layout.invites_fragment, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }

    @Override
    public void onViewCreated(final View view, final Bundle bundle) {
        super.onViewCreated(view, bundle);
        getBus().registerSticky(mEventHandler);

        mAdapter = new InviteAdapter(getActivity(), new AcceptListener(),
                new DeclineListener());
        mRecyclerView.setAdapter(mAdapter);
        updateAdapter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getBus().unregister(mEventHandler);
    }

    private void updateAdapter() {
        if (mAdapter == null) {
            return;
        }
        final Collection<InviteEvent> events = mInterceptor != null
                ? mInterceptor.getInviteEvents() : null;
        mAdapter.setItems(events);
    }

    private class AcceptListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            final InviteEvent event = (InviteEvent) v.getTag();
            mInterceptor.acceptInviteEvents(Collections.singletonList(event));
            mAdapter.remove(event);
        }
    }

    private class DeclineListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            final InviteEvent event = (InviteEvent) v.getTag();
            mInterceptor.declineInviteEvents(Collections.singletonList(event));
            mAdapter.remove(event);
        }
    }

    private class EventHandler {
        private Server mServer;
        private IRCService mService;

        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            mServer = conversationChanged.conversation != null
                    ? conversationChanged.conversation.getServer() : null;
            updateInterceptor();
        }

        @Subscribe
        public void onEvent(final OnServiceConnectionStateChanged serviceChanged) {
            mService = serviceChanged.getService();
            updateInterceptor();
        }

        private void updateInterceptor() {
            ServiceEventInterceptor interceptor = mServer != null && mService != null
                    ? mService.getEventHelper(mServer) : null;
            if (interceptor != mInterceptor) {
                mInterceptor = interceptor;
                updateAdapter();
            }
        }
    }

}

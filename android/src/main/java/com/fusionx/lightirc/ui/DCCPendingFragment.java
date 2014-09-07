package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.misc.Theme;
import com.fusionx.lightirc.service.ServiceEventInterceptor;
import com.fusionx.lightirc.util.FragmentUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import co.fusionx.relay.event.server.DCCChatRequestEvent;
import co.fusionx.relay.event.server.DCCRequestEvent;
import co.fusionx.relay.event.server.DCCSendRequestEvent;

import static com.fusionx.lightirc.misc.AppPreferences.getAppPreferences;
import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class DCCPendingFragment extends DialogFragment {

    private final EventHandler mEventHandler = new EventHandler();

    private Callbacks mCallbacks;

    private ListView mListView;

    private DCCAdapter mAdapter;

    private ServiceEventInterceptor mInterceptor;

    public static DCCPendingFragment createInstance() {
        return new DCCPendingFragment();
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInterceptor = mCallbacks.getEventHelper();

        setStyle(STYLE_NO_FRAME, getAppPreferences().getTheme() == Theme.DARK
                ? android.R.style.Theme_DeviceDefault_Dialog
                : android.R.style.Theme_DeviceDefault_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dcc_pending_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBus().registerSticky(mEventHandler);

        final Button button = (Button) view.findViewById(R.id.dcc_pending_ok);
        button.setOnClickListener(v -> dismiss());

        mAdapter = new DCCAdapter(getActivity(), mInterceptor.getDCCRequests(),
                new AcceptListener(), new DeclineListener());

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        mInterceptor.getServer().getServerWideBus().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        final ServiceEventInterceptor interceptor = mCallbacks.getEventHelper();
        interceptor.getServer().getServerWideBus().unregister(this);
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final DCCRequestEvent requestEvent) {
        final ServiceEventInterceptor interceptor = mCallbacks.getEventHelper();
        mAdapter.replaceAll(interceptor.getDCCRequests());
    }

    public interface Callbacks {

        public ServiceEventInterceptor getEventHelper();
    }

    private static class DCCAdapter extends BaseAdapter {

        private final List<DCCRequestEvent> mRequestEventList;

        private final LayoutInflater mLayoutInflater;

        private final View.OnClickListener mAcceptListener;

        private final View.OnClickListener mDeclineListener;

        public DCCAdapter(final Context context, final Collection<DCCRequestEvent> dccRequests,
                final View.OnClickListener acceptListener, final View.OnClickListener
                declineListener) {
            mLayoutInflater = LayoutInflater.from(context);
            mRequestEventList = new ArrayList<>(dccRequests);
            mAcceptListener = acceptListener;
            mDeclineListener = declineListener;
        }

        @Override
        public int getCount() {
            return mRequestEventList.size();
        }

        @Override
        public DCCRequestEvent getItem(final int position) {
            return mRequestEventList.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                final ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.dcc_pending_list_item, parent,
                        false);
            }

            final DCCRequestEvent requestEvent = getItem(position);
            String type = "";
            if (requestEvent instanceof DCCChatRequestEvent) {
                type = "CHAT";
            } else if (requestEvent instanceof DCCSendRequestEvent) {
                type = "SEND";
            }
            final String titleText = String.format("%1$s requested by %2$s", type,
                    requestEvent.getPendingConnection().getDccRequestNick());
            final TextView title = (TextView) convertView
                    .findViewById(R.id.dcc_pending_list_item_title);
            title.setText(titleText);

            final String contentText = String.format("Suggested IP %1$s and port %2$d for %3$s",
                    requestEvent.getPendingConnection().getIP(),
                    requestEvent.getPendingConnection().getPort(),
                    requestEvent.getPendingConnection().getArgument());
            final TextView content = (TextView) convertView
                    .findViewById(R.id.dcc_pending_list_item_content);
            content.setText(contentText);

            final ImageView accept = (ImageView) convertView.findViewById(R.id.accept_list_item);
            accept.setOnClickListener(mAcceptListener);
            accept.setTag(position);

            final ImageView decline = (ImageView) convertView.findViewById(R.id.decline_list_item);
            decline.setOnClickListener(mDeclineListener);
            decline.setTag(position);

            return convertView;
        }

        public void replaceAll(final Set<DCCRequestEvent> dccRequests) {
            mRequestEventList.clear();
            mRequestEventList.addAll(dccRequests);
            notifyDataSetChanged();
        }
    }

    private class AcceptListener implements View.OnClickListener {

        @Override
        public void onClick(final View a) {
            final int position = (int) a.getTag();
            final DCCRequestEvent event = mAdapter.getItem(position);
            mInterceptor.acceptDCCConnection(event);
            mAdapter.replaceAll(mInterceptor.getDCCRequests());
        }
    }

    private class DeclineListener implements View.OnClickListener {

        @Override
        public void onClick(final View d) {
            final int position = (int) d.getTag();
            final DCCRequestEvent event = mAdapter.getItem(position);
            mInterceptor.declineDCCRequestEvent(event);
            mAdapter.replaceAll(mInterceptor.getDCCRequests());
        }
    }

    private class EventHandler {

        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {

        }
    }
}
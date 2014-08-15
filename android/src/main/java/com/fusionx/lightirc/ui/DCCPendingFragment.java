package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import co.fusionx.relay.event.server.DCCRequestEvent;

import static com.fusionx.lightirc.misc.AppPreferences.getAppPreferences;

public class DCCPendingFragment extends DialogFragment {

    private Callbacks mCallbacks;

    private ListView mListView;

    private DCCAdapter mAdapter;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        final ServiceEventInterceptor interceptor = mCallbacks.getEventInterceptor();
        mAdapter = new DCCAdapter(getActivity(), interceptor.getDCCRequests());

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        interceptor.getServer().getServerEventBus().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        final ServiceEventInterceptor interceptor = mCallbacks.getEventInterceptor();
        interceptor.getServer().getServerEventBus().unregister(this);
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final DCCRequestEvent requestEvent) {
        final ServiceEventInterceptor interceptor = mCallbacks.getEventInterceptor();
        mAdapter.replaceAll(interceptor.getDCCRequests());
    }

    public interface Callbacks {

        public ServiceEventInterceptor getEventInterceptor();
    }

    private static class DCCAdapter extends BaseAdapter {

        private final List<DCCRequestEvent> mRequestEventList;

        private final LayoutInflater mLayoutInflater;

        public DCCAdapter(final Context context, final Collection<DCCRequestEvent> dccRequests) {
            mLayoutInflater = LayoutInflater.from(context);
            mRequestEventList = new ArrayList<>(dccRequests);
        }

        @Override
        public int getCount() {
            return mRequestEventList.size();
        }

        @Override
        public Object getItem(final int position) {
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
            return convertView;
        }

        public void replaceAll(final Set<DCCRequestEvent> dccRequests) {
            mRequestEventList.clear();
            mRequestEventList.addAll(dccRequests);
            notifyDataSetChanged();
        }
    }
}
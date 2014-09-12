package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.service.ServiceEventInterceptor;
import com.fusionx.lightirc.util.FragmentUtils;

import org.lucasr.twowayview.TwoWayView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import co.fusionx.relay.event.server.InviteEvent;

public class InviteFragment extends DialogFragment {

    private Callbacks mCallbacks;

    private TwoWayView mTwoWayView;

    private InviteAdapter mAdapter;

    public static InviteFragment createInstance() {
        return new InviteFragment();
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle bundle) {
        return inflater.inflate(R.layout.invites_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle bundle) {
        super.onViewCreated(view, bundle);

        mTwoWayView = (TwoWayView) view.findViewById(android.R.id.list);
        updateAdapter();
    }

    private void updateAdapter() {
        final Collection<InviteEvent> events = mCallbacks.getInterceptor().getInviteEvents();
        mAdapter = new InviteAdapter(getActivity(), events, new AcceptListener(),
                new DeclineListener());
        mTwoWayView.setAdapter(mAdapter);
    }

    public interface Callbacks {

        public void acceptInviteEvents(final InviteEvent event);

        public void declineInviteEvents(final InviteEvent event);

        public ServiceEventInterceptor getInterceptor();
    }

    private class AcceptListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            final InviteEvent event = (InviteEvent) v.getTag();
            mCallbacks.acceptInviteEvents(event);
            mAdapter.remove(event);
        }
    }

    private class DeclineListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            final InviteEvent event = (InviteEvent) v.getTag();
            mCallbacks.declineInviteEvents(event);
            mAdapter.remove(event);
        }
    }
}
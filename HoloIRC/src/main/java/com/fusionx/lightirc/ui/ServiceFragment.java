package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ServiceFragment extends Fragment {

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCService.IRCBinder) binder).getService();
            final ServerConfiguration.Builder builder = getActivity().getIntent()
                    .getParcelableExtra("server");
            if (builder == null) {
                final ServerConfiguration configuration = getActivity().getIntent()
                        .getParcelableExtra("serverConfig");
                mServer = mService.connectToServer(configuration);
            } else {
                mServer = mService.connectToServer(builder.build());
            }
            mCallback.onServerAvailable(mServer);
            mCallback.onSetupViewPager();

            mService.setNoMention(mServer.getConfiguration().getTitle());
        }

        // Should never occur
        @Override
        public void onServiceDisconnected(final ComponentName name) {
        }
    };

    private IRCService mService;

    private Callbacks mCallback;

    private Server mServer;

    /**
     * This method will only be called once when the retained Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    public void connectToServer(Context context, final String serverTitle) {
        if (mService == null || mServer == null) {
            final Intent service = new Intent(context, IRCService.class);
            service.putExtra("serverName", serverTitle);
            service.putExtra("stop", false);
            service.putExtra(IRCService.MENTIONACTIVITY, serverTitle);

            context.getApplicationContext().startService(service);
            context.getApplicationContext().bindService(service, mConnection, 0);
        }
    }

    /**
     * Hold a reference to the parent Activity so we can report the task's current progress and
     * results. The Android framework will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "Callback");
        }
    }

    @Override
    public void onResume() {
        super.onPause();

        if (mService != null) {
            mService.setNoMention(mServer.getConfiguration().getTitle());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mService != null) {
            mService.setNoMention(null);
        }
    }

    /**
     * Set the callback to null so we don't accidentally leak the Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();

        mCallback = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getApplicationContext().unbindService(mConnection);
    }

    /**
     * This is a worker fragment so return null for the view always
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return null;
    }

    public Server getServer() {
        return mServer;
    }

    public void disconnectFromServer() {
        if (mService != null) {
            mService.disconnect(mServer);
        }
    }

    public void onFinalUnexpectedDisconnect() {
        if (mService != null) {
            mService.onFinalUnexpectedDisconnect(mServer);
        }
    }

    public interface Callbacks {

        public void onSetupViewPager();

        public void onServerAvailable(final Server server);
    }
}
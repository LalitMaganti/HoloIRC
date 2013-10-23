package com.fusionx.lightirc.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.communication.MessageSender;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;

public class ServiceFragment extends Fragment {
    private IRCService mService;
    private ServiceFragmentCallback mCallbacks;
    private MessageSender mSender;
    private Server mServer;

    /**
     * This method will only be called once when the retained Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        mSender = MessageSender.getSender(mCallbacks.getServerTitle());
    }

    public void connectToServer(Context context, final String serverTitle) {
        if (mService == null) {
            final Intent service = new Intent(context, IRCService.class);
            service.putExtra("server", true);
            service.putExtra("serverName", serverTitle);
            service.putExtra("stop", false);
            service.putExtra("setBound", serverTitle);

            context.getApplicationContext().startService(service);
            context.getApplicationContext().bindService(service, mConnection, 0);
        }
    }

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (ServiceFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "ServiceFragmentCallback");
        }
        if (mSender == null) {
            mSender = MessageSender.getSender(mCallbacks.getServerTitle());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mService != null) {
            mService.setServerDisplayed(mCallbacks.getServerTitle());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mService != null) {
            mService.setServerDisplayed(null);
        }
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = null;
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

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCService.IRCBinder) binder).getService();

            mService.setServerDisplayed(mCallbacks.getServerTitle());

            if (getServer(mCallbacks.getServerTitle()) != null) {
                mCallbacks.setUpViewPager();
                mCallbacks.repopulateFragmentsInPager();
            } else {
                final ServerConfiguration.Builder builder =
                        getActivity().getIntent().getParcelableExtra("server");
                mService.connectToServer(builder);
                mCallbacks.setUpViewPager();
            }
        }

        // Should never occur
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            throw new IllegalArgumentException();
        }
    };

    public Server getServer(final String serverTitle) {
        if (mServer == null && mService != null) {
            mServer = mService.getServer(serverTitle);
        }
        return mServer;
    }

    public void removeServiceReference(final String serverTitle) {
        final AsyncTask<Void, Void, Void> disconnect = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (mService != null) {
                    mService.setServerDisplayed(null);
                    mService.removeServerFromManager(serverTitle);
                }
                return null;
            }
        };
        disconnect.execute();
    }

    public MessageSender getSender() {
        return mSender;
    }

    public interface ServiceFragmentCallback {
        public void setUpViewPager();

        public String getServerTitle();

        public void repopulateFragmentsInPager();

        public void onDisconnect(final boolean expected, final boolean retryPending);
    }
}
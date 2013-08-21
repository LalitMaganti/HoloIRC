package com.fusionx.lightirc.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;

import com.fusionx.irc.Channel;
import com.fusionx.irc.PrivateMessageUser;
import com.fusionx.irc.Server;
import com.fusionx.irc.ServerConfiguration;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.uiircinterface.IRCBridgeService;
import com.fusionx.uiircinterface.MessageSender;
import com.fusionx.uiircinterface.interfaces.FragmentSideHandlerInterface;

import java.util.Iterator;

public class ServiceFragment extends Fragment {
    private IRCBridgeService mService;
    private ServiceFragmentCallback mCallback;
    private MessageSender sender;

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        sender = MessageSender.getSender(mCallback.getServerTitle());
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

        mCallback = (ServiceFragmentCallback) activity;
        if(sender != null) {
            sender.registerServerChannelHandler(mCallback);
        } else {
            MessageSender.getSender(mCallback.getServerTitle()).registerServerChannelHandler
                    (mCallback);
        }
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();

        sender.unregisterFragmentSideHandlerInterface();
        mCallback = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mService == null) {
            setUpService();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mService != null) {
            mService.setServerDisplayed(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getApplicationContext().unbindService(mConnection);
        mService = null;
    }

    public void setUpService() {
        final Intent service = new Intent(getActivity(), IRCBridgeService.class);
        service.putExtra("server", true);
        service.putExtra("serverName", mCallback.getServerTitle());
        service.putExtra("stop", false);
        service.putExtra("setBound", mCallback.getServerTitle());

        getActivity().getApplicationContext().startService(service);
        getActivity().getApplicationContext().bindService(service, mConnection, 0);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCBridgeService.IRCBinder) binder).getService();
            mCallback.setUpViewPager();

            mService.setServerDisplayed(mCallback.getServerTitle());

            if (getServer(true) != null) {
                mCallback.repopulateFragmentsInPager();
            } else {
                final ServerConfiguration.Builder builder =
                        getActivity().getIntent().getParcelableExtra("server");
                mService.connectToServer(builder);
            }
        }

        // Should not occur
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService.disconnectFromServer(mCallback.getServerTitle());
            mService = null;

            mCallback.onUnexpectedDisconnect();
        }
    };

    public Server getServer(final boolean nullAllowed) {
        Server server;
        if (mService == null || (server = mService.getServer(mCallback.getServerTitle())) == null) {
            if (nullAllowed) {
                return null;
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            return server;
        }
    }

    public void removeServiceReference() {
        mService.setServerDisplayed(null);
        mService.onUnexpectedDisconnect(mCallback.getServerTitle());
        mService = null;
    }

    public void disconnect() {
        MessageSender.getSender(mCallback.getServerTitle()).unregisterFragmentSideHandlerInterface();
        if (mService != null) {
            mService.disconnectFromServer(mCallback.getServerTitle());
        }

        final Intent intent = new Intent(getActivity(), MainServerListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public interface ServiceFragmentCallback extends FragmentSideHandlerInterface {
        public void setUpViewPager();

        public void repopulateFragmentsInPager();
    }
}
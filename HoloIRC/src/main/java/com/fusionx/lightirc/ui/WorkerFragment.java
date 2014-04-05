package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.loader.ServiceLoader;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.relay.Server;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.io.File;
import java.util.List;

/**
 * Class where all the IRC work is carried out
 */
public class WorkerFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<IRCService> {

    private IRCService mService;

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = (Callback) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            private Handler mHandler;

            private Dialog mDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mHandler = new Handler();
                mDialog = new Dialog(getActivity());
            }

            @Override
            protected Void doInBackground(Void... params) {
                final List<File> fileList = SharedPreferencesUtils.getOldServers(getActivity());
                if (!fileList.isEmpty()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.setCancelable(false);
                            mDialog.setCanceledOnTouchOutside(false);
                            mDialog.setTitle("Please wait...");
                            mDialog.show();
                        }
                    });
                    SharedPreferencesUtils.migrateToDatabase(fileList, getActivity());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.cancel();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mService == null) {
                    getLoaderManager().initLoader(1, null, WorkerFragment.this);
                }
            }
        };
        asyncTask.execute();
    }

    @Override
    public Loader<IRCService> onCreateLoader(final int i, final Bundle bundle) {
        return new ServiceLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<IRCService> ircBinderLoader,
            final IRCService service) {
        mService = service;
        mCallback.onServiceConnected(mService);
    }

    @Override
    public void onLoaderReset(final Loader<IRCService> listLoader) {
    }

    public IRCService getService() {
        return mService;
    }

    public void disconnectFromServer(final Server server) {
        mService.requestDisconnectionFromServer(server);
    }

    public interface Callback {

        public void onServiceConnected(final IRCService service);
    }
}
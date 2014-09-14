package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.util.SharedPreferencesUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;

/**
 * Class where all the IRC work is carried out
 */
public class WorkerFragment extends Fragment {

    private IRCService mService;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder binder) {
            final IRCService.IRCBinder serviceBinder = (IRCService.IRCBinder) binder;
            mService = serviceBinder.getService();
            mCallback.onServiceConnected(serviceBinder.getService());
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
        }
    };

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = (Callback) activity;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        if (SharedPreferencesUtils.isInitialDatabaseRun(getActivity())) {
            final AsyncTask<Void, Void, Void> asyncTask = new DatabaseUpdateTask();
            asyncTask.execute();
        } else if (mService == null) {
            bindToService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unbindService(mConnection);
    }

    private void bindToService() {
        final Intent service = new Intent(getActivity(), IRCService.class);
        getActivity().startService(service);
        getActivity().bindService(service, mConnection, 0);
    }

    public IRCService getService() {
        return mService;
    }

    public interface Callback {

        public void onServiceConnected(final IRCService service);
    }

    private class DatabaseUpdateTask extends AsyncTask<Void, Void, Void> {

        private Handler mHandler;

        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mHandler = new Handler();
            mDialog = new ProgressDialog(getActivity());
            mDialog.setTitle("Please wait");
            mDialog.setMessage("Updating data to new format");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (SharedPreferencesUtils.isInitialDatabaseRun(getActivity())) {
                mHandler.post(mDialog::show);
                SharedPreferencesUtils.onInitialSetup(getActivity());
                mHandler.post(mDialog::dismiss);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mService == null) {
                bindToService();
            }
        }
    }
}
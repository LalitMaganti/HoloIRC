package com.fusionx.lightirc.loader;

import com.fusionx.lightirc.communication.NewIRCService;
import com.fusionx.lightirc.model.WrappedServerListItem;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.relay.ServerConfiguration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServiceLoader extends Loader<NewIRCService> {

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder binder) {
            final NewIRCService service = ((NewIRCService.NewIRCBinder) binder).getService();
            deliverResult(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public ServiceLoader(final Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        final Intent service = new Intent(getContext(), NewIRCService.class);
        getContext().startService(service);
        getContext().bindService(service, mConnection, 0);
    }
}
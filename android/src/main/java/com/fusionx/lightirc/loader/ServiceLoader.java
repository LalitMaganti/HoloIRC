package com.fusionx.lightirc.loader;

import com.fusionx.lightirc.service.IRCService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.Loader;

public class ServiceLoader extends Loader<IRCService> {

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder binder) {
            final IRCService.IRCBinder serviceBinder = (IRCService.IRCBinder) binder;
            final IRCService service = serviceBinder.getService();
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
        final Context context = getContext();
        final Intent service = new Intent(context, IRCService.class);
        context.startService(service);
        context.bindService(service, mConnection, 0);
    }
}
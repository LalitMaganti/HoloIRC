package com.fusionx.lightirc.loader;

import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.model.ServerWrapper;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.relay.ServerConfiguration;

import android.content.Context;

import java.util.ArrayList;

public class ServerWrapperLoader extends AbstractLoader<ArrayList<ServerWrapper>> {

    private final IRCService mService;

    public ServerWrapperLoader(Context context, final IRCService service) {
        super(context);

        mService = service;
    }

    @Override
    public ArrayList<ServerWrapper> loadInBackground() {
        final ArrayList<ServerWrapper> listItems = new ArrayList<>();
        final BuilderDatabaseSource source = new BuilderDatabaseSource(getContext());

        source.open();
        for (final ServerConfiguration.Builder builder : source.getAllBuilders()) {
            listItems.add(new ServerWrapper(builder, mService.getServerIfExists(builder)));
        }
        source.close();

        return listItems;
    }
}

package com.fusionx.lightirc.loader;

import com.fusionx.lightirc.model.ServerWrapper;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.lightirc.service.IRCService;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import co.fusionx.relay.Server;
import co.fusionx.relay.ServerConfiguration;

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
            final Server server = mService.getServerIfExists(builder);
            final Collection<String> ignoreList = source.getIgnoreListByName(builder.getTitle());
            final ServerWrapper wrapper = new ServerWrapper(builder, ignoreList, server);
            listItems.add(wrapper);
        }
        source.close();

        return listItems;
    }
}

package com.fusionx.lightirc.loader;

import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.model.ServerWrapper;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

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
            final List<String> ignoreList = source.getIgnoreListByName(builder.getTitle());
            final ServerWrapper wrapper = new ServerWrapper(builder, ignoreList, server);
            listItems.add(wrapper);
        }
        source.close();

        return listItems;
    }
}

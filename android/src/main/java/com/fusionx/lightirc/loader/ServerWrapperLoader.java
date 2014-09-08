package com.fusionx.lightirc.loader;

import com.google.common.base.Optional;

import com.fusionx.lightirc.model.ConnectionContainer;
import com.fusionx.lightirc.model.db.ServerDatabase;
import com.fusionx.lightirc.service.IRCService;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import co.fusionx.relay.base.ConnectionConfiguration;
import co.fusionx.relay.base.Session;

public class ServerWrapperLoader extends AbstractLoader<ArrayList<ConnectionContainer>> {

    private final IRCService mService;

    public ServerWrapperLoader(final Context context, final IRCService service) {
        super(context);

        mService = service;
    }

    @Override
    public ArrayList<ConnectionContainer> loadInBackground() {
        final ArrayList<ConnectionContainer> listItems = new ArrayList<>();
        final ServerDatabase source = ServerDatabase.getInstance(getContext());

        for (final ConnectionConfiguration.Builder builder : source.getAllBuilders()) {
            final Optional<Session> connection = mService.getConnectionIfExists(builder);
            final Collection<String> ignoreList = source.getIgnoreListByName(builder.getTitle());

            final ConnectionContainer container  = new ConnectionContainer(builder, ignoreList,
                    connection);
            listItems.add(container);
        }

        return listItems;
    }
}

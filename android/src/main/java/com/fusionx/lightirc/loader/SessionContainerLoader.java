package com.fusionx.lightirc.loader;

import com.google.common.base.Optional;

import com.fusionx.lightirc.model.SessionContainer;
import com.fusionx.lightirc.model.db.ServerDatabase;
import com.fusionx.lightirc.service.IRCService;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import co.fusionx.relay.core.ConnectionConfiguration;
import co.fusionx.relay.core.Session;

public class SessionContainerLoader extends AbstractLoader<ArrayList<SessionContainer>> {

    public SessionContainerLoader(final Context context) {
        super(context);
    }

    @Override
    public ArrayList<SessionContainer> loadInBackground() {
        final ArrayList<SessionContainer> listItems = new ArrayList<>();
        final ServerDatabase source = ServerDatabase.getInstance(getContext());

        for (final ConnectionConfiguration.Builder builder : source.getAllBuilders()) {
            final Optional<Session> connection = IRCService.getConnectionIfExists(builder);
            final Collection<String> ignoreList = source.getIgnoreListByName(builder.getTitle());

            final SessionContainer container  = new SessionContainer(builder, ignoreList,
                    connection);
            listItems.add(container);
        }

        return listItems;
    }
}

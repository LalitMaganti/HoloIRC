package com.fusionx.lightirc.loader;

import com.google.common.base.Optional;

import com.fusionx.lightirc.model.SessionContainer;
import com.fusionx.lightirc.model.db.BuilderDatabase;
import com.fusionx.lightirc.service.IRCService;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import co.fusionx.relay.core.ConnectionConfiguration;
import co.fusionx.relay.core.Session;

public class SessionContainerLoader extends AbstractLoader<List<SessionContainer>> {

    public SessionContainerLoader(final Context context) {
        super(context);
    }

    @Override
    public List<SessionContainer> loadInBackground() {
        final List<SessionContainer> listItems = new ArrayList<>();
        final BuilderDatabase source = BuilderDatabase.getInstance(getContext());

        for (final ConnectionConfiguration.Builder builder : source.getAllBuilders()) {
            final Optional<Session> connection = IRCService.getConnectionIfExists(builder);
            final SessionContainer container = new SessionContainer(builder, connection);
            listItems.add(container);
        }

        return listItems;
    }
}

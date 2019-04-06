package com.fusionx.lightirc.loader;

import com.fusionx.lightirc.model.ServerConversationContainer;
import com.fusionx.lightirc.model.db.ServerDatabase;
import com.fusionx.lightirc.service.IRCService;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import co.fusionx.relay.base.Server;
import co.fusionx.relay.base.ServerConfiguration;

public class ServerWrapperLoader extends AbstractLoader<ArrayList<ServerConversationContainer>> {

    private final IRCService mService;

    public ServerWrapperLoader(final Context context, final IRCService service) {
        super(context);

        mService = service;
    }

    @Override
    public ArrayList<ServerConversationContainer> loadInBackground() {
        final ArrayList<ServerConversationContainer> listItems = new ArrayList<>();
        final ServerDatabase source = ServerDatabase.getInstance(getContext());
        for (final ServerConfiguration.Builder builder : source.getAllBuilders()) {
             final Server server = mService.getServerIfExists(builder);
            final Collection<String> ignoreList = source.getIgnoreListByName(builder.getTitle());
            final ServerConversationContainer wrapper = new ServerConversationContainer(builder,
                    ignoreList, server);
            listItems.add(wrapper);
        }
        return listItems;
    }
}

package com.fusionx.lightirc.collections;

import com.fusionx.lightirc.irc.ServerConfiguration;

import java.util.ArrayList;

public class BuilderList extends ArrayList<ServerConfiguration.Builder> {
    public boolean remove(String name) {
        for (ServerConfiguration.Builder builder : this) {
            if (builder.getTitle().equalsIgnoreCase(name)) {
                return super.remove(builder);
            }
        }
        return false;
    }
}
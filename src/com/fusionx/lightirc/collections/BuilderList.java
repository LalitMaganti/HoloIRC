package com.fusionx.lightirc.collections;

import com.fusionx.irc.ServerConfiguration;

import java.util.ArrayList;

public class BuilderList extends ArrayList<ServerConfiguration.Builder> {
    public ArrayList<String> getListOfTitles() {
        final ArrayList<String> listOfTitles = new ArrayList<>();
        for(ServerConfiguration.Builder builder : this) {
            listOfTitles.add(builder.getTitle());
        }
        return listOfTitles;
    }
}

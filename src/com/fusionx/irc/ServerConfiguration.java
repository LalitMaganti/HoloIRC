/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.irc;

import android.os.Parcel;
import android.os.Parcelable;

import com.fusionx.irc.misc.NickStorage;

import java.util.ArrayList;

import lombok.Data;

@Data
public class ServerConfiguration {
    private final String title;
    private final String url;
    private final int port;
    private final boolean ssl;

    private final NickStorage nickStorage;
    private final String realName;
    private final boolean nickChangable;

    private final String serverUserName;
    private final String serverPassword;

    private final String saslUsername;
    private final String saslPassword;

    private final String nickservPassword;

    private final ArrayList<String> autoJoinChannels;

    private ServerConfiguration(final Builder builder) {
        title = builder.getTitle();
        url = builder.getUrl();
        port = builder.getPort();
        ssl = builder.isSsl();

        nickStorage = builder.getNickStorage();
        realName = builder.getRealName();
        nickChangable = builder.isNickChangeable();

        serverUserName = builder.getServerUserName();
        serverPassword = builder.getServerPassword();

        saslUsername = builder.getSaslUsername();
        saslPassword = builder.getSaslPassword();

        nickservPassword = builder.getNickservPassword();

        autoJoinChannels = builder.getAutoJoinChannels();
    }

    @Data
    public static class Builder implements Parcelable {
        private String file;

        private String title;
        private String url;
        private int port;
        private boolean ssl;

        private NickStorage nickStorage;
        private String realName;
        private boolean nickChangeable;

        private String serverUserName;
        private String serverPassword;

        private String saslUsername;
        private String saslPassword;

        private String nickservPassword;

        private ArrayList<String> autoJoinChannels = new ArrayList<>();

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(file);

            out.writeString(title);
            out.writeString(url);
            out.writeInt(port);
            out.writeInt(ssl ? 1 : 0);

            out.writeParcelable(nickStorage, 0);
            out.writeString(realName);
            out.writeInt(nickChangeable ? 1 : 0);

            out.writeString(serverUserName);
            out.writeString(serverPassword);

            out.writeString(saslUsername);
            out.writeString(saslPassword);

            out.writeString(nickservPassword);

            out.writeStringList(autoJoinChannels);
        }

        public static final Parcelable.Creator<Builder> CREATOR =
                new Parcelable.Creator<Builder>() {
                    public Builder createFromParcel(Parcel in) {
                        return new Builder(in);
                    }

                    public Builder[] newArray(int size) {
                        return new Builder[size];
                    }
                };

        public Builder() {
        }

        private Builder(final Parcel in) {
            file = in.readString();

            title = in.readString();
            url = in.readString();
            port = in.readInt();
            ssl = in.readInt() == 1;

            nickStorage = in.readParcelable(NickStorage.class.getClassLoader());
            realName = in.readString();
            nickChangeable = in.readInt() == 1;

            serverUserName = in.readString();
            serverPassword = in.readString();

            saslUsername = in.readString();
            saslPassword = in.readString();

            nickservPassword = in.readString();

            in.readStringList(autoJoinChannels);
        }

        public ServerConfiguration build() {
            return new ServerConfiguration(this);
        }
    }
}

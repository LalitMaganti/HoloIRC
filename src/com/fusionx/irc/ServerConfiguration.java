package com.fusionx.irc;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import lombok.Data;

@Data
public class ServerConfiguration {
    private final String title;
    private final String url;
    private final int port;
    private final boolean ssl;

    private final String nick;
    private final String realName;

    private final String serverUserName;
    private final String serverPassword;

    private final String nickservPassword;

    private final ArrayList<String> autoJoinChannels;

    public ServerConfiguration(final Builder builder) {
        title = builder.getTitle();
        url = builder.getUrl();
        port = builder.getPort();
        ssl = builder.isSsl();

        nick = builder.getNick();
        realName = builder.getRealName();

        serverUserName = builder.getServerUserName();
        serverPassword = builder.getServerPassword();

        nickservPassword = builder.getNickservPassword();

        autoJoinChannels = null;
    }

    @Data
    public static class Builder implements Parcelable {
        private String file;

        private String title;
        private String url;
        private int port;
        private boolean ssl;

        private String nick;
        private String realName;

        private String serverUserName;
        private String serverPassword;

        private String nickservPassword;

        private ArrayList<String> autoJoinChannels = new ArrayList<>();

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(title);

            out.writeString(file);

            out.writeString(url);
            out.writeInt(port);

            out.writeString(nick);
            out.writeString(realName);

            out.writeString(serverUserName);
            out.writeString(serverPassword);

            out.writeString(nickservPassword);

            out.writeStringList(autoJoinChannels);
        }

        public static final Parcelable.Creator<Builder> CREATOR = new Parcelable.Creator<Builder>() {
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };

        public Builder() {
        }

        private Builder(Parcel in) {
            title = in.readString();

            file = in.readString();

            url = in.readString();
            port = in.readInt();

            nick = in.readString();
            realName = in.readString();

            serverUserName = in.readString();
            serverPassword = in.readString();

            nickservPassword = in.readString();

            in.readStringList(autoJoinChannels);
        }

        public ServerConfiguration build() {
            return new ServerConfiguration(this);
        }
    }
}

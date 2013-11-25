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

package com.fusionx.lightirc.irc;

import org.apache.commons.lang3.StringUtils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ServerConfiguration {

    private final String mTitle;

    private final String mUrl;

    private final int mPort;

    private final boolean mSsl;

    private final boolean mSslAcceptAllCertificates;

    private final NickStorage mNickStorage;

    private final String mRealName;

    private final boolean mNickChangable;

    private final String mServerUserName;

    private final String mServerPassword;

    private final String mSaslUsername;

    private final String mSaslPassword;

    private final String mNickservPassword;

    private final ArrayList<String> mAutoJoinChannels;

    private ServerConfiguration(final Builder builder) {
        mTitle = builder.getTitle();
        mUrl = builder.getUrl();
        mPort = builder.getPort();

        mSsl = builder.isSsl();
        mSslAcceptAllCertificates = builder.isSslAcceptAllCertificates();

        mNickStorage = builder.getNickStorage();
        mRealName = builder.getRealName();
        mNickChangable = builder.isNickChangeable();

        mServerUserName = builder.getServerUserName();
        mServerPassword = builder.getServerPassword();

        mSaslUsername = builder.getSaslUsername();
        mSaslPassword = builder.getSaslPassword();

        mNickservPassword = builder.getNickservPassword();

        mAutoJoinChannels = builder.getAutoJoinChannels();
    }

    public boolean isSaslAvailable() {
        return StringUtils.isNotEmpty(mSaslUsername) && StringUtils.isNotEmpty(mSaslPassword);
    }

    public static class Builder implements Parcelable {

        // For app use only
        private String mFile;

        private String mTitle;

        private String mUrl;

        private int mPort;

        private boolean mSsl;

        private boolean mSslAcceptAllCertificates;

        private NickStorage mNickStorage;

        private String mRealName;

        private boolean mNickChangeable;

        private String mServerUserName;

        private String mServerPassword;

        private String mSaslUsername;

        private String mSaslPassword;

        private String mNickservPassword;

        private final ArrayList<String> mAutoJoinChannels = new ArrayList<String>();

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(mFile);

            out.writeString(mTitle);
            out.writeString(mUrl);
            out.writeInt(mPort);

            out.writeInt(mSsl ? 1 : 0);
            out.writeInt(mSslAcceptAllCertificates ? 1 : 0);

            out.writeParcelable(mNickStorage, 0);
            out.writeString(mRealName);
            out.writeInt(mNickChangeable ? 1 : 0);

            out.writeString(mServerUserName);
            out.writeString(mServerPassword);

            out.writeString(mSaslUsername);
            out.writeString(mSaslPassword);

            out.writeString(mNickservPassword);

            out.writeStringList(mAutoJoinChannels);
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
            mFile = in.readString();

            mTitle = in.readString();
            mUrl = in.readString();
            mPort = in.readInt();

            mSsl = in.readInt() == 1;
            mSslAcceptAllCertificates = in.readInt() == 1;

            mNickStorage = in.readParcelable(NickStorage.class.getClassLoader());
            mRealName = in.readString();
            mNickChangeable = in.readInt() == 1;

            mServerUserName = in.readString();
            mServerPassword = in.readString();

            mSaslUsername = in.readString();
            mSaslPassword = in.readString();

            mNickservPassword = in.readString();

            in.readStringList(mAutoJoinChannels);
        }

        public ServerConfiguration build() {
            return new ServerConfiguration(this);
        }

        // Getters and setters
        public String getFile() {
            return mFile;
        }

        public void setFile(String file) {
            this.mFile = file;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public String getUrl() {
            return mUrl;
        }

        public void setUrl(String url) {
            this.mUrl = url;
        }

        public int getPort() {
            return mPort;
        }

        public void setPort(int port) {
            this.mPort = port;
        }

        public boolean isSsl() {
            return mSsl;
        }

        public void setSsl(boolean ssl) {
            this.mSsl = ssl;
        }

        public boolean isSslAcceptAllCertificates() {
            return mSslAcceptAllCertificates;
        }

        public void setSslAcceptAllCertificates(boolean sslAcceptAllCertificates) {
            this.mSslAcceptAllCertificates = sslAcceptAllCertificates;
        }

        public NickStorage getNickStorage() {
            return mNickStorage;
        }

        public void setNickStorage(NickStorage nickStorage) {
            this.mNickStorage = nickStorage;
        }

        public String getRealName() {
            return mRealName;
        }

        public void setRealName(String realName) {
            this.mRealName = realName;
        }

        public boolean isNickChangeable() {
            return mNickChangeable;
        }

        public void setNickChangeable(boolean nickChangeable) {
            this.mNickChangeable = nickChangeable;
        }

        public String getServerUserName() {
            return mServerUserName;
        }

        public void setServerUserName(String serverUserName) {
            this.mServerUserName = serverUserName;
        }

        public String getServerPassword() {
            return mServerPassword;
        }

        public void setServerPassword(String serverPassword) {
            this.mServerPassword = serverPassword;
        }

        public String getSaslUsername() {
            return mSaslUsername;
        }

        public void setSaslUsername(String saslUsername) {
            this.mSaslUsername = saslUsername;
        }

        public String getSaslPassword() {
            return mSaslPassword;
        }

        public void setSaslPassword(String saslPassword) {
            this.mSaslPassword = saslPassword;
        }

        public String getNickservPassword() {
            return mNickservPassword;
        }

        public void setNickservPassword(String nickservPassword) {
            this.mNickservPassword = nickservPassword;
        }

        public ArrayList<String> getAutoJoinChannels() {
            return mAutoJoinChannels;
        }
    }

    public static class NickStorage implements Parcelable {

        private String mFirstChoiceNick = "HoloIRCUser";

        private String mSecondChoiceNick = "";

        private String mThirdChoiceNick = "";

        public NickStorage(final String firstChoice, final String secondChoice,
                final String thirdChoice) {
            mFirstChoiceNick = firstChoice;
            mSecondChoiceNick = secondChoice;
            mThirdChoiceNick = thirdChoice;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mFirstChoiceNick);
            parcel.writeString(mSecondChoiceNick);
            parcel.writeString(mThirdChoiceNick);
        }

        public static final Parcelable.Creator<NickStorage> CREATOR = new Parcelable
                .Creator<NickStorage>() {
            public NickStorage createFromParcel(Parcel in) {
                return new NickStorage(in.readString(), in.readString(), in.readString());
            }

            public NickStorage[] newArray(int size) {
                return new NickStorage[size];
            }
        };

        // Getters and setters
        public String getFirstChoiceNick() {
            return mFirstChoiceNick;
        }

        public String getSecondChoiceNick() {
            return mSecondChoiceNick;
        }

        public String getThirdChoiceNick() {
            return mThirdChoiceNick;
        }
    }

    // Getters and setters
    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public int getPort() {
        return mPort;
    }

    public boolean isSsl() {
        return mSsl;
    }

    public boolean isSslAcceptAllCertificates() {
        return mSslAcceptAllCertificates;
    }

    public NickStorage getNickStorage() {
        return mNickStorage;
    }

    public String getRealName() {
        return mRealName;
    }

    public boolean isNickChangable() {
        return mNickChangable;
    }

    public String getServerUserName() {
        return mServerUserName;
    }

    public String getServerPassword() {
        return mServerPassword;
    }

    public String getSaslUsername() {
        return mSaslUsername;
    }

    public String getSaslPassword() {
        return mSaslPassword;
    }

    public String getNickservPassword() {
        return mNickservPassword;
    }

    public ArrayList<String> getAutoJoinChannels() {
        return mAutoJoinChannels;
    }
}

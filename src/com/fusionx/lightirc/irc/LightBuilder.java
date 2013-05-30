/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.irc;

import android.os.Parcel;
import android.os.Parcelable;
import org.pircbotx.Configuration.Builder;

import java.util.ArrayList;

public class LightBuilder extends Builder implements Parcelable {
    private String mTitle;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    // Parcelable stuff
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeString(getTitle());
        dest.writeString(getServerHostname());
        dest.writeString(getName());
        dest.writeString(getLogin());
        dest.writeString(getServerPassword());
        dest.writeString(getNickservPassword());

        ArrayList<String> list = new ArrayList<String>(getAutoJoinChannels()
                .keySet());
        dest.writeStringList(list);
    }

    private void readFromParcel(Parcel in) {
        setTitle(in.readString());
        setServerHostname(in.readString());
        setName(in.readString());
        setLogin(in.readString());
        setServerPassword(in.readString());
        setNickservPassword(in.readString());

        ArrayList<String> list = new ArrayList<String>();
        in.readStringList(list);
        for (String s : list) {
            addAutoJoinChannel(s);
        }
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public LightBuilder createFromParcel(Parcel in) {
            LightBuilder c = new LightBuilder();
            c.readFromParcel(in);
            return c;
        }

        @Override
        public LightBuilder[] newArray(int size) {
            return new LightBuilder[size];
        }
    };
}

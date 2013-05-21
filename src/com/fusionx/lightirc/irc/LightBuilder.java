package com.fusionx.lightirc.irc;

import android.os.Parcel;
import android.os.Parcelable;
import org.pircbotx.Configuration.Builder;

import java.util.ArrayList;
import java.util.HashMap;

public class LightBuilder extends Builder implements Parcelable {
    public String mTitle;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public HashMap<String, String> toHashMap() {
        HashMap<String, String> nameIcons = new HashMap<String, String>();
        nameIcons.put("url", getServerHostname());
        nameIcons.put("userName", getLogin());
        nameIcons.put("nick", getName());
        nameIcons.put("serverPassword", getServerPassword());
        nameIcons.put("title", getTitle());

        return nameIcons;
    }

    // Parcelable stuff
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeString(getServerHostname());
        dest.writeString(getLogin());
        dest.writeString(getName());
        dest.writeString(getServerPassword());
        dest.writeString(getTitle());

        ArrayList<String> list = new ArrayList<String>(getAutoJoinChannels()
                .keySet());
        dest.writeStringList(list);
    }

    private void readFromParcel(Parcel in) {
        setServerHostname(in.readString());
        setLogin(in.readString());
        setName(in.readString());
        setServerPassword(in.readString());
        setTitle(in.readString());

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

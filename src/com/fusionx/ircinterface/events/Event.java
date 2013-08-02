package com.fusionx.ircinterface.events;

import android.os.Parcel;
import android.os.Parcelable;
import lombok.Data;

@Data
public class Event implements Parcelable {
    private String destination;
    private Enum type;
    private String[] message;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(destination);
        out.writeSerializable(type);
        out.writeStringArray(message);
    }

    public final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public Event() {
    }

    private Event(Parcel in) {
        destination = in.readString();
        type = (Enum) in.readSerializable();
        in.readStringArray(message);
    }
}

package com.fusionx.irc.misc;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class NickStorage implements Parcelable {
    private String firstChoiceNick = "HoloIRCUser";
    private String secondChoiceNick = "";
    private String thirdChoiceNick = "";

    public NickStorage(final String firstChoice, final String secondChoice,
                       final String thirdChoice) {
        firstChoiceNick = firstChoice;
        secondChoiceNick = secondChoice;
        thirdChoiceNick = thirdChoice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(firstChoiceNick);
        parcel.writeString(secondChoiceNick);
        parcel.writeString(thirdChoiceNick);
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
}

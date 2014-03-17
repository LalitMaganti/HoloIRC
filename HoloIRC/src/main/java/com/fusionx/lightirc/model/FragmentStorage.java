package com.fusionx.lightirc.model;

import com.fusionx.lightirc.misc.FragmentType;

import android.os.Parcel;
import android.os.Parcelable;

public class FragmentStorage implements Parcelable {

    public static final Parcelable.Creator<FragmentStorage> CREATOR =
            new Parcelable.Creator<FragmentStorage>() {
                public FragmentStorage createFromParcel(Parcel in) {
                    return new FragmentStorage(in);
                }

                public FragmentStorage[] newArray(int size) {
                    return new FragmentStorage[size];
                }
            };

    private final String mTitle;

    private final FragmentType mFragmentType;

    public FragmentStorage(final String title, final FragmentType typeEnum) {
        mTitle = title;
        mFragmentType = typeEnum;
    }

    private FragmentStorage(final Parcel in) {
        mTitle = in.readString();
        mFragmentType = (FragmentType) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeSerializable(mFragmentType);
    }

    public String getTitle() {
        return mTitle;
    }

    public FragmentType getFragmentType() {
        return mFragmentType;
    }
}
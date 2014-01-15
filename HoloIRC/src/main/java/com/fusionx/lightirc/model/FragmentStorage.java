package com.fusionx.lightirc.model;

import com.fusionx.lightirc.constants.FragmentTypeEnum;

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

    private final FragmentTypeEnum mFragmentTypeEnum;

    public FragmentStorage(final String title, final FragmentTypeEnum typeEnum) {
        mTitle = title;
        mFragmentTypeEnum = typeEnum;
    }

    private FragmentStorage(final Parcel in) {
        mTitle = in.readString();
        mFragmentTypeEnum = (FragmentTypeEnum) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeSerializable(mFragmentTypeEnum);
    }

    public String getTitle() {
        return mTitle;
    }

    public FragmentTypeEnum getFragmentTypeEnum() {
        return mFragmentTypeEnum;
    }
}
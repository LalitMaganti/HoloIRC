package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.misc.FragmentType;

import android.support.v4.app.Fragment;

public abstract class BaseIRCFragment extends Fragment {

    public abstract FragmentType getType();

    public abstract boolean isValid();
}
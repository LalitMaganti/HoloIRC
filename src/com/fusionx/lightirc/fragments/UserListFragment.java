package com.fusionx.lightirc.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.fusionx.lightirc.adapters.UserListAdapter;

public class UserListFragment extends ListFragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final UserListAdapter adapter = new UserListAdapter(inflater.getContext());
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
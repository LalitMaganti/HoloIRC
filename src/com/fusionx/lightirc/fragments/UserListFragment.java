package com.fusionx.lightirc.fragments;

import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.fusionx.lightirc.R;

import java.util.ArrayList;

public class UserListFragment extends ListFragment {
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                inflater.getContext(),
                R.layout.layout_text_list, new ArrayList<String>());
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}

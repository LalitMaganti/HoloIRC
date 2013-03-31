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

package com.fusionx.lightirc.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.fusionx.lightirc.R;

import java.util.ArrayList;

public class ListViewSettingsFragment extends ListFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ArrayList<String> personList = new ArrayList<String>();

        final ArrayAdapter<String> ds = new ArrayAdapter<String>(getActivity()
                .getApplicationContext(), R.layout.layout_simple_list,
                personList);
        ds.add("#testingircandroid");

        setListAdapter(ds);
    }
}

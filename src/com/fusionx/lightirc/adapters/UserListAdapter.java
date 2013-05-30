package com.fusionx.lightirc.adapters;


import android.content.Context;
import android.widget.ArrayAdapter;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.UserComparator;

import java.util.ArrayList;

public class UserListAdapter extends ArrayAdapter<String> {
    public UserListAdapter(Context context) {
        super(context, R.layout.layout_text_list, new ArrayList<String>());
    }

    public void replace(String old, String newString) {
        super.remove(old);
        super.add(newString);
        super.sort(new UserComparator());
        notifyDataSetChanged();
    }

    public void sort() {
        super.sort(new UserComparator());
    }
}
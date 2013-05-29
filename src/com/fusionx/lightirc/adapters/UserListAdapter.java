package com.fusionx.lightirc.adapters;


import android.content.Context;
import android.widget.ArrayAdapter;
import com.fusionx.lightirc.R;

import java.util.ArrayList;
import java.util.Comparator;

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

    public class UserComparator implements Comparator<String> {
        @Override
        public int compare(String s, String s2) {
            if (s.startsWith(s2.substring(0, 1)) && (s.substring(0, 1).equals("@")
                    || s.substring(0, 1).equals("+"))) {
                return 0;
            } else if (s.startsWith("@")) {
                return 1;
            } else if (s2.startsWith("@")) {
                return -1;
            } else if (s.startsWith("+")) {
                return 1;
            } else if (s2.startsWith("+")) {
                return -1;
            }
            return s.compareTo(s2);
        }
    }
}
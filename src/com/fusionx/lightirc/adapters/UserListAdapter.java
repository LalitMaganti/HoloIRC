package com.fusionx.lightirc.adapters;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.UserComparator;

import java.util.ArrayList;

public class UserListAdapter extends SelectionAdapter {
    public UserListAdapter(Context context, ArrayList<String> array) {
        super(context, array);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(R.id.text1);
        textView.setText(Html.fromHtml(getItem(position)));
        return view;
    }
}
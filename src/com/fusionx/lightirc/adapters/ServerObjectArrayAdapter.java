package com.fusionx.lightirc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.ServerObject;

public class ServerObjectArrayAdapter extends ArrayAdapter<ServerObject> {
	private final Context context;
	private final ServerObject[] values;

	public ServerObjectArrayAdapter(Context context, ServerObject[] values) {
		super(context, R.layout.listlayout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.listlayout, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.text);
		textView.setText(values[position].url);

		return rowView;
	}
}
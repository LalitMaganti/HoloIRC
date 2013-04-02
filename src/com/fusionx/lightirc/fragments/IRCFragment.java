package com.fusionx.lightirc.fragments;

import com.fusionx.lightirc.R;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public abstract class IRCFragment extends Fragment {
	protected String tabTitle;

	public String getTitle() {
		return tabTitle;
	}

	protected void writeToTextView(String text) {
		writeToTextView(text, getView());
	}

	protected void writeToTextView(String text, View rootView) {
		final TextView textView = (TextView) rootView
				.findViewById(R.id.textview);
		final ScrollView scrollView = (ScrollView) rootView
				.findViewById(R.id.scrollview);
		textView.append(text);
		scrollView.fullScroll(View.FOCUS_DOWN);
	}
}
package com.fusionx.lightirc.fragments;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fusionx.lightirc.R;

public abstract class IRCFragment extends Fragment {
	private String tabTitle;

	public String getTitle() {
		return tabTitle;
	}

	public void setTitle(String title) {
		tabTitle = title;
	}

	public void writeToTextView(final String text) {
		writeToTextView(text, getView());
	}

	protected void writeToTextView(final String text, final View rootView) {
		final TextView textView = (TextView) rootView
				.findViewById(R.id.textview);
		final ScrollView scrollView = (ScrollView) rootView
				.findViewById(R.id.scrollview);
		textView.append(text);
		scrollView.fullScroll(View.FOCUS_DOWN);
	}
}
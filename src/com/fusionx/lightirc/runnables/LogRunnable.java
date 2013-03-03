package com.fusionx.lightirc.runnables;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fusionx.lightirc.R;

public abstract class LogRunnable implements Runnable {
	private final String modLine;
	private final View vi;

	public LogRunnable(String line, View d) {
		modLine = line;
		vi = d;
	}

	@Override
	public void run() {
		final TextView textView = (TextView) vi.findViewById(R.id.textview);
		final ScrollView scrollView = (ScrollView) vi
				.findViewById(R.id.scrollview);
		textView.append(modLine + "\n");
		scrollView.fullScroll(View.FOCUS_DOWN);
	}
}

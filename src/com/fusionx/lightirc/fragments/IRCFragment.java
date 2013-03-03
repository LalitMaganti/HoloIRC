package com.fusionx.lightirc.fragments;

import android.support.v4.app.Fragment;

public abstract class IRCFragment extends Fragment {
	protected String tabTitle;

	public String getTitle() {
		return tabTitle;
	}
}
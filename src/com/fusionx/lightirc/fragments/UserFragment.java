package com.fusionx.lightirc.fragments;

import java.util.ArrayList;

import com.fusionx.lightirc.R;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ArrayAdapter;

public class UserFragment extends ListFragment implements
		MultiChoiceModeListener, android.view.ActionMode.Callback {
	public ArrayList<String> lst = new ArrayList<String>();
	public ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.layout_simple_list, lst);
		setListAdapter(adapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setMultiChoiceModeListener(this);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_user_list_mention:
			// TODO - mention here
			mode.finish();
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// Inflate a menu resource providing context menu items
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.user_list, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		mode.setTitle(getListView().getCheckedItemCount() + " items selected");
	}
}

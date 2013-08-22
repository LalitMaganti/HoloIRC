package com.fusionx.lightirc.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.actions.IgnoreAdapter;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.PreferenceKeys;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.HashSet;

public class IgnoreListFragment extends ListFragment implements ActionMode.Callback,
        SlidingMenu.OnCloseListener {
    private IgnoreListCallback mCallbacks;

    private ActionMode mMode;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (IgnoreListCallback) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() +
                    " must implement IgnoreListCallback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ArrayList<String> arrayList = new ArrayList<>(getActivity().getSharedPreferences
                (mCallbacks.getServerTitle().toLowerCase(), Context.MODE_PRIVATE).getStringSet
                (PreferenceKeys.IgnoreList, new HashSet<String>()));
        final View serverHeader = inflater.inflate(R.layout.sliding_menu_header, null);
        final TextView textView = (TextView) serverHeader.findViewById(R.id
                .sliding_menu_heading_textview);
        textView.setText("Ignore List");

        final MergeAdapter adapter = new MergeAdapter();
        final IgnoreAdapter ignoreAdapter = new IgnoreAdapter(getActivity(), arrayList);

        adapter.addView(serverHeader);
        adapter.addAdapter(ignoreAdapter);

        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.ignore_list_cab, menu);

        mMode = actionMode;

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mCallbacks.switchToIRCActionFragment();
        mMode = null;
    }

    @Override
    public void onClose() {
        if(mMode != null) {
            mMode.finish();
        }
    }

    public interface IgnoreListCallback extends CommonCallbacks {
        public void switchToIRCActionFragment();

    }
}
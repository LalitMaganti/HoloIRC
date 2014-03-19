package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.Server;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ActionsPagerFragment extends Fragment implements IgnoreListFragment
        .IgnoreListCallback, ActionsFragment.Callbacks {

    private ActionsFragment mActionFragment;

    private IgnoreListFragment mIgnoreListFragment;

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (Callback) activity;
        } catch (final ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final FrameLayout layout = new FrameLayout(getActivity());
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setId(R.id.card_server_content);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            mActionFragment = new ActionsFragment();
            mIgnoreListFragment = new IgnoreListFragment();
            final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.card_server_content, mActionFragment, "Actions").commit();
        } else {
            mActionFragment = (ActionsFragment) getChildFragmentManager().findFragmentByTag(
                    "Actions");
            mIgnoreListFragment = (IgnoreListFragment) getChildFragmentManager().findFragmentByTag(
                    "Ignore");
        }
    }

    @Override
    public void switchToIRCActionFragment() {
        getChildFragmentManager().popBackStackImmediate();
    }

    @Override
    public String getServerTitle() {
        return getServer().getTitle();
    }

    @Override
    public void onRemoveCurrentFragment() {
        mCallback.onRemoveCurrentFragment();
    }

    @Override
    public Server getServer() {
        return mCallback.getServer();
    }

    @Override
    public void closeDrawer() {
        mCallback.closeDrawer();
    }

    @Override
    public void disconnectFromServer() {
        mCallback.disconnectFromServer();
    }

    @Override
    public void switchToIgnoreFragment() {
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.addToBackStack(null);
        transaction.replace(R.id.card_server_content, mIgnoreListFragment, "Ignore").commit();
    }

    public void onConnectionStatusChanged(final ConnectionStatus status) {
        mActionFragment.onConnectionStatusChanged(status);
    }

    public void onDrawerClosed() {
        mIgnoreListFragment.getListener().finish();
    }

    public void onFragmentTypeChanged(final FragmentType type) {
        mActionFragment.onFragmentTypeChanged(type);
    }

    public interface Callback {

        public void onRemoveCurrentFragment();

        public Server getServer();

        public void disconnectFromServer();

        public void closeDrawer();
    }
}
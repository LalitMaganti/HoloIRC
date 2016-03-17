package com.fusionx.lightirc.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.misc.Theme;
import com.fusionx.lightirc.model.db.ServerDatabase;
import com.fusionx.lightirc.ui.dialogbuilder.DialogBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import co.fusionx.relay.base.Conversation;

import static com.fusionx.lightirc.misc.AppPreferences.getAppPreferences;
import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class IgnoredUsersFragment extends AppCompatDialogFragment {

    private final EventHandler mEventHandler = new EventHandler();

    private Conversation mConversation;

    private ServerDatabase mDatabaseSource;

    private IgnoredUsersAdapter mAdapter;

    private RecyclerView mRecyclerView;

    public static IgnoredUsersFragment createInstance() {
        return new IgnoredUsersFragment();
    }

    @SuppressLint("PrivateResource")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseSource = ServerDatabase.getInstance(getActivity());
        mAdapter = new IgnoredUsersAdapter(getActivity(), new DeclineListener());

        setStyle(DialogFragment.STYLE_NO_FRAME, getAppPreferences().getTheme() == Theme.DARK
                ? android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog
                : android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View v = inflater.inflate(R.layout.ignored_users_fragment, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBus().registerSticky(mEventHandler);

        final String title = mConversation.getServer().getTitle();
        final Collection<String> ignoreList = mDatabaseSource.getIgnoreListByName(title);
        final List<String> arrayList = new ArrayList<>(ignoreList);
        mAdapter.addAll(arrayList);
        mRecyclerView.setAdapter(mAdapter);

        final ImageButton button = (ImageButton) view.findViewById(R.id.ignored_users_add);
        button.setOnClickListener(new AddClickListener());
    }

    @Override
    public void onPause() {
        super.onPause();

        saveIgnoreList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getBus().unregister(mEventHandler);
    }

    private void saveIgnoreList() {
        final List<String> list = mAdapter.getItems();
        mDatabaseSource.updateIgnoreList(mConversation.getServer().getTitle(), list);
    }

    private class IgnoreListDialogBuilder extends DialogBuilder {

        public IgnoreListDialogBuilder() {
            super(getActivity(), getString(R.string.ignore_nick_title),
                    getString(R.string.ignore_nick_description), "");
        }

        @Override
        public void onOkClicked(final String input) {
            mAdapter.add(input);
        }
    }

    private class EventHandler {

        @Subscribe
        public void onConversationChanged(final OnConversationChanged conversationChanged) {
            mConversation = conversationChanged.conversation;
        }
    }

    private class AddClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            final IgnoreListDialogBuilder builder = new IgnoreListDialogBuilder();
            builder.show();
        }
    }

    private class DeclineListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            final String string = (String) v.getTag();
            mAdapter.remove(string);
        }
    }
}

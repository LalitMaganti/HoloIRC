package app.holoirc.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.fusionx.bus.Subscribe;
import app.holoirc.R;
import app.holoirc.event.OnConversationChanged;
import app.holoirc.model.db.ServerDatabase;
import app.holoirc.ui.dialogbuilder.DialogBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import app.holoirc.util.MiscUtils;
import co.fusionx.relay.base.Conversation;

public class IgnoredUsersFragment extends AppCompatDialogFragment {

    private final EventHandler mEventHandler = new EventHandler();

    private Conversation mConversation;

    private ServerDatabase mDatabaseSource;

    private IgnoredUsersAdapter mAdapter;

    private RecyclerView mRecyclerView;

    public static IgnoredUsersFragment createInstance() {
        return new IgnoredUsersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseSource = ServerDatabase.getInstance(getActivity());
        mAdapter = new IgnoredUsersAdapter(getActivity(), new DeclineListener());

        setStyle(STYLE_NO_TITLE, 0);
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

        MiscUtils.getBus().registerSticky(mEventHandler);

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

        MiscUtils.getBus().unregister(mEventHandler);
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

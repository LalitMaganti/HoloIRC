package app.holoirc.ui;

import com.fusionx.bus.Subscribe;
import app.holoirc.R;
import app.holoirc.event.OnConversationChanged;
import app.holoirc.event.OnServerStatusChanged;
import app.holoirc.misc.FragmentType;
import app.holoirc.util.UIUtils;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.fusionx.relay.base.ConnectionStatus;

import static app.holoirc.util.MiscUtils.getBus;

public class ActionsAdapter extends RecyclerView.Adapter<ActionsAdapter.ActionViewHolder> {

    private final LayoutInflater mInflater;

    private final List<String> mServerNormalActions;

    private final List<String> mServerConnectedActions;

    private final List<String> mServerDisconnectedActions;

    private final List<String> mChannelActions;

    private final List<String> mUserActions;

    private final Context mContext;

    private final View.OnClickListener mClickListener;

    private ConnectionStatus mStatus = ConnectionStatus.DISCONNECTED;

    private FragmentType mFragmentType = FragmentType.SERVER;

    private List<String> mActions;

    private SimpleSectionedRecyclerViewAdapter mSectionedAdapter;

    public ActionsAdapter(final Context context, final View.OnClickListener clickListener) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mClickListener = clickListener;

        mActions = new ArrayList<>();

        mServerNormalActions = Arrays
                .asList(context.getResources().getStringArray(R.array.server_actions_normal));
        mServerConnectedActions = Arrays
                .asList(context.getResources().getStringArray(R.array.server_actions_connected));
        mServerDisconnectedActions = Arrays
                .asList(context.getResources().getStringArray(R.array.server_actions_disconnected));
        mChannelActions = Arrays.asList(context.getResources().getStringArray(R.array
                .channel_actions));
        mUserActions = Arrays.asList(context.getResources().getStringArray(R.array.user_actions));

        final EventHandler eventHandler = new EventHandler();
        getBus().register(eventHandler);
        final OnConversationChanged event = getBus().getStickyEvent(OnConversationChanged.class);
        if (event == null) {
            return;
        }

        mFragmentType = event.fragmentType;
        if (event.conversation != null) {
            mStatus = event.conversation.getServer().getStatus();
        }
    }

    private void updateActionsList() {
        mActions.clear();

        mSectionedAdapter.setSections(new SimpleSectionedRecyclerViewAdapter.Section[]{});
        if (mFragmentType == null) {
            return;
        }
        final List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();
        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, "Server"));

        if (isConnected()) {
            mActions.addAll(mServerConnectedActions);
        } else if (isDisconnected()) {
            mActions.addAll(mServerDisconnectedActions);
        } else {
            mActions.addAll(mServerNormalActions);
        }

        final int serverCount = mActions.size();
        String sectionTitle = null;
        if (isConnected()) {
            if (mFragmentType == FragmentType.CHANNEL) {
                sectionTitle = "Channel";
                mActions.addAll(mChannelActions);
            } else if (mFragmentType == FragmentType.USER) {
                sectionTitle = "User";
                mActions.addAll(mUserActions);
            }
        } else {
            sectionTitle = null;
        }

        if (sectionTitle != null) {
            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(serverCount, sectionTitle));
        }
        final SimpleSectionedRecyclerViewAdapter.Section[] array = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        sections.toArray(array);
        mSectionedAdapter.setSections(array);

        notifyDataSetChanged();
    }

    public String getItem(int position) {
        return mActions.get(position);
    }

    boolean isConnected() {
        return mStatus == ConnectionStatus.CONNECTED;
    }

    boolean isDisconnected() {
        return mStatus == ConnectionStatus.DISCONNECTED;
    }

    @Override
    public ActionViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = mInflater.inflate(R.layout.action_recycler_item, parent, false);
        final ActionViewHolder holder = new ActionViewHolder(view);
        UIUtils.setRobotoLight(mContext, holder.textView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ActionViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(mClickListener);

        holder.textView.setText(getItem(position));
    }

    @Override
    public int getItemCount() {
        return mActions.size();
    }

    public void setSectionedAdapter(final SimpleSectionedRecyclerViewAdapter sectionedAdapter) {
        mSectionedAdapter = sectionedAdapter;

        // Now we have the child adapter, we can update the list.
        updateActionsList();
    }

    public class ActionViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ActionViewHolder(final View itemView) {
            super(itemView);

            textView = (TextView) itemView;
        }
    }

    private class EventHandler {

        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            mFragmentType = conversationChanged.fragmentType;
            mStatus = conversationChanged.conversation == null ? null
                    : conversationChanged.conversation.getServer().getStatus();
            updateActionsList();
        }

        @Subscribe
        public void onEvent(final OnServerStatusChanged statusChanged) {
            mStatus = statusChanged.status;
            updateActionsList();
        }
    }
}

package com.fusionx.lightirc.adapters;

/*
public class IRCAdapter<T extends Event> extends RecyclerView.Adapter<IRCAdapter.IRCViewHolder>
        implements Filterable {

    private final Object mLock = new Object();

    private final Context mContext;

    private final LayoutInflater mInflater;

    private boolean mShouldFilter;

    private IRCFilter mFilter;

    private List<T> mEvents;

    private EventCache mEventCache;

    public IRCAdapter(final Context context, final EventCache cache, final boolean filter) {
        mContext = context;
        mEvents = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);
        mEventCache = cache;
        mShouldFilter = filter;
    }

    @Override
    public IRCViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View view = mInflater.inflate(R.layout.irc_listview_textview, viewGroup, false);
        return new IRCViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final IRCViewHolder holder, final int position) {
        final Event event = getEvent(position);
        addTimestampIfRequired(holder, event);
        holder.message.setText(mEventCache.get(event).getMessage());

        Linkify.addLinks(holder.message, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    @Override
    public IRCFilter getFilter() {
        if (mFilter == null) {
            mFilter = new IRCFilter();
        }
        return mFilter;
    }

    private Event getEvent(final int position) {
        synchronized (mLock) {
            return mEvents.get(position);
        }
    }

    private void addTimestampIfRequired(IRCViewHolder holder, final Event event) {
        if (AppPreferences.getAppPreferences().shouldDisplayTimestamps()) {
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.timestamp.setText(event.timestamp.format("%H:%M"));
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }
    }

    public void add(final T event) {
        if (mShouldFilter && !EventUtils.shouldStoreEvent(event)) {
            return;
        }

        synchronized (mLock) {
            mEvents.add(event);
        }
        notifyItemInserted(mEvents.size() - 1);
    }

    public void setData(final List<T> list, final Runnable runnable) {
        if (mShouldFilter) {
            getFilter().setDataToFilter(list);
            getFilter().setCallback(runnable);
            getFilter().filter(null);
        } else {
            synchronized (mLock) {
                mEvents = new ArrayList<>(list);
            }
            notifyDataSetChanged();
        }
    }

    class IRCViewHolder extends RecyclerView.ViewHolder {

        public final TextView timestamp;

        public final TextView message;

        public IRCViewHolder(View itemView) {
            super(itemView);

            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            UIUtils.setRobotoLight(mContext, timestamp);
            timestamp.setTextSize(AppPreferences.getAppPreferences().getMainFontSize());

            message = (TextView) itemView.findViewById(R.id.message);
            UIUtils.setRobotoLight(mContext, message);
            message.setTextSize(AppPreferences.getAppPreferences().getMainFontSize());
        }
    }

    private class IRCFilter extends Filter {

        private List<T> mDataToFilter = new ArrayList<>();

        private Runnable mCallback;

        public void setDataToFilter(final List<T> list) {
            mDataToFilter = ImmutableList.copyOf(list);
        }

        public void setCallback(Runnable callback) {
            mCallback = callback;
        }

        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final ArrayList<T> resultList = new ArrayList<>();
            for (final T object : mDataToFilter) {
                if (EventUtils.shouldStoreEvent(object)) {
                    resultList.add(object);
                }
            }

            final FilterResults results = new FilterResults();
            results.values = resultList;
            results.count = resultList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            mEvents = (List<T>) results.values;
            notifyDataSetChanged();
            if (mCallback != null) {
                mCallback.run();
            }
        }
    }
}
*/
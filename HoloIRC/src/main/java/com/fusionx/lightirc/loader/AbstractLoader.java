package com.fusionx.lightirc.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class AbstractLoader<T> extends AsyncTaskLoader<T> {

    protected T mItem;

    public AbstractLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(final T item) {
        mItem = item;

        if (isStarted()) {
            super.deliverResult(item);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mItem != null) {
            deliverResult(mItem);
        }

        if (takeContentChanged() || mItem == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
    }
}
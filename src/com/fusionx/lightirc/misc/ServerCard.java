package com.fusionx.lightirc.misc;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.fima.cardsui.objects.Card;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.irc.LightBuilder;

public class ServerCard extends Card {
    private final LightBuilder mBuilder;
    protected final String mStatus;

    public ServerCard(final String title, final String status,
                      final LightBuilder builder) {
        super(title);
        mStatus = status;
        mBuilder = builder;
    }

    @Override
    public View getCardContent(Context context) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_server_card, null);
        ((TextView) view.findViewById(R.id.title)).setText(title);
        ((TextView) view.findViewById(R.id.description)).setText(mStatus);
        return view;
    }

    @Override
    public View getView(final Context context) {
        View view = super.getView(context);
        view.findViewById(R.id.cardContent).setTag(mBuilder);
        view.setLongClickable(true);
        OnLongClickListener listener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!((MainServerListActivity) context).actionModeStarted) {
                    ((Activity) context)
                            .startActionMode((MainServerListActivity) context);
                }
                ((MainServerListActivity) context).actionModeItems
                        .add(mBuilder);
                ((MainServerListActivity) context).updateActionMode();
                return true;
            }
        };

        view.setOnLongClickListener(listener);
        return view;
    }

    @Override
    public View getViewFirst(final Context context) {
        View view = super.getViewFirst(context);
        ((FrameLayout) view.findViewById(R.id.cardContent)).setTag(mBuilder);
        view.setLongClickable(true);
        OnLongClickListener listener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!((MainServerListActivity) context).actionModeStarted) {
                    ((Activity) context)
                            .startActionMode((MainServerListActivity) context);
                }
                ((MainServerListActivity) context).actionModeItems
                        .add(mBuilder);
                ((MainServerListActivity) context).updateActionMode();
                return true;
            }
        };

        view.setOnLongClickListener(listener);
        return view;
    }

    @Override
    public View getViewLast(final Context context) {
        View view = super.getViewLast(context);
        ((FrameLayout) view.findViewById(R.id.cardContent)).setTag(mBuilder);
        view.setLongClickable(true);
        OnLongClickListener listener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!((MainServerListActivity) context).actionModeStarted) {
                    ((Activity) context)
                            .startActionMode((MainServerListActivity) context);
                }
                ((MainServerListActivity) context).actionModeItems
                        .add(mBuilder);
                ((MainServerListActivity) context).updateActionMode();
                return true;
            }
        };

        view.setOnLongClickListener(listener);
        return view;
    }
}
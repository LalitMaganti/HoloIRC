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
import com.fusionx.lightirc.irc.LightPircBotX;

public class ServerCard extends Card {
	protected final String mStatus;
	private final LightPircBotX mBot;

	public ServerCard(final String title, final String status, final LightPircBotX bot) {
		super(title);
		mStatus = status;
		mBot = bot;
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
		((FrameLayout) view.findViewById(R.id.cardContent)).setTag(mBot);
		view.setLongClickable(true);
		OnLongClickListener listener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if(!((MainServerListActivity)context).actionModeStarted) {
					((Activity) context).startActionMode((MainServerListActivity)context);
				}
				((MainServerListActivity)context).actionModeItems.add(mBot);
				((MainServerListActivity)context).updateActionMode();
				return true;
			}
		};

		view.setOnLongClickListener(listener);
		return view;
	}

	public View getViewLast(final Context context) {
		View view = super.getViewLast(context);
		((FrameLayout) view.findViewById(R.id.cardContent)).setTag(mBot);
		view.setLongClickable(true);
		OnLongClickListener listener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if(!((MainServerListActivity)context).actionModeStarted) {
					((Activity) context).startActionMode((MainServerListActivity)context);
				}
				((MainServerListActivity)context).actionModeItems.add(mBot);
				((MainServerListActivity)context).updateActionMode();
				return true;
			}
		};

		view.setOnLongClickListener(listener);
		return view;
	}

	public View getViewFirst(final Context context) {
		View view = super.getViewFirst(context);
		((FrameLayout) view.findViewById(R.id.cardContent)).setTag(mBot);
		view.setLongClickable(true);
		OnLongClickListener listener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if(!((MainServerListActivity)context).actionModeStarted) {
					((Activity) context).startActionMode((MainServerListActivity)context);
				}
				((MainServerListActivity)context).actionModeItems.add(mBot);
				((MainServerListActivity)context).updateActionMode();
				return true;
			}
		};

		view.setOnLongClickListener(listener);
		return view;
	}
}
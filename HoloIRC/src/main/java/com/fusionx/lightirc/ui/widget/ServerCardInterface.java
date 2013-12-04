package com.fusionx.lightirc.ui.widget;

import com.fusionx.androidirclibrary.Server;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public interface ServerCardInterface {

    public String getTitle();

    public View getView(final View convertView, final ViewGroup parent, final Server server);

    public void onCardDismiss();

    public void onCardEdit(final ArrayList<String> currentlyDisplayedCards);
}

package com.fusionx.uiircinterface.interfaces;

import android.os.Handler;

import com.fusionx.lightirc.misc.FragmentType;

public interface IFragmentSideHandler {
    public Handler getServerChannelHandler();

    public Handler getFragmentHandler(final String destination, final FragmentType type);

    public void onMention(final String destination);
}

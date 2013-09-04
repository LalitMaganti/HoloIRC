package com.fusionx.lightirc.interfaces;

import android.os.Handler;

import com.fusionx.lightirc.constants.FragmentTypeEnum;

public interface IFragmentSideHandler {
    public Handler getServerChannelHandler();

    public Handler getFragmentHandler(final String destination, final FragmentTypeEnum type);

    public void onMention(final String destination);
}

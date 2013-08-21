package com.fusionx.uiircinterface.interfaces;

import android.os.Handler;

import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;

public interface FragmentSideHandlerInterface extends CommonCallbacks {
    public Handler getServerChannelHandler();

    public Handler getFragmentHandler(final String destination, final FragmentType type);

    public void mention(final String destination);
}

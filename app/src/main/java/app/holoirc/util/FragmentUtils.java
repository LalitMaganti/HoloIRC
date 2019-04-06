package app.holoirc.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class FragmentUtils {

    // Casts are checked using runtime methods
    @SuppressWarnings("unchecked")
    public static <T> T getParent(Fragment frag, Class<T> callbackInterface) {
        final Fragment parentFragment = frag.getParentFragment();
        if (parentFragment != null && callbackInterface.isInstance(parentFragment)) {
            return (T) parentFragment;
        } else {
            final FragmentActivity activity = frag.getActivity();
            if (activity != null && callbackInterface.isInstance(activity)) {
                return (T) activity;
            }
        }
        return null;
    }
}
package app.holoirc;

import android.app.Application;

import app.holoirc.handlers.NotificationsHandler;
import app.holoirc.misc.AppPreferences;

public class IRCApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppPreferences.setupAppPreferences(this);
        NotificationsHandler.init(this);
    }


    /**
     * Determines if we are running a beta or a stable version
     * @return true if this is a stable build, false otherwise
     */
    public static boolean isStable() {
        return BuildConfig.FLAVOR.toLowerCase().equals("stable");
    }

    /**
     * Determines whether this is a release build.
     *
     * @return true if this is a release build, false otherwise.
     */
    public static boolean isRelease() {
        return !BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.toLowerCase().equals("release");
    }
}

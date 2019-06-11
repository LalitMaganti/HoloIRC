package app.holoirc.ui;

import android.os.Bundle;

import app.holoirc.R;
import app.holoirc.util.UIUtils;

import java.util.List;

public class SettingsActivity extends SettingsActivityBase {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.app_settings_headers, target);
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        return SettingsActivity.class.getName().equals(fragmentName);
    }
}

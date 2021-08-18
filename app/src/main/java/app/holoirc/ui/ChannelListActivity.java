package app.holoirc.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import app.holoirc.util.UIUtils;

public class ChannelListActivity extends AppCompatActivity {

    private ChannelListFragment mChannelListFragment;

    @Override
    public void onBackPressed() {
        mChannelListFragment.onSaveData();

        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());
        super.onCreate(savedInstanceState);

        mChannelListFragment = new ChannelListFragment();

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                mChannelListFragment).commit();
    }
}
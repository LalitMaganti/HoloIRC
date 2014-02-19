package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.relay.ServerConfiguration;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

public class SharedPrefsToDatabaseConverter {

    public void migrate(final Context context) {
        final BuilderDatabaseSource source = new BuilderDatabaseSource(context);
        final ArrayList<File> array = new ArrayList<>();
        final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath(context));
        boolean oldSystem = false;
        for (final File file : folder.listFiles()) {
            if (!SharedPreferencesUtils.isExcludedString(file.getName())) {
                oldSystem = true;
                array.add(file);
            }
        }
        if (oldSystem) {
            source.open();
            for (final File file : array) {
                final ServerConfiguration.Builder builder = SharedPreferencesUtils
                        .convertPrefsToBuilder(context, file.getName().replace(".xml", ""));
                source.addBuilder(builder);
            }
            source.close();
        }
    }
}
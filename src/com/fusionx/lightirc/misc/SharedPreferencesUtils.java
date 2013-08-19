/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static com.fusionx.lightirc.misc.PreferenceKeys.AutoNickChange;
import static com.fusionx.lightirc.misc.PreferenceKeys.FirstNick;
import static com.fusionx.lightirc.misc.PreferenceKeys.Port;
import static com.fusionx.lightirc.misc.PreferenceKeys.SSL;
import static com.fusionx.lightirc.misc.PreferenceKeys.ServerUserName;
import static com.fusionx.lightirc.misc.PreferenceKeys.Title;
import static com.fusionx.lightirc.misc.PreferenceKeys.URL;

public class SharedPreferencesUtils {
    public static String getSharedPreferencesPath(final Context context) {
        return context.getFilesDir().getAbsolutePath().replace("files", "shared_prefs/");
    }

    public static void firstTimeServerSetup(final Context context) {
        AssetManager assetManager = context.getAssets();
        final String[] files = {"freenode.xml"};
        for(String filename : files) {
            try {
                final InputStream in = assetManager.open(filename);
                final File outFile = new File(getSharedPreferencesPath(context));
                if(outFile.mkdir()) {
                    final File file = new File(getSharedPreferencesPath(context), filename);
                    final FileOutputStream out = new FileOutputStream(file);

                    byte[] buffer = new byte[2048];
                    int read;
                    while((read = in.read(buffer)) != -1){
                        out.write(buffer, 0, read);
                    }

                    in.close();
                    out.flush();
                    out.close();
                }
            } catch(IOException e) {
                // TODO - fix this
            }
        }
    }

    public static ArrayList<String> getServersFromPreferences(final Context context) {
        final ArrayList<String> array = new ArrayList<>();
        final File folder = new File(getSharedPreferencesPath(context));
        for (final String file : folder.list()) {
            if (file.startsWith("server_")) {
                array.add(migrateFileToNewSystem(context, file));
            } else if(!file.equals("main.xml")) {
                array.add(file.replace(".xml", ""));
            }
        }
        Collections.sort(array);
        return array;
    }

    public static String migrateFileToNewSystem(final Context context, final String fileName) {
        final File file = new File(getSharedPreferencesPath(context), fileName);
        final SharedPreferences sharedPreferences = context.getSharedPreferences(StringUtils
                .remove(fileName, ".xml"), Context.MODE_PRIVATE);
        final String newName = sharedPreferences.getString(Title, "").toLowerCase();
        file.renameTo(new File(getSharedPreferencesPath(context),
                newName + ".xml"));
        return newName;
    }
}

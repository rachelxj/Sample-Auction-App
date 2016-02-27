package com.auctionapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by apple on 2/25/16.
 */
public class Preferences {
    private static final String LOGGED_USER_ID = "LOGGED_USER_ID";
    private Context context;
    private SharedPreferences prefs;

    public Preferences(Context context) {
        this.context = context;
    }

    private SharedPreferences getSharedPreferences() {
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        }
        return prefs;
    }

    public void saveLoggedUserId(long id) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(LOGGED_USER_ID, id);
        editor.commit();
    }

    public long getLoggedUserId() {
        return getSharedPreferences().getLong(LOGGED_USER_ID, -1);
    }
}

package com.ravi.flashnews.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    public PreferenceUtils(Context context) {
        String main_key = JsonKeys.CACHE_KEY;
        this.sharedPref = context.getSharedPreferences(main_key, Context.MODE_PRIVATE);
    }

    public void setData(String key, int value) {
        editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setData(String key, String value) {
        editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getData(String key){
        return sharedPref.getString(key, null);
    }

    public int getIntData(String key) {
        return sharedPref.getInt(key, 0);
    }
}

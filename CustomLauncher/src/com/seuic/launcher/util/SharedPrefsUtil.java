package com.seuic.launcher.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.seuic.launcher.LauncherApp;

public class SharedPrefsUtil {
    private static final String SHARED_PREF_NAME = "launcher_prefs";
    
    private static final String KEY_INIT_LOAD_SUCCESS = "key_init_load_success";
    
    public static void setInitLoadSuccess(boolean value){
        putBoolean(KEY_INIT_LOAD_SUCCESS, value);
    }
    
    public static boolean isInitLoadSuccess(){
        return getBoolean(KEY_INIT_LOAD_SUCCESS, false);
    }
    
    private static void putBoolean(String key,boolean value){
        getSharedPreferences().edit().putBoolean(key, value).commit();
    }
    
    private static boolean getBoolean(String key,boolean defValue){
        return getSharedPreferences().getBoolean(key, defValue);
    }
    
    @SuppressLint("WorldWriteableFiles")
    private static SharedPreferences getSharedPreferences() {
        return LauncherApp.getAppContext().getSharedPreferences(SHARED_PREF_NAME,
                Context.MODE_WORLD_WRITEABLE);
    }
    
}

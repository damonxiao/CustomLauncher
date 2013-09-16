package com.seuic.launcher;

import android.app.Application;
import android.content.Context;

import com.seuic.launcher.util.AppHelper;
import com.seuic.launcher.util.SharedPrefsUtil;

public class LauncherApp extends Application {
    private static Context mContext;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        if(!SharedPrefsUtil.isInitLoadSuccess()){
            SharedPrefsUtil.setInitLoadSuccess(AppHelper.initDefinedApps());
        }
        AppHelper.loadDefinedApp();
    }
    
    public static Context getAppContext(){
        return mContext;
    }
}

package com.seuic.launcher.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.seuic.launcher.LauncherApp;
import com.seuic.launcher.R;
import com.seuic.launcher.data.AppInfo;
import com.seuic.launcher.data.AppInfo.AppSize;
import com.seuic.launcher.data.AppLiteInfo;
import com.seuic.launcher.util.LauncherTables.TAppLiteInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AppHelper {
    private static final String TAG = "AppLoader";
    
    private static final HashMap<String, AppLiteInfo> DEFAULT_DEFINED_ICONS = new HashMap<String, AppLiteInfo>();
    
    private static final HashMap<String, AppLiteInfo> PACKAGE_ICON_MAPPING = new HashMap<String, AppLiteInfo>();
    
    static {
        DEFAULT_DEFINED_ICONS.put("com.android.calculator2", new AppLiteInfo(
                "com.android.calculator2", "icons/calculator.png", getColor(R.color.launcher_icon_item_blue),
                AppSize.small));
        DEFAULT_DEFINED_ICONS.put("org.openintents.filemanager", new AppLiteInfo(
                "org.openintents.filemanager", "icons/file_management.png",
                getColor(R.color.orangered), AppSize.large));
        DEFAULT_DEFINED_ICONS.put("com.android.browser", new AppLiteInfo("com.android.browser",
                "icons/browser.png", getColor(R.color.green), AppSize.small));
        DEFAULT_DEFINED_ICONS.put("com.android.calendar", new AppLiteInfo("com.android.calendar",
                "icons/calender.png", getColor(R.color.launcher_icon_item_blue), AppSize.large));
        DEFAULT_DEFINED_ICONS.put("com.android.settings", new AppLiteInfo("com.android.settings",
                "icons/setting.png", getColor(R.color.launcher_icon_item_blue), AppSize.small));
        DEFAULT_DEFINED_ICONS.put("com.android.deskclock", new AppLiteInfo("com.android.deskclock",
                "icons/clock.png", getColor(R.color.launcher_icon_item_blue), AppSize.large));
        DEFAULT_DEFINED_ICONS.put("com.android.gallery3d", new AppLiteInfo("com.android.gallery3d",
                "icons/picture.png", getColor(R.color.launcher_icon_item_blue), AppSize.small));
    }
    
    private static int getColor(int colorResId) {
        return LauncherApp.getAppContext().getResources().getColor(colorResId);
    }
    
    private static Drawable getDefinedIconByPackage(String packageName,Context context){
        if(TextUtils.isEmpty(packageName)){
            return null;
        }
        if (PACKAGE_ICON_MAPPING.containsKey(packageName)) {
            return loadDrawableFromAssets(PACKAGE_ICON_MAPPING.get(packageName).getIconPath(), context);
        }
        return null;
    }
    
    public static AppLiteInfo getAppLiteInfoByPackage(String packageName){
        if(TextUtils.isEmpty(packageName)){
            return null;
        }
        if (PACKAGE_ICON_MAPPING.containsKey(packageName)) {
            return PACKAGE_ICON_MAPPING.get(packageName);
        }
        return null;
    }
    
    public static boolean saveAppLiteInfo(AppInfo appInfo) {
        if (appInfo == null) {
            return false;
        }
        AppLiteInfo appLiteInfo = getAppLiteInfoByPackage(appInfo.getPackageName());
        if (appLiteInfo == null) {
            appLiteInfo = new AppLiteInfo(appInfo.getPackageName(), null, appInfo.getIconBgColor(),
                    appInfo.getAppSize());
        }else {
            appLiteInfo.setColor(appInfo.getIconBgColor());
            appLiteInfo.setSize(appInfo.getAppSize());
        }
        if(appInfo.getTitle() != null){
            appLiteInfo.setLabel(appInfo.getTitle().toString());
        }
        Logger.d(TAG, "saveAppLiteInfo()[appLiteInfo="+appLiteInfo+"]");
        if (!checkAppInfoExist(appInfo)) {
            LauncherApp.getAppContext().getContentResolver()
                    .insert(LauncherTables.TAppLiteInfo.CONTENT_URI, appLiteInfo.toContentValues());
            return true;
        } else {
            return LauncherApp
                    .getAppContext()
                    .getContentResolver()
                    .update(LauncherTables.TAppLiteInfo.CONTENT_URI, appLiteInfo.toContentValues(),
                            TAppLiteInfo.PACKAGE_NAME+"='"+appInfo.getPackageName()+"'", null) > 0;
        }
    }
    
    private static boolean checkAppInfoExist(AppInfo appInfo){
        if(appInfo == null){
            return false;
        }
        boolean exist = false;
        Cursor cursor = null;
        try {
            cursor = LauncherApp.getAppContext().getContentResolver().query
                    (LauncherTables.TAppLiteInfo.CONTENT_URI, null, TAppLiteInfo.PACKAGE_NAME+"='"+appInfo.getPackageName()+"'", null, null);
            exist =  cursor != null && cursor.getCount() > 0;
        } finally{
            if(cursor != null){
                cursor.close();
            }
        }
        return exist;
    }
    
    @SuppressLint("ResourceAsColor")
    public static AppInfo loadAppInfo(ResolveInfo info,Context context,PackageManager manager){
        AppInfo application = new AppInfo();
        application.setIconBgColor(R.color.launcher_icon_item_blue);
        application.setTitle(info.loadLabel(manager));
        application.setActivity(new ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name),
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        String pkgName = info.activityInfo.applicationInfo.packageName;
        application.setPackageName(pkgName);
        if (PACKAGE_ICON_MAPPING.containsKey(pkgName) && PACKAGE_ICON_MAPPING.get(pkgName) != null) {
            AppLiteInfo liteInfo = PACKAGE_ICON_MAPPING.get(pkgName);
            Drawable definedIcon = getDefinedIconByPackage(
                    liteInfo.getPkgName(), context);
            application.setIcon(definedIcon != null ? definedIcon : info.activityInfo
                    .loadIcon(manager));
            application.setAppSize(liteInfo.getSize());
            if(liteInfo.getLabel() != null){
                application.setTitle(liteInfo.getLabel());
            }
            application.setIconBgColor(liteInfo.getColor());
        }
        else {
            application.setIcon(info.activityInfo
                    .loadIcon(manager));
            application.setAppSize(AppSize.small);
            application.setIconBgColor(getColor(R.color.launcher_icon_item_blue));
        }
        return application;
    }
    
    @SuppressLint("ResourceAsColor")
    public static AppInfo loadAppInfo(String packageName,Context context,PackageManager manager){
        AppInfo application = null;
        try {
            application = new AppInfo();
            ApplicationInfo info = manager.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA);
            application.setIconBgColor(R.color.launcher_icon_item_blue);
            application.setTitle(info.loadLabel(manager));
            application.setPackageName(packageName);
            Intent it = new Intent(Intent.ACTION_MAIN);
            it.setPackage(packageName);
            it.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName ac = it.resolveActivity(manager);
            application.setActivity(ac,
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if (PACKAGE_ICON_MAPPING.containsKey(packageName)
                    && PACKAGE_ICON_MAPPING.get(packageName) != null) {
                AppLiteInfo liteInfo = PACKAGE_ICON_MAPPING.get(packageName);
                Drawable definedIcon = getDefinedIconByPackage(
                        liteInfo.getPkgName(), context);
                application.setIcon(definedIcon != null ? definedIcon : info
                        .loadIcon(manager));
                application.setAppSize(liteInfo.getSize());
                if(liteInfo.getLabel() != null){
                    application.setTitle(liteInfo.getLabel());
                }
                application.setIconBgColor(liteInfo.getColor());
            }
            else {
                application.setIcon(info
                        .loadIcon(manager));
                application.setAppSize(AppSize.small);
                application.setIconBgColor(getColor(R.color.launcher_icon_item_blue));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return application;
    }
    
    public static Drawable loadDrawableFromAssets(String iconPath,Context context){
        if(TextUtils.isEmpty(iconPath) || context == null){
            return null;
        }
        InputStream is = null;
        Drawable icon = null;
        try {
            is = context.getAssets().open(iconPath);
            icon = Drawable.createFromStream(is, iconPath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return icon;
    }
    
    public static boolean initDefinedApps() {
        Logger.d(
                TAG,
                "initDefinedApps()[PACKAGE_ICON_MAPPING.isEmpty()="
                        + PACKAGE_ICON_MAPPING.isEmpty() + "]");
        if (DEFAULT_DEFINED_ICONS.isEmpty()) {
            return false;
        }
        PackageManager manager = LauncherApp.getAppContext().getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();
            for (int i = 0; i < count; i++) {
                ResolveInfo info = apps.get(i);
                if (!DEFAULT_DEFINED_ICONS.containsKey(info.activityInfo.packageName)) {
                    DEFAULT_DEFINED_ICONS.put(info.activityInfo.packageName, new AppLiteInfo(
                            info.activityInfo.packageName, null,
                            getColor(R.color.launcher_icon_item_blue), AppSize.small));
                }
            }
        }
        Collection<AppLiteInfo> collection = DEFAULT_DEFINED_ICONS.values();
        List<ContentValues> values = new ArrayList<ContentValues>();
        int temp = 0;
        for (Iterator<AppLiteInfo> iter = collection.iterator(); iter.hasNext();) {
            AppLiteInfo liteInfo = iter.next();
            liteInfo.setSortPositon(temp);
            values.add(liteInfo.toContentValues());
            temp++;
        }
        ContentValues valuesArray[] = new ContentValues[values.size()];
        return LauncherApp.getAppContext().getContentResolver()
                .bulkInsert(LauncherTables.TAppLiteInfo.CONTENT_URI, values.toArray(valuesArray)) > 0;
    }
    
    public static void loadDefinedApp() {
        Logger.d(
                TAG,
                "loadDefinedApp()");
        Cursor cursor = LauncherApp.getAppContext().getContentResolver()
                .query(LauncherTables.TAppLiteInfo.CONTENT_URI, null, null, null, LauncherTables.TAppLiteInfo.SORT_POSITION+" DESC");
        try {
            if (cursor != null && cursor.getCount() > 0) {
                PACKAGE_ICON_MAPPING.clear();
                while (cursor.moveToNext()) {
                    AppLiteInfo appLiteInfo = new AppLiteInfo(cursor);
                    PACKAGE_ICON_MAPPING.put(appLiteInfo.getPkgName(), appLiteInfo);
                }
            }
            Logger.d(TAG, "loadDefinedApp()[PACKAGE_ICON_MAPPING:"+PACKAGE_ICON_MAPPING+"]");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

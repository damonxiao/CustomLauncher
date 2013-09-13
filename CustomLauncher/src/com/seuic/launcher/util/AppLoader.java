package com.seuic.launcher.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.seuic.launcher.R;
import com.seuic.launcher.widget.AppInfo;
import com.seuic.launcher.widget.AppInfo.AppSize;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class AppLoader {
    private static class AppLiteInfo{
        String pkgName;
        String iconPath;
        int color;
        AppSize size = AppSize.small;
        public AppLiteInfo(String pkgName, String iconPath, int color, AppSize size) {
            super();
            this.pkgName = pkgName;
            this.iconPath = iconPath;
            this.color = color;
            this.size = size;
        }
    }
    
    private static final HashMap<String, AppLiteInfo> PACKAGE_ICON_MAPPING = new HashMap<String, AppLiteInfo>();
    static {
        PACKAGE_ICON_MAPPING.put("com.android.calculator2", new AppLiteInfo(
                "com.android.calculator2", "icons/calculator.png", R.color.launcher_icon_item_blue,
                AppSize.large));
        PACKAGE_ICON_MAPPING.put("org.openintents.filemanager", new AppLiteInfo(
                "org.openintents.filemanager", "icons/file_management.png",
                R.color.orangered, AppSize.large));
        PACKAGE_ICON_MAPPING.put("com.android.browser", new AppLiteInfo("com.android.browser",
                "icons/browser.png", R.color.green, AppSize.small));
        PACKAGE_ICON_MAPPING.put("com.android.calendar", new AppLiteInfo("com.android.calendar",
                "icons/calender.png", R.color.launcher_icon_item_blue, AppSize.large));
        PACKAGE_ICON_MAPPING.put("com.android.settings", new AppLiteInfo("com.android.settings",
                "icons/setting.png", R.color.launcher_icon_item_blue, AppSize.small));
        PACKAGE_ICON_MAPPING.put("com.android.deskclock", new AppLiteInfo("com.android.deskclock",
                "icons/clock.png", R.color.launcher_icon_item_blue, AppSize.large));
        PACKAGE_ICON_MAPPING.put("com.android.gallery3d", new AppLiteInfo("com.android.gallery3d",
                "icons/picture.png", R.color.launcher_icon_item_blue, AppSize.small));
    }
    
    private static Drawable getDefinedIconByPackage(String packageName,Context context){
        if(TextUtils.isEmpty(packageName)){
            return null;
        }
        if(PACKAGE_ICON_MAPPING.containsKey(packageName)){
            return loadIconFromAssets(PACKAGE_ICON_MAPPING.get(packageName).iconPath, context);
        }
        return null;
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
        if (PACKAGE_ICON_MAPPING.containsKey(pkgName) && PACKAGE_ICON_MAPPING.get(pkgName) != null) {
            AppLiteInfo liteInfo = PACKAGE_ICON_MAPPING.get(pkgName);
            Drawable definedIcon = getDefinedIconByPackage(
                    liteInfo.pkgName, context);
            application.setIcon(definedIcon != null ? definedIcon : info.activityInfo
                    .loadIcon(manager));
            application.setAppSize(liteInfo.size);
            application.setIconBgColor(liteInfo.color);
        }
        else {
            application.setIcon(info.activityInfo
                    .loadIcon(manager));
            application.setAppSize(AppSize.small);
        }
        return application;
    }
    
    private static Drawable loadIconFromAssets(String iconPath,Context context){
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
}

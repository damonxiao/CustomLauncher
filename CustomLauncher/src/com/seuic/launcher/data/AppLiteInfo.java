package com.seuic.launcher.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.seuic.launcher.data.AppInfo.AppSize;
import com.seuic.launcher.util.LauncherTables.TAppLiteInfo;

public class AppLiteInfo{
    private int id;
    private String pkgName;
    private String iconPath;
    private int color;
    private AppSize size = AppSize.small;
    private String label;
    public AppLiteInfo(String pkgName, String iconPath, int color, AppSize size) {
        super();
        this.pkgName = pkgName;
        this.iconPath = iconPath;
        this.color = color;
        this.size = size;
    }
    
    public AppLiteInfo() {
        super();
    }
    
    public AppLiteInfo(Cursor cursor) {
        if (cursor != null) {
            id = cursor.getInt(cursor.getColumnIndex(TAppLiteInfo._ID));
            pkgName = cursor.getString(cursor.getColumnIndex(TAppLiteInfo.PACKAGE_NAME));
            iconPath = cursor.getString(cursor.getColumnIndex(TAppLiteInfo.ICON_IMAGE_PATH));
            color = cursor.getInt(cursor.getColumnIndex(TAppLiteInfo.ICON_COLOR));
            size = AppSize.valueOf(cursor.getInt(cursor.getColumnIndex(TAppLiteInfo.ICON_SIZE)));
            label = cursor.getString(cursor.getColumnIndex(TAppLiteInfo.LABEL));
        }
    }

    public String getPkgName() {
        return pkgName;
    }
    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }
    public String getIconPath() {
        return iconPath;
    }
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public AppSize getSize() {
        return size;
    }
    public void setSize(AppSize size) {
        this.size = size;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        values.put(TAppLiteInfo.PACKAGE_NAME, pkgName);
        values.put(TAppLiteInfo.ICON_IMAGE_PATH, iconPath);
        values.put(TAppLiteInfo.ICON_COLOR, color);
        values.put(TAppLiteInfo.ICON_SIZE, size.getSizeValue());
        values.put(TAppLiteInfo.LABEL, label);
        return values;
    }

    @Override
    public String toString() {
        return "AppLiteInfo [id=" + id + ", pkgName=" + pkgName + ", iconPath=" + iconPath
                + ", color=" + color + ", size=" + size + "]";
    }
    
}
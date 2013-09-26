package com.seuic.launcher.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.seuic.launcher.LauncherApp;
import com.seuic.launcher.data.AppItem.ItemType;
import com.seuic.launcher.util.AppHelper;
import com.seuic.launcher.util.LauncherTables.TAppLiteInfo;

public class AppLiteInfo{
    public enum AppSize{
        large(1),small(0);
        private int size;
        AppSize(int size){
            this.size = size;
        }
        
        public int getSizeValue(){
            return size;
        }
        public static AppSize valueOf(int orginalValue){
            if(orginalValue == 0){
                return small;
            }
            if(orginalValue == 1){
                return large;
            }
            return small;
        }
    }
    
    private int id;
    private String pkgName;
    private String iconPath;
    private int color;
    private AppSize size = AppSize.small;
    private String label;
    private int sortPositon;
    
    //to mark the APP icons position is left,right or match line.
    private ItemType itemPositionType = ItemType.LEFT_RIGHT;
    
    /**
     * The intent used to start the application.
     */
    private Intent intent;
    /**
     * When set to true, indicates that the icon has been resized.
     */
    private boolean filtered;
    
    private Drawable icon;
    
    private int visibility = View.VISIBLE;//see View.VISIBLE,View.INVISIBLE,View.GONE
    
    public AppLiteInfo(String pkgName, String iconPath, int color, AppSize size, ItemType itemPositonType) {
        super();
        this.pkgName = pkgName;
        this.iconPath = iconPath;
        this.color = color;
        this.size = size;
        this.itemPositionType = itemPositonType;
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
            sortPositon = cursor.getInt(cursor.getColumnIndex(TAppLiteInfo.SORT_POSITION));
            itemPositionType = ItemType.values()[cursor.getInt(cursor
                    .getColumnIndex(TAppLiteInfo.ITEM_POSITION_TYPE))];
            
            //initial needed data
            Intent it = new Intent(Intent.ACTION_MAIN);
            it.setPackage(pkgName);
            it.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager pkgMgr = LauncherApp.getAppContext().getPackageManager();
            ResolveInfo info = pkgMgr.resolveActivity(it, PackageManager.GET_RESOLVED_FILTER);
            setActivity(new ComponentName(
                    info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name),
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Drawable definedIcon = AppHelper.getDefinedIconByPackage(
                    pkgName, LauncherApp.getAppContext());
            icon = definedIcon != null ? definedIcon : info
                    .loadIcon(pkgMgr);
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

    public int getSortPositon() {
        return sortPositon;
    }

    public void setSortPositon(int sortPositon) {
        this.sortPositon = sortPositon;
    }
    
    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }
    
    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public ItemType getItemPositionType() {
        return itemPositionType;
    }

    public void setItemPositionType(ItemType itemPositionType) {
        this.itemPositionType = itemPositionType;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    public final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
    }

    public Intent getIntent() {
        return intent;
    }

    public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        values.put(TAppLiteInfo.PACKAGE_NAME, pkgName);
        values.put(TAppLiteInfo.ICON_IMAGE_PATH, iconPath);
        values.put(TAppLiteInfo.ICON_COLOR, color);
        values.put(TAppLiteInfo.ICON_SIZE, size.getSizeValue());
        values.put(TAppLiteInfo.LABEL, label);
        values.put(TAppLiteInfo.SORT_POSITION, sortPositon);
        values.put(TAppLiteInfo.ITEM_POSITION_TYPE, itemPositionType.ordinal());
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppLiteInfo)) {
            return false;
        }

        AppLiteInfo that = (AppLiteInfo) o;
        return label.equals(that.label) &&
                intent.getComponent().getClassName().equals(
                        that.intent.getComponent().getClassName());
    }

    @Override
    public int hashCode() {
        int result;
        result = (label != null ? label.hashCode() : 0);
        final String name = intent.getComponent().getClassName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "AppLiteInfo [pkgName=" + pkgName + ", sortPositon="
                + sortPositon + "]";
    }
//    @Override
//    public String toString() {
//        return "AppLiteInfo [id=" + id + ", pkgName=" + pkgName + ", iconPath=" + iconPath
//                + ", color=" + color + ", size=" + size + ", label=" + label + ", sortPositon="
//                + sortPositon + "]";
//    }

}
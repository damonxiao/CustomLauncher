
package com.seuic.launcher.util;

import android.net.Uri;
import android.provider.BaseColumns;

import com.seuic.launcher.LauncherApp;

public class LauncherTables {
    
    public static class TAppLiteInfo implements BaseColumns {
        
        
        public static final String TABLE_NAME = "TAppLiteInfo";
        public static final String _ID = BaseColumns._ID;
        public static final String PACKAGE_NAME = "packageName";
        public static final String LABEL = "label";
        public static final String ICON_IMAGE_PATH = "iconImagePath";
        public static final String ICON_SIZE = "iconSize";
        public static final String ICON_COLOR = "iconColor";
        public static final String SORT_POSITION = "sortPosition";
        public static final String ITEM_POSITION_TYPE = "itemPositionType";//to save the item is left,right or all line.
        
        public static final Uri CONTENT_URI = LauncherProvider.getUriFor(LauncherApp.getAppContext(), TABLE_NAME);
        
        public static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
                _ID + " integer primary key,"
                + PACKAGE_NAME + " text,"
                + LABEL + " text,"
                + ICON_IMAGE_PATH + " text,"
                + ICON_SIZE + " integer,"
                + ICON_COLOR + " integer,"
                + SORT_POSITION + " integer,"
                + ITEM_POSITION_TYPE + " integer);";
    }
}

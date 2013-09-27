/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seuic.launcher.util;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.seuic.launcher.util.LauncherTables.TAppLiteInfo;

import java.util.List;



public class LauncherProvider extends ContentProvider {
    private static final String TAG = "LauncherProvider";

    public static final String AUTHORITY = "com.seuic.launcher.provider";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri sBaseUri;
    
    private static final int DEFINED_APP_ALL = 0;
    private static final int DEFINED_APP_BY_ID = 1;
    private static final int DEFINED_APP_BY_PACKAGENAME = 2;
    
    private static final UriMatcher sURLMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);
    
    private SqliteHelper mSqliteHelper;

    static {
        sURLMatcher.addURI(AUTHORITY,LauncherTables.TAppLiteInfo.TABLE_NAME, DEFINED_APP_ALL);
    }
    
    public static Uri getUriFor(Context context, String path) {
        if (sBaseUri == null) {
            sBaseUri = Uri.parse("content://" + context.getPackageName() + ".provider");
        }
        return sBaseUri.buildUpon()
                .appendEncodedPath(path) // ignore the leading '/'
                .build();
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        /*long token = Binder.clearCallingIdentity();
        try {
            Path path = Path.fromString(uri.getPath());
            MediaItem item = (MediaItem) mDataManager.getMediaObject(path);
            return item != null ? item.getMimeType() : null;
        } finally {
            Binder.restoreCallingIdentity(token);
        }*/
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sURLMatcher.match(uri);
        String table = null;
        switch (match) {
            case DEFINED_APP_ALL:
            case DEFINED_APP_BY_ID:
            case DEFINED_APP_BY_PACKAGENAME:
                table = LauncherTables.TAppLiteInfo.TABLE_NAME;
                break;
            default:
                break;
        }
        long rowId = 0;
        if(table != null){
            SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
            rowId = db.insert(table, null, values);
        }
        if (rowId > 0) {
            uri =  ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }
        return null;
    }
    
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);
        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                db.insert(args.table, null, values[i]);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return values.length;
    }
    
    @Override
    public boolean onCreate() {
        mSqliteHelper = new SqliteHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query.
        int match = sURLMatcher.match(uri);
        switch (match) {
            case DEFINED_APP_ALL:
                qb.setTables(LauncherTables.TAppLiteInfo.TABLE_NAME);
                break;
            default:
                return null;
        }

        String orderBy = null;
        if (!TextUtils.isEmpty(sort)) {
            orderBy = sort;
        }
        SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
        Cursor ret = qb.query(db, null, selection, selectionArgs,
                              null, null, orderBy);
        ret.setNotificationUri(getContext().getContentResolver(),
                uri);
        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);                
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
    
    class SqliteHelper extends SQLiteOpenHelper{
        private static final String TAG = "SqliteHelper";

        private static final String DB_NAME = "CustomLauncher";

        private static final int DB_VERSION = 1;

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TAppLiteInfo.CREATE_TABLE_SQL);
        }

        public SqliteHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
        
        public synchronized void bulkInsert(List<ContentValues> values, String tableName) {
            Logger.d(TAG, "buldInsert()[values:" + values + ",tableName:" + tableName + "]");
            if (values == null || tableName == null) {
                return;
            }
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            for (ContentValues value : values) {
                db.insert(tableName, null, value);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }

        public synchronized void bulkDelete(String tableName, String whereClause[]) {
            Logger.d(TAG, "buldInsert()[tableName:" + tableName + ",whereClause:" + whereClause
                    + "]");
            if (tableName == null || whereClause == null) {
                return;
            }
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            for (String whereSql : whereClause) {
                db.delete(tableName, whereSql, null);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
        
        public synchronized void bulkUpdate(List<ContentValues> values, String tableName,
                String[] whereClauses) {
            Logger.d(TAG, "bulkUpdate()[values:" + values + ",tableName:" + tableName
                    + ",whereClauses:" + whereClauses + "]");
            if (values == null || tableName == null || whereClauses == null) {
                return;
            }
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            for (int i = 0; i < values.size(); i++) {
                if (whereClauses.length > i) {
                    db.update(tableName, values.get(i), whereClauses[i], null);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }
}

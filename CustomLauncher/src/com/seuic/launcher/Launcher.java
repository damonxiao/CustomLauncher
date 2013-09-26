package com.seuic.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.seuic.launcher.data.AppItem;
import com.seuic.launcher.data.AppItem.ItemType;
import com.seuic.launcher.data.AppLiteInfo;
import com.seuic.launcher.data.AppLiteInfo.AppSize;
import com.seuic.launcher.util.AppHelper;
import com.seuic.launcher.util.LauncherTables.TAppLiteInfo;
import com.seuic.launcher.util.Logger;
import com.seuic.launcher.widget.AppListAdapter;
import com.seuic.launcher.widget.HorizontalListView;

import java.util.ArrayList;
import java.util.List;

public class Launcher extends Activity{
    
    private static final String TAG = "Launcher";
    
    private HorizontalListView mAllAppsList;
    
    private List<List<AppItem>> mAllApps;
    
    private AppListAdapter mAdapter;
    
    private PackageStateReceiver mPackageStateReceiver;
    
    private static final int MSG_SCREEN_OUT_TIME = 1;
    private static final long SCREEN_OUT_TIME_DURATION = 1000*60;
    
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SCREEN_OUT_TIME:
                    Intent intent = new Intent(Launcher.this, Albums.class);
                    startActivity(intent);
                    break;

                default:
                    break;
            }
        }
    };
    
    private ContentObserver mAppLiteInfoObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Logger.d(TAG,"mAppLiteInfoObserver.onChange");
            reloadApps();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate()");
        setContentView(R.layout.app_list);
        mAllAppsList = (HorizontalListView) findViewById(R.id.all_apps_list);
        mAllAppsList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Logger.d(TAG, "onItemLongClick()[position="+position+"]");
                return false;
            }
        });
        loadApplications(false);
        mAdapter = new AppListAdapter(mAllApps, this);
        mAllAppsList.setAdapter(mAdapter);
        regReceiver();
        getContentResolver().registerContentObserver(TAppLiteInfo.CONTENT_URI, true, mAppLiteInfoObserver);
    }
    
    /**
     * Loads the list of installed applications in mApplicati(count -i) <=ons.
     */
    private void loadApplications(boolean forceReload) {
        Logger.d(TAG, "loadApplications()[forceReload=" + forceReload + "]");

        List<AppLiteInfo> appInfos = AppHelper.getAppLiteInfos(forceReload);
        if (appInfos == null || appInfos.isEmpty()) {
            return;
        }
        final int count = appInfos.size();
        if (mAllApps == null) {
            mAllApps = new ArrayList<List<AppItem>>();
        }
        mAllApps.clear();
        List<AppItem> items = new ArrayList<AppItem>();
        AppItem item = new AppItem();
        AppLiteInfo previousPosApp = null;
        for (int i = 0; i < count; i++) {
            AppLiteInfo application = appInfos.get(i);
            AppItem previousItem = !items.isEmpty() ? items.get(items.size() - 1) : null;
            if(previousItem == null && !mAllApps.isEmpty()){
                List<AppItem> tmpItems = mAllApps.get(mAllApps.size()-1);
                if(tmpItems != null && !tmpItems.isEmpty()){
                    previousItem = tmpItems.get(tmpItems.size() - 1);
                }
            }
            if (previousPosApp != null && previousPosApp.getSize() == AppSize.small) {
                if (application.getSize() == AppSize.small
                        && previousItem != null && previousItem.getRightItem() == null)
                {
                    previousItem.setRightItem(application);
                } else if (application.getSize() == AppSize.small
                        && ((previousItem != null && previousItem.getRightItem() != null)
                        || previousItem == null)) {
                    item.setLeftItem(application);
                    item.setItemType(ItemType.LEFT_RIGHT);
                    items.add(item);
                    item = new AppItem();
                } else if (application.getSize() == AppSize.large) {
                    item.setLeftItem(application);
                    item.setItemType(ItemType.ONE_LINE);
                    items.add(item);
                    item = new AppItem();
                }
            } else if (previousPosApp != null && previousPosApp.getSize() == AppSize.large) {
                if (application.getSize() == AppSize.small) {
                    item.setLeftItem(application);
                    item.setItemType(ItemType.LEFT_RIGHT);
                    items.add(item);
                    item = new AppItem();
                }
                else if (application.getSize() == AppSize.large) {
                    item.setLeftItem(application);
                    item.setItemType(ItemType.ONE_LINE);
                    items.add(item);
                    item = new AppItem();
                }
            } else if (previousPosApp == null) {
                if (application.getSize() == AppSize.small) {
                    item.setLeftItem(application);
                    item.setItemType(ItemType.LEFT_RIGHT);
                    items.add(item);
                    item = new AppItem();
                }
                else if (application.getSize() == AppSize.large) {
                    item.setLeftItem(application);
                    item.setItemType(ItemType.ONE_LINE);
                    items.add(item);
                    item = new AppItem();
                }
            }
            if (items.size() == 3 || (count - i == 1)) {
                mAllApps.add(items);
                items = new ArrayList<AppItem>();
            }
            previousPosApp = application;
        }
        Logger.d(TAG, "loadApplications()[mAllApps=" + mAllApps + "]");

    }
    
    /*private List<AppLiteInfo> getAllApps() {
        List<AppLiteInfo> appInfos = new ArrayList<AppLiteInfo>();
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();
            for (int i = 0; i < count; i++) {
                ResolveInfo info = apps.get(i);
                appInfos.add(AppHelper.loadAppInfo(info, this, manager));
            }
        }
        return appInfos;
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mHandler.removeMessages(MSG_SCREEN_OUT_TIME);
        mHandler.sendEmptyMessageDelayed(MSG_SCREEN_OUT_TIME, SCREEN_OUT_TIME_DURATION);
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy()");
        unregReceiver();
        getContentResolver().unregisterContentObserver(mAppLiteInfoObserver);
    }
    
    private void regReceiver(){
        Logger.d(TAG, "regReceiver()");
        if(mPackageStateReceiver == null){
            mPackageStateReceiver = new PackageStateReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addDataScheme("package");
            registerReceiver(mPackageStateReceiver, filter);
        }
    }
    
    private void unregReceiver(){
        Logger.d(TAG, "unregReceiver()");
        if(mPackageStateReceiver != null){
            unregisterReceiver(mPackageStateReceiver);
            mPackageStateReceiver = null;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mHandler.removeMessages(MSG_SCREEN_OUT_TIME);
        mHandler.sendEmptyMessageDelayed(MSG_SCREEN_OUT_TIME, SCREEN_OUT_TIME_DURATION);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_SCREEN_OUT_TIME);
    }
    
    private void reloadApps(){
        loadApplications(true);
        mAdapter.refreshData(mAllApps);
    }
    
    private class PackageStateReceiver extends BroadcastReceiver {

        /**
         * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
         * ACTION_PACKAGE_CHANGED.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d(TAG, "PackageStateReceiver.onReceive() intent=" + intent);
            final String action = intent.getAction();

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                reloadApps();
            }
        }
        
    }
    
}

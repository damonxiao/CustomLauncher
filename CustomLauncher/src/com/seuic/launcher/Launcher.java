package com.seuic.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import com.seuic.launcher.util.AppLoader;
import com.seuic.launcher.util.Logger;
import com.seuic.launcher.widget.AppInfo;
import com.seuic.launcher.widget.AppInfo.AppSize;
import com.seuic.launcher.widget.AppItem;
import com.seuic.launcher.widget.AppItem.ItemType;
import com.seuic.launcher.widget.AppListAdapter;
import com.seuic.launcher.widget.HorizontalListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Launcher extends Activity{
    
    private static final String TAG = "Launcher";
    
    private HorizontalListView mAllAppsList;
    
    private List<List<AppItem>> mAllApps;
    
    private AppListAdapter mAdapter;
    
    private PackageStateReceiver mPackageStateReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate()");
        setContentView(R.layout.app_list);
        mAllAppsList = (HorizontalListView) findViewById(R.id.all_apps_list);
        loadApplications(false);
        mAdapter = new AppListAdapter(mAllApps, this);
        mAllAppsList.setAdapter(mAdapter);
        regReceiver();
    }
    
    /**
     * Loads the list of installed applications in mApplicati(count -i) <=ons.
     */
    private void loadApplications(boolean isLaunching) {
        Logger.d(TAG, "loadApplications()[isLaunching=" + isLaunching + "]");
        if (isLaunching) {
            return;
        }

            List<AppInfo> appInfos = getAllApps();
            if(appInfos == null || appInfos.isEmpty()){
                return;
            }
            final int count = appInfos.size();
            if(mAllApps == null){
                mAllApps = new ArrayList<List<AppItem>>();
            }
            mAllApps.clear();
            List<AppItem> items = new ArrayList<AppItem>();
            AppItem item = new AppItem();
            AppInfo previousPosApp = null;
            for (int i = 0; i < count; i++) {
                AppInfo application = appInfos.get(i);
                AppItem previousItem = !items.isEmpty()?items.get(items.size()-1):null;
            if (previousPosApp != null && previousPosApp.getAppSize() == AppSize.small) {
                if (application.getAppSize() == AppSize.small
                        && previousItem != null && previousItem.getRightItem() == null)
                {
                    items.get(items.size() - 1).setRightItem(application);
                } else if (application.getAppSize() == AppSize.small
                        && ((previousItem != null && previousItem.getRightItem() != null)
                        || previousItem == null)) {
                    item.setLeftItem(application);
                    item.setItemType(ItemType.LEFT_RIGHT);
                    items.add(item);
                    item = new AppItem();
                } else if (application.getAppSize() == AppSize.large) {
                    item.setLeftItem(application);
                    item.setItemType(ItemType.ONE_LINE);
                    items.add(item);
                    item = new AppItem();
                }
            }else if(previousPosApp != null && previousPosApp.getAppSize() == AppSize.large){
                    if(application.getAppSize() == AppSize.small){
                        item.setLeftItem(application);
                        item.setItemType(ItemType.LEFT_RIGHT);
                        items.add(item);
                        item = new AppItem();
                    }
                    else if(application.getAppSize() == AppSize.large){
                        item.setLeftItem(application);
                        item.setItemType(ItemType.ONE_LINE);
                        items.add(item);
                        item = new AppItem();
                    }
                }else if(previousPosApp == null){
                    if(application.getAppSize() == AppSize.small){
                        item.setLeftItem(application);
                        item.setItemType(ItemType.LEFT_RIGHT);
                        items.add(item);
                        item = new AppItem();
                    }
                    else if(application.getAppSize() == AppSize.large){
                        item.setLeftItem(application);
                        item.setItemType(ItemType.ONE_LINE);
                        items.add(item);
                        item = new AppItem();
                    }
                }
                if(items.size() == 3 || (count -i ==1)){
                    mAllApps.add(items);
                    items = new ArrayList<AppItem>();
                }
                previousPosApp = application;
            }
        Logger.d(TAG, "loadApplications()[mAllApps="+mAllApps+"]");
        
    }
    
    private List<AppInfo> getAllApps() {
        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();
            for (int i = 0; i < count; i++) {
                ResolveInfo info = apps.get(i);
                appInfos.add(AppLoader.loadAppInfo(info, this, manager));
            }
        }
        return appInfos;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy()");
        unregReceiver();
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
    
    private void reloadApps(){
        loadApplications(false);
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

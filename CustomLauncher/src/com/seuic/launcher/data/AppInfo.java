/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.seuic.launcher.data;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Represents a launchable application. An application is made of a name (or title), an intent
 * and an icon.
 */
public class AppInfo implements Serializable{
    
    /** @Fields serialVersionUID: */
      	
    private static final long serialVersionUID = 1L;

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
    /**
     * The application name.
     */
    private CharSequence title;

    /**
     * The intent used to start the application.
     */
    private Intent intent;

    /**
     * The application icon.
     */
    private Drawable icon;

    /**
     * When set to true, indicates that the icon has been resized.
     */
    private boolean filtered;
    
    /**
     * To define the appIcon size,use this show different size icon.
     */
    private AppSize appSize = AppSize.small;
    
    /**
     * Background color of appIcon.
     */
    private int iconBgColor;

    private String packageName;
    
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppInfo)) {
            return false;
        }

        AppInfo that = (AppInfo) o;
        return title.equals(that.title) &&
                intent.getComponent().getClassName().equals(
                        that.intent.getComponent().getClassName());
    }

    @Override
    public int hashCode() {
        int result;
        result = (title != null ? title.hashCode() : 0);
        final String name = intent.getComponent().getClassName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public AppSize getAppSize() {
        return appSize;
    }

    public void setAppSize(AppSize appSize) {
        this.appSize = appSize;
    }

    public int getIconBgColor() {
        return iconBgColor;
    }

    public void setIconBgColor(int iconBgColor) {
        this.iconBgColor = iconBgColor;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return "AppInfo [title=" + title + "]";
    }
    
}

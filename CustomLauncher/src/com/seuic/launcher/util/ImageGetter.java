package com.seuic.launcher.util;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.seuic.launcher.LauncherApp;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ImageGetter {
    private static final HashMap<String, WeakReference<Drawable>> imageCache = new HashMap<String, WeakReference<Drawable>>();
    
    public interface ImageGetterCb{
        void onGetterSuccess(Drawable image);
    }
    
    public static void loadImageFromAssets(String assetsPath,ImageGetterCb callback){
        if(imageCache.containsKey(assetsPath) && callback != null){
            WeakReference<Drawable> reference = imageCache.get(assetsPath);
            if(reference != null && reference.get()!=null){
                callback.onGetterSuccess(reference.get());
                return;
            }
        }
        new ImageGetterTask(callback).execute(assetsPath);
        
    }
    
    private static class ImageGetterTask extends AsyncTask<String, Void, Drawable>{
        private ImageGetterCb callback;
        
        public ImageGetterTask(ImageGetterCb callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            if(params != null && params.length > 0){
                String path = params[0];
                Drawable drawable = AppHelper.loadDrawableFromAssets(path, LauncherApp.getAppContext());
                if(drawable != null){
                    imageCache.put(path, new WeakReference<Drawable>(drawable));
                    return imageCache.get(path).get();
                }
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Drawable result) {
            if(result != null && callback != null){
                callback.onGetterSuccess(result);
            }
        }
        
    }
}

package com.seuic.launcher;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.seuic.launcher.util.Const;
import com.seuic.launcher.widget.AlbumAdapter;
import com.seuic.launcher.widget.AlbumGallery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Albums extends Activity {
    private static final List<String> IMAGES = new ArrayList<String>();
    
    private AlbumGallery mGallery;
    
    private AlbumAdapter mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album);
        try {
            String files[] = getAssets().list(Const.ASSET_IMAGE_BASE_PATH);
            for(String file:files){
                IMAGES.add(Const.ASSET_IMAGE_BASE_PATH+File.separator+file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mGallery = (AlbumGallery) findViewById(R.id.album_gallery);
        mAdapter = new AlbumAdapter(IMAGES, this);
        mGallery.setAdapter(mAdapter);
        mGallery.startPlay();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGallery.stopPlay();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGallery.stopPlay();
        finish();
        return true;
    }
}

package com.seuic.launcher;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageSwitcher;

import com.seuic.launcher.widget.AlbumAdapter;
import com.seuic.launcher.widget.AlbumGallery;

import java.util.ArrayList;
import java.util.List;


public class Albums extends Activity {
    private static final List<String> IMAGES = new ArrayList<String>();
    static{
        IMAGES.add("albums/001.jpg");
        IMAGES.add("albums/002.jpg");
        IMAGES.add("albums/003.jpg");
    }
    
    private AlbumGallery mGallery;
    
    private AlbumAdapter mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album);
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

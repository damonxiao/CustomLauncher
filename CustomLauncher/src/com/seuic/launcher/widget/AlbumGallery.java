package com.seuic.launcher.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

import com.seuic.launcher.util.Logger;

public class AlbumGallery extends Gallery{

    private static final String TAG = "AlbumGallery";
    
    private boolean autoPlay;
    
    private static final int MSG_PLAY = 1;
    private static final int MSG_PAUSE = 2;
    private static final int MSG_NEXT = 3;
    private static final long DURATION = 3000;
    
    public AlbumGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AlbumGallery(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AlbumGallery(Context context) {
        this(context,null);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int kEvent;
        if (e2.getX() > e1.getX()) {
            // Check if scrolling left
            kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
        } else {
            // Otherwise scrolling right
            kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
        }
        onKeyDown(kEvent, null);
        return true;
    }
    
    public void startPlay(){
        mHandler.sendEmptyMessage(MSG_PLAY);
    }
    
    public void stopPlay(){
        mHandler.sendEmptyMessage(MSG_PAUSE);
    }
    
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_PLAY:
                    autoPlay = true;
                    sendEmptyMessageDelayed(MSG_NEXT, DURATION);
                    break;
                case MSG_PAUSE:
                    removeMessages(MSG_NEXT);
                    removeMessages(MSG_PLAY);
                    autoPlay = false;
                    break;
                case MSG_NEXT:
                    if(autoPlay){
                        if(getSelectedItemPosition() == getCount()-1){
                            setSelection(0);
                        }else{
                            setSelection(getSelectedItemPosition()+1);
                            onKeyDown(KeyEvent.KEYCODE_DPAD_UP, null);
                        }
                        sendEmptyMessageDelayed(MSG_NEXT, DURATION);
                    }
                    break;
                default:
                    break;
            }
        }
    };
}

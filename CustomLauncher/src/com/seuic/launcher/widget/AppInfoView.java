
package com.seuic.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.seuic.launcher.R;
import com.seuic.launcher.util.Logger;

public class AppInfoView extends FrameLayout implements OnClickListener {

    private AppInfo mAppIno;

    public AppInfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.icon_view, this);
        setClickable(true);
        setOnClickListener(this);
    }

    public AppInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppInfoView(Context context) {
        this(context, null);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logger.d("AppInfoView", "onKeyDown");
        return super.onKeyDown(keyCode, event);
    }
    
    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            Animation scaleIn = AnimationUtils.loadAnimation(getContext(), R.anim.scale_in);
            scaleIn.setFillAfter(true);
            this.startAnimation(scaleIn);
            return true;
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            Animation scaleOut = AnimationUtils.loadAnimation(getContext(), R.anim.scale_out);
            scaleOut.setFillAfter(true);
            scaleOut.setAnimationListener(new AnimationListener() {
                
                @Override
                public void onAnimationStart(Animation animation) {
                    
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {
                    
                }
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(mAppIno != null && mAppIno.getIntent() != null){
                        getContext().startActivity(mAppIno.getIntent());
                    }
                }
            });
            this.startAnimation(scaleOut);
        }
        return super.onTouchEvent(event);
    }*/

    @Override
    public void onClick(View v) {
        Animation scaleIn = AnimationUtils.loadAnimation(getContext(), R.anim.scale_in);
        scaleIn.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                if(mAppIno != null && mAppIno.getIntent() != null){
                    getContext().startActivity(mAppIno.getIntent());
                }
            }
        });
        v.startAnimation(scaleIn);
    }

    public void bindData(AppInfo appInfo) {
        mAppIno = appInfo;
        if(mAppIno == null){
            this.setVisibility(View.INVISIBLE);
            return;
        }
        this.setVisibility(View.VISIBLE);
        if(mAppIno != null && mAppIno.getIcon() != null){
            ((ImageView) (findViewById(R.id.icon)))
            .setImageDrawable(mAppIno.getIcon());
        }
        if (mAppIno != null) {
            findViewById(R.id.icon_view_root).setBackgroundColor(
                    getContext().getResources().getColor(mAppIno.getIconBgColor()));
        }
        if(mAppIno != null && mAppIno.getTitle() != null){
            ((TextView) (findViewById(R.id.label)))
            .setText(mAppIno.getTitle());
        }
    }

}

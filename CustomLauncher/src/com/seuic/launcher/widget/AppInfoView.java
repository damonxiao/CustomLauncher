
package com.seuic.launcher.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.seuic.launcher.AppInfoEditor;
import com.seuic.launcher.R;
import com.seuic.launcher.data.AppInfo;
import com.seuic.launcher.util.Const;
import com.seuic.launcher.util.Logger;

public class AppInfoView extends FrameLayout implements OnClickListener ,OnLongClickListener{

    private AppInfo mAppInfo;

    private View mViewRoot;
    
    private View mEditView;
    
    public AppInfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.icon_view, this);
        mViewRoot = findViewById(R.id.icon_view_root);
        mViewRoot.setClickable(true);
        mViewRoot.setOnClickListener(this);
        mViewRoot.setOnLongClickListener(this);
        
        mEditView = findViewById(R.id.edit);
        mEditView.setOnClickListener(this);
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
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon_view_root:
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
                        if(mAppInfo != null && mAppInfo.getIntent() != null){
                            getContext().startActivity(mAppInfo.getIntent());
                        }
                    }
                });
                v.startAnimation(scaleIn);
                break;
            case R.id.edit:
                dismissEditView();
                Intent edit = new Intent(getContext(),AppInfoEditor.class);
                edit.putExtra(Const.EXTRA_PACKAGE_NAME, mAppInfo.getPackageName());
                getContext().startActivity(edit);
                break;
            default:
                break;
        }
    }

    public void bindData(AppInfo appInfo) {
        mAppInfo = appInfo;
        if(mAppInfo == null){
            this.setVisibility(View.INVISIBLE);
            return;
        }
        this.setVisibility(View.VISIBLE);
        if(mAppInfo != null && mAppInfo.getIcon() != null){
            ((ImageView) (findViewById(R.id.icon)))
            .setImageDrawable(mAppInfo.getIcon());
        }
        if (mAppInfo != null) {
            findViewById(R.id.icon_view_root).setBackgroundColor(mAppInfo.getIconBgColor());
        }
        if(mAppInfo != null && mAppInfo.getTitle() != null){
            ((TextView) (findViewById(R.id.label)))
            .setText(mAppInfo.getTitle());
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        findViewById(R.id.edit).setVisibility(View.VISIBLE);
        ScaleAnimation scaleIn = (ScaleAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.scale_in);
        scaleIn.setFillAfter(true);
        scaleIn.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mViewRoot.setClickable(false);
                mViewRoot.setLongClickable(false);
            }
        });
        mViewRoot.startAnimation(scaleIn);
        return true;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean dismissEdit = dismissEditView();
        if(dismissEdit){
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    private boolean dismissEditView(){
        if (!mViewRoot.isClickable() && mEditView.getVisibility() == View.VISIBLE) {
            mViewRoot.setClickable(true);
            mViewRoot.setLongClickable(true);
            mEditView.setVisibility(View.INVISIBLE);
            ScaleAnimation scaleOut = (ScaleAnimation) AnimationUtils.loadAnimation(getContext(),
                    R.anim.scale_out);
            scaleOut.setFillAfter(true);
            mViewRoot.startAnimation(scaleOut);
            return true;
        }
        return false;
    }
    
    /*private void showEditPopup(View v) {
        ImageView editView = new ImageView(getContext());
        editView.setImageResource(R.drawable.icon_view_editor);
        PopupWindow popupWindow = new PopupWindow(editView, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        int x = getWidth() - getWidth() / 6;
        int y = getHeight() - (getHeight() / 4) * 5;
        popupWindow.showAsDropDown(v, x, y);
    }*/

}

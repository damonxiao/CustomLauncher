
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
import com.seuic.launcher.data.AppLiteInfo;
import com.seuic.launcher.data.AppItem;
import com.seuic.launcher.data.AppItem.ItemType;
import com.seuic.launcher.util.Const;
import com.seuic.launcher.util.Logger;

public class AppInfoView extends FrameLayout implements OnClickListener ,OnLongClickListener{

    public interface AppItemSelectedListener{
        void onItemInfoViewSelected(AppInfoView infoView);
        void clearSelected();
    }
    
    public static class DragItemInto{
        int pos0;//means adapter's position
        int pos1;//means in the item's list position
        AppItem.ItemType itemType = ItemType.LEFT;//to mark the item is left or right
        public DragItemInto(int pos0, int pos1, ItemType itemType) {
            super();
            this.pos0 = pos0;
            this.pos1 = pos1;
            this.itemType = itemType;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            DragItemInto item = (DragItemInto) o;
            return item.pos0 == pos0 && item.pos1 == pos1 && item.itemType == itemType;
        }
    }
    
    
    private AppLiteInfo mAppInfo;

    private View mViewRoot;
    
    private View mEditView;
    
    private AppItemSelectedListener mAppItemSelectedListener;
    
    private DragItemInto mDragItemInfo;
    
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
                            if (mAppItemSelectedListener != null) {
                                mAppItemSelectedListener.clearSelected();
                            }
                        }
                    }
                });
                v.startAnimation(scaleIn);
                break;
            case R.id.edit:
                dismissEditView();
                Intent edit = new Intent(getContext(),AppInfoEditor.class);
                edit.putExtra(Const.EXTRA_PACKAGE_NAME, mAppInfo.getPkgName());
                getContext().startActivity(edit);
                if(mAppItemSelectedListener != null){
                   mAppItemSelectedListener.clearSelected();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(mAppInfo != null ){
            mAppInfo.setVisibility(visibility);
        }
    }
    
    public void bindData(AppLiteInfo appInfo) {
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
            findViewById(R.id.icon_view_root).setBackgroundColor(mAppInfo.getColor());
        }
        if(mAppInfo != null && mAppInfo.getLabel() != null){
            ((TextView) (findViewById(R.id.label)))
            .setText(mAppInfo.getLabel());
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        if(mAppItemSelectedListener != null){
            mAppItemSelectedListener.onItemInfoViewSelected(this);
        }
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
    
    public void setOnItemSelectedListener(AppItemSelectedListener listener){
        mAppItemSelectedListener = listener;
    }
    
    public boolean containsPonit(int x,int y){
        int location[] = new int[2];
        getLocationOnScreen(location);
        Logger.d("AppInfoView", "containsPonit()x="+x+",y="+y);
        Logger.d("AppInfoView", "containsPonit()getLeft="+getLeft()+",getRight="+getRight()+",getTop="+getTop()+",getBottom="+getBottom());
        if(x > location[0] && x < (getRight()+location[0]) && y > location[1] && y < (location[1]+getBottom())){
            Logger.d("AppInfoView", "containsPonit() contains");
            return true;
        }
        Logger.d("AppInfoView", "containsPonit() not contains");
        return false;
    }

    public DragItemInto getDragItemInfo() {
        return mDragItemInfo;
    }

    public void setDragItemInfo(DragItemInto dragItemInfo) {
        this.mDragItemInfo = dragItemInfo;
    }
    
    public AppLiteInfo getAppInfo(){
        return mAppInfo;
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

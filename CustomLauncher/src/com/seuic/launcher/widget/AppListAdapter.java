
package com.seuic.launcher.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.seuic.launcher.LauncherApp;
import com.seuic.launcher.R;
import com.seuic.launcher.data.AppItem;
import com.seuic.launcher.data.AppLiteInfo;
import com.seuic.launcher.data.AppItem.ItemType;
import com.seuic.launcher.util.AppHelper;
import com.seuic.launcher.util.Logger;
import com.seuic.launcher.widget.AppInfoView.DragItemInto;

import java.util.List;

public class AppListAdapter extends BaseAdapter implements AppInfoView.AppItemSelectedListener{
    
    private List<List<AppItem>> mItems;

    private LayoutInflater mInflater;
    
    private AppInfoView mSelectInfoView;
    
    private FrameLayout mDragViewAnimContianer;
    private FrameLayout mDropViewAnimContianer;
    
    public AppListAdapter(List<List<AppItem>> items, Context context, FrameLayout dragViewAnimContianer,
            FrameLayout dropViewAnimContianer) {
        super();
        this.mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDragViewAnimContianer = dragViewAnimContianer;
        mDropViewAnimContianer = dropViewAnimContianer;
    }

    public void refreshData(List<List<AppItem>> allApps){
        mItems = allApps;
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public List<AppItem> getItem(int position) {
        return (mItems != null && mItems.size() > position) ? mItems.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.app_item_group, null);;
        List<AppItem> items = mItems.get(position);
        if(items != null && !items.isEmpty()){
            for(int count = 0;count < items.size();count++){
                AppItem item = items.get(count);
                if(item != null){
                    View itemView = mInflater.inflate(R.layout.app_item, null);
                    AppInfoView leftItemView = (AppInfoView) itemView.findViewById(R.id.app_left_item);
                    AppInfoView rightItemView = (AppInfoView) itemView.findViewById(R.id.app_right_item);
                    AppInfoView oneLineItemView = (AppInfoView) itemView.findViewById(R.id.app_one_line_item);
                    leftItemView.setOnItemSelectedListener(this);
                    rightItemView.setOnItemSelectedListener(this);
                    oneLineItemView.setOnItemSelectedListener(this);
                    
                    ItemType itemType = item.getItemType();
                    leftItemView.setDragItemInfo(new DragItemInto(position, count, ItemType.LEFT));
                    rightItemView.setDragItemInfo(new DragItemInto(position, count, ItemType.RIGHT));
                    oneLineItemView.setDragItemInfo(new DragItemInto(position, count, ItemType.ONE_LINE));
                    View leftRightItemGroup = itemView.findViewById(R.id.app_left_right_item_group);
                    switch (itemType) {
                        case LEFT_RIGHT://one item contains two item
                            /*if(item.getRightItem() == null){
                                leftRightItemGroup.setVisibility(View.GONE);
                                oneLineItemView.setVisibility(View.VISIBLE);
                                oneLineItemView.bindData(item.getLeftItem());
                            }else {
                                leftRightItemGroup.setVisibility(View.VISIBLE);
                                leftItemView.setVisibility(View.VISIBLE);
                                rightItemView.setVisibility(View.VISIBLE);
                                oneLineItemView.setVisibility(View.GONE);
                                leftItemView.bindData(item.getLeftItem());
                                rightItemView.bindData(item.getRightItem());
                            }*/
                            leftRightItemGroup.setVisibility(View.VISIBLE);
                            leftItemView.setVisibility(View.VISIBLE);
                            rightItemView.setVisibility(View.VISIBLE);
                            oneLineItemView.setVisibility(View.GONE);
                            leftItemView.bindData(item.getLeftItem());
                            rightItemView.bindData(item.getRightItem());
                            break;
                       /* case LEFT://one item just contains LEFT
                            leftRightItemGroup.setVisibility(View.VISIBLE);
                            leftItemView.setVisibility(View.VISIBLE);
                            rightItemView.setVisibility(View.GONE);
                            oneLineItemView.setVisibility(View.GONE);
                            leftItemView.bindData(item.getLeftItem());
                            break;
                        case RIGHT://one item just contains RIGHT
                            leftRightItemGroup.setVisibility(View.VISIBLE);
                            leftItemView.setVisibility(View.GONE);
                            rightItemView.setVisibility(View.VISIBLE);
                            oneLineItemView.setVisibility(View.GONE);
                            rightItemView.bindData(item.getRightItem());
                            break;*/
                        case ONE_LINE://one item just contains one item and match it.
                            leftRightItemGroup.setVisibility(View.GONE);
                            oneLineItemView.setVisibility(View.VISIBLE);
                            oneLineItemView.bindData(item.getLeftItem());
                            break;
                        default:
                            break;
                    }
                    if(itemView != null && convertView instanceof LinearLayout){
                        ((LinearLayout)convertView).addView(itemView);
                    }
                }
            }
        }
        return convertView;
    }

    @Override
    public void onItemInfoViewSelected(AppInfoView infoView) {
        mSelectInfoView = infoView;
        Logger.d("AppListAdapter", "onItemInfoViewSelected()[infoView="+infoView+"]");
    }

    public AppInfoView getSelectedItem(){
        return mSelectInfoView;
    }
    
    public boolean exchange(AppInfoView dragView,AppInfoView dropView){
        DragItemInto src = dragView.getDragItemInfo();
        DragItemInto dst = dropView.getDragItemInfo();
        if(src != null && dst != null){
            if(src.equals(dst)){
                return false;
            }
            AppLiteInfo srcInfo = getItem(src.pos0).get(src.pos1).getItemByType(src.itemType);
            AppLiteInfo dstInfo = getItem(dst.pos0).get(dst.pos1).getItemByType(dst.itemType);
            if(srcInfo != null && dstInfo != null && srcInfo.getSize() == dstInfo.getSize()){
                int srcSortPosition = srcInfo.getSortPositon();
                int dstSortPosition = dstInfo.getSortPositon();
                dstInfo.setSortPositon(srcSortPosition);
                srcInfo.setSortPositon(dstSortPosition);
                AppHelper.saveAppLiteInfo(srcInfo);
                AppHelper.saveAppLiteInfo(dstInfo);
                return true;
            }
            else if(srcInfo != null && dstInfo == null){
                AppItem dstItem = getItem(dst.pos0).get(dst.pos1);
                if (dstItem.getLeftItem() == null && dstItem.getRightItem() != null) {
                    srcInfo.setSortPositon(dstItem.getRightItem().getSortPositon());
                }
                if (dstItem.getRightItem() == null && dstItem.getLeftItem() != null) {
                    srcInfo.setSortPositon(dstItem.getLeftItem().getSortPositon());
                }
                AppHelper.saveAppLiteInfo(srcInfo);
                return true;
            }
        }
        return false;
    }
    
    private void playExchangeAnimation(AppInfoView dragView,AppInfoView dropView,AnimationListener listener){
        if(dragView == null || dropView == null){
            return;
        }
        int locationDragView[] = new int[2];
        dragView.getLocationOnScreen(locationDragView);
        int locationDropView[] = new int[2];
        dropView.getLocationOnScreen(locationDropView);
        TranslateAnimation dragViewAnim = new TranslateAnimation(0,
                locationDropView[0] - locationDragView[0], 0, locationDropView[1]
                        - locationDragView[1]);
        dragViewAnim.setDuration(5000);
        dragViewAnim.setFillAfter(true);
        dragViewAnim.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mDragViewAnimContianer.removeAllViews();
            }
        });
//        dragView.startAnimation(dragViewAnim);
        
        mDragViewAnimContianer.removeAllViews();
        mDragViewAnimContianer.setX(locationDragView[0]);
        mDragViewAnimContianer.setY(locationDragView[1]);
        mDragViewAnimContianer.addView(createCacheView(dragView));
        mDragViewAnimContianer.postInvalidate();
        mDragViewAnimContianer.bringToFront();
        dragView.setVisibility(View.INVISIBLE);
        mDragViewAnimContianer.startAnimation(dragViewAnim);
        
//        TranslateAnimation dropViewAnim = new TranslateAnimation(0,
//                locationDragView[0] - locationDropView[0], 0, locationDragView[1]
//                        - locationDropView[1]);
//        dropViewAnim.setDuration(5000);
//        dropViewAnim.setFillAfter(true);
////        dropView.startAnimation(dropViewAnim);
//        mDropViewAnimContianer.removeAllViews();
//        mDropViewAnimContianer.setX(locationDropView[0]);
//        mDropViewAnimContianer.setY(locationDropView[1]);
//        mDropViewAnimContianer.addView(createCacheView(dropView));
//        mDropViewAnimContianer.postInvalidate();
//        mDropViewAnimContianer.bringToFront();
//        dropView.setVisibility(View.INVISIBLE);
//        mDropViewAnimContianer.startAnimation(dropViewAnim);
//        
//        ScaleAnimation scaleIn = (ScaleAnimation) AnimationUtils.loadAnimation(LauncherApp.getAppContext(), R.anim.scale_in);
//        scaleIn.setFillAfter(true);
//        dropView.startAnimation(dragViewAnim);
    }

    private ImageView createCacheView(View itemView){
        itemView.destroyDrawingCache();
        itemView.setDrawingCacheEnabled(true);
        Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());
        ImageView iv = new ImageView(LauncherApp.getAppContext());
        iv.setImageBitmap(bm);
        return iv;
    }
    
    @Override
    public void clearSelected() {
        mSelectInfoView = null;
    }
}

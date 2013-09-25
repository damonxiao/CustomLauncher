
package com.seuic.launcher.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.seuic.launcher.R;
import com.seuic.launcher.data.AppItem;
import com.seuic.launcher.data.AppItem.ItemType;
import com.seuic.launcher.util.Logger;
import com.seuic.launcher.widget.AppInfoView.DragItemInto;

import java.util.List;

public class AppListAdapter extends BaseAdapter implements AppInfoView.AppItemSelectedListener{
    
    private List<List<AppItem>> mItems;

    private LayoutInflater mInflater;
    
    private AppInfoView mSelectInfoView;
    
    public AppListAdapter(List<List<AppItem>> items, Context context) {
        super();
        this.mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                    leftItemView.setDragItemInfo(new DragItemInto(position, count, itemType));
                    rightItemView.setDragItemInfo(new DragItemInto(position, count, itemType));
                    oneLineItemView.setDragItemInfo(new DragItemInto(position, count, itemType));
                    View leftRightItemGroup = itemView.findViewById(R.id.app_left_right_item_group);
                    switch (itemType) {
                        case LEFT_RIGHT://one item contains two item
                            if(item.getRightItem() == null){
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
                            }
                            break;
                        case LEFT://one item just contains LEFT
                            leftRightItemGroup.setVisibility(View.VISIBLE);
                            leftItemView.setVisibility(View.VISIBLE);
                            rightItemView.setVisibility(View.INVISIBLE);
                            oneLineItemView.setVisibility(View.GONE);
                            leftItemView.bindData(item.getLeftItem());
                            break;
                        case RIGHT://one item just contains RIGHT
                            leftRightItemGroup.setVisibility(View.VISIBLE);
                            leftItemView.setVisibility(View.INVISIBLE);
                            rightItemView.setVisibility(View.VISIBLE);
                            oneLineItemView.setVisibility(View.GONE);
                            rightItemView.bindData(item.getRightItem());
                            break;
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
    
    public void clearSelectedItem(){
        mSelectInfoView = null;
    }
    
    public void exchage(AppInfoView dragView,AppInfoView dropView){
        DragItemInto src = dragView.getDragItemInfo();
        DragItemInto dst = dropView.getDragItemInfo();
        if(src != null && dst != null){
            if(src.equals(dst)){
                return;
            }
            AppItem srcInfo = getItem(src.pos0).get(src.pos1);
            AppItem dstInfo = getItem(dst.pos0).get(dst.pos1);
            getItem(dst.pos0).set(dst.pos1, srcInfo);
            getItem(src.pos0).set(src.pos1, dstInfo);
            notifyDataSetChanged();
        }
    }
}

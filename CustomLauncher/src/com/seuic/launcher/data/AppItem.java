package com.seuic.launcher.data;



public class AppItem {
    public enum ItemType{
        LEFT_RIGHT,LEFT,RIGHT,ONE_LINE
    }
    
    private ItemType itemType = ItemType.LEFT;
    private AppInfo leftItem;
    private AppInfo rightItem;
    
    public ItemType getItemType() {
        return itemType;
    }
    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
    public AppInfo getLeftItem() {
        return leftItem;
    }
    public void setLeftItem(AppInfo leftItem) {
        this.leftItem = leftItem;
    }
    public AppInfo getRightItem() {
        return rightItem;
    }
    public void setRightItem(AppInfo rightItem) {
        this.rightItem = rightItem;
    }
    @Override
    public String toString() {
        return "AppItem [itemType=" + itemType + ", leftItem=" + leftItem + ", rightItem="
                + rightItem + "]";
    }
    
}

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
    
    public void setItemByType(AppInfo item,ItemType itemType){
        switch (itemType) {
            case LEFT:
            case LEFT_RIGHT:
            case ONE_LINE:
            default:
                leftItem = item;
                break;
            case RIGHT:
                rightItem = item;
                break;
        }
    }
    
    public AppInfo getItemByType(ItemType itemType){
        switch (itemType) {
            case LEFT:
            case LEFT_RIGHT:
            case ONE_LINE:
            default:
                return leftItem;
            case RIGHT:
                return rightItem;
        }
    }
    
    @Override
    public String toString() {
        return "AppItem [itemType=" + itemType + ", leftItem=" + leftItem + ", rightItem="
                + rightItem + "]";
    }
    
}

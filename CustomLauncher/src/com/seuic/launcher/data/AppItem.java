package com.seuic.launcher.data;



public class AppItem {
    public enum ItemType {
        LEFT_RIGHT,LEFT, RIGHT, ONE_LINE
    }
    
    /*
     * to mark the current APP item is left only,rightonly,both left and right
     * or match line.
     */
    private ItemType itemType = ItemType.LEFT_RIGHT;
    private AppLiteInfo leftItem;
    private AppLiteInfo rightItem;
    
    public ItemType getItemType() {
        return itemType;
    }
    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
    public AppLiteInfo getLeftItem() {
        return leftItem;
    }
    public void setLeftItem(AppLiteInfo leftItem) {
        this.leftItem = leftItem;
    }
    public AppLiteInfo getRightItem() {
        return rightItem;
    }
    public void setRightItem(AppLiteInfo rightItem) {
        this.rightItem = rightItem;
    }
    
    public void setItemByType(AppLiteInfo item,ItemType itemType){
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
    
    public AppLiteInfo getItemByType(ItemType itemType){
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

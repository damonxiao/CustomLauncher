/*
 * HorizontalListView.java v1.5
 *
 * 
 * The MIT License
 * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.seuic.launcher.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Scroller;

import com.seuic.launcher.R;
import com.seuic.launcher.util.Logger;
import com.seuic.launcher.widget.AppInfoView.DragItemInto;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class HorizontalListView extends AdapterView<ListAdapter> implements Runnable {
    private static final String TAG = "HorizontalListView";
    public boolean mAlwaysOverrideTouch = true;
    protected ListAdapter mAdapter;
    private int mLeftViewIndex = -1;
    private int mRightViewIndex = 0;
    protected int mCurrentX;
    protected int mNextX;
    private int mMaxX = Integer.MAX_VALUE;
    private int mDisplayOffset = 0;
    protected Scroller mScroller;
    private GestureDetector mGesture;
    private Queue<View> mRemovedViewQueue = new LinkedList<View>();
    private OnItemSelectedListener mOnItemSelected;
    private OnItemClickListener mOnItemClicked;
    private OnItemLongClickListener mOnItemLongClicked;
    private boolean mDataChanged = false;

    private float mLastDownX = 0f;

    private int mDistance = 0;

    private int mStep = 10;

    private boolean mPositive = false;

    private Rect mTouchFrame;
    private int dragPosition; // 开始拖拽的位置
    private int dropPosition; // 结束拖拽的位置
    private int dragPointX; // 相对于item的x坐标
    private int dragPointY; // 相对于item的y坐标
    private int dragOffsetX;
    private int dragOffsetY;
    private ImageView dragImageView; // 拖动item的preview
    
    private int dragLeftMargin = 200;
    private int dragRightMargin;//initial when get window width

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;

    private int itemHeight;

    boolean flag = false;
    
    private boolean mStartDrag;
    
    public void setLongFlag(boolean temp)
    {
        flag = temp;
    }

    public boolean setOnItemLongClickListener(final MotionEvent ev)
    {
        this.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3)
            {
                // onInterceptTouchEvent(ev);
                // TODO Auto-generated method stub

                int x = (int) ev.getX();
                int y = (int) ev.getY();
                Logger.d(TAG, "onItemLongClick()[x="+x+",y="+y+"]");
                dragPosition = dropPosition = pointToPosition(x, y);
                System.out.println(dragPosition);
                if (dragPosition == AdapterView.INVALID_POSITION)
                {
                }
                /*ViewGroup itemView = (ViewGroup) getChildAt(dragPosition
                        - getFirstVisiblePosition());*/
                AppInfoView itemView = ((AppListAdapter) getAdapter()).getSelectedItem();
                if(itemView == null || itemView.getAppInfo() == null){
                    return false;
                }
                Logger.d(TAG, "onItemLongClick()[itemView.getX()="+itemView.getX()+",itemView.getY()="+itemView.getY()+"]");
                Logger.d(TAG, "onItemLongClick()[itemView.getLeft()="+itemView.getLeft()+",itemView.getTop()="+itemView.getTop()+"]");
                int location[] = new int[2];
                itemView.getLocationOnScreen(location);
                Logger.d(TAG, "onItemLongClick()[itemView.location="+Arrays.toString(location)+"]");
                // 得到当前点在item内部的偏移量 即相对于item左上角的坐标
                dragPointX = x - location[0];
                dragPointY = y - location[1];

                dragOffsetX = (int) (ev.getRawX() - x);
                dragOffsetY = (int) (ev.getRawY() - y);

                itemHeight = itemView.getHeight();

                // 解决问题3
                // 每次都销毁一次cache，重新生成一个bitmap
                itemView.destroyDrawingCache();
                itemView.setDrawingCacheEnabled(true);
                Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());
                // 建立item的缩略图
                createDragView(bm,x,y);
                return false;
            };
        });
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            return setOnItemLongClickListener(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void startDrag()
    {
        if(windowManager == null){
            windowManager = (WindowManager) getContext().getSystemService(
                    Context.WINDOW_SERVICE);// "window"
            dragRightMargin = windowManager.getDefaultDisplay().getWidth()-dragLeftMargin;
        }
        dragImageView.setTag(true);
        windowManager.addView(dragImageView, windowParams);
        mStartDrag = true;
    }
    
    private void createDragView(Bitmap bm, int x, int y){
        stopDrag();
        windowParams = new WindowManager.LayoutParams();
        System.out.println("X: " + x + " dragPointX: " + dragPointX
                + " dragOffsetX: " + dragOffsetX);
        windowParams.gravity = Gravity.TOP | Gravity.LEFT;// 这个必须加
        // 得到preview左上角相对于屏幕的坐标
        windowParams.x = x - dragPointX + dragOffsetX;
        windowParams.y = y - dragPointY + dragOffsetY;
        // 设置宽和高
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        
        ImageView iv = new ImageView(getContext());
        iv.setImageBitmap(bm);
        dragImageView = iv;
    }
    
    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private synchronized void initView() {
        mLeftViewIndex = -1;
        mRightViewIndex = 0;
        mDisplayOffset = 0;
        mCurrentX = 0;
        mNextX = 0;
        mMaxX = Integer.MAX_VALUE;
        mScroller = new Scroller(getContext());
        mGesture = new GestureDetector(getContext(), mOnGesture);
    }

    /**
     * Maps a point to a position in the list.
     * 
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an
     *         item.
     */
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return getFirstVisiblePosition() + i;
                }
            }
        }
        return INVALID_POSITION;
    }

    @Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        mOnItemSelected = listener;
    }

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClicked = listener;
    }

    @Override
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mOnItemLongClicked = listener;
    }

    private DataSetObserver mDataObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            synchronized (HorizontalListView.this) {
                mDataChanged = true;
            }
            invalidate();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            reset();
            invalidate();
            requestLayout();
        }

    };

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View getSelectedView() {
        // TODO: implement
        return null;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataObserver);
        reset();
    }

    private synchronized void reset() {
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    @Override
    public void setSelection(int position) {
        // TODO: implement
    }

    private void addAndMeasureChild(final View child, int viewPos) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }

        addViewInLayout(child, viewPos, params, true);
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
    }

    @Override
    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mAdapter == null) {
            return;
        }

        if (mDataChanged) {
            int oldCurrentX = mCurrentX;
            initView();
            removeAllViewsInLayout();
            mNextX = oldCurrentX;
            mDataChanged = false;
        }

        if (mScroller.computeScrollOffset()) {
            int scrollx = mScroller.getCurrX();
            mNextX = scrollx;
        }

        if (mNextX <= 0) {
            mNextX = 0;
            mScroller.forceFinished(true);
        }
        if (mNextX >= mMaxX) {
            mNextX = mMaxX;
            mScroller.forceFinished(true);
        }

        int dx = mCurrentX - mNextX;

        removeNonVisibleItems(dx);
        fillList(dx);
        positionItems(dx);

        mCurrentX = mNextX;

        if (!mScroller.isFinished()) {
            post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });

        }
    }

    private void fillList(final int dx) {
        int edge = 0;
        View child = getChildAt(getChildCount() - 1);
        if (child != null) {
            edge = child.getRight();
        }
        fillListRight(edge, dx);

        edge = 0;
        child = getChildAt(0);
        if (child != null) {
            edge = child.getLeft();
        }
        fillListLeft(edge, dx);

    }

    private void fillListRight(int rightEdge, final int dx) {
        while (rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {

            View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, -1);
            rightEdge += child.getMeasuredWidth();

            if (mRightViewIndex == mAdapter.getCount() - 1) {
                mMaxX = mCurrentX + rightEdge - getWidth();
            }

            if (mMaxX < 0) {
                mMaxX = 0;
            }
            mRightViewIndex++;
        }

    }

    private void fillListLeft(int leftEdge, final int dx) {
        while (leftEdge + dx > 0 && mLeftViewIndex >= 0) {
            View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, 0);
            leftEdge -= child.getMeasuredWidth();
            mLeftViewIndex--;
            mDisplayOffset -= child.getMeasuredWidth();
        }
    }

    private void removeNonVisibleItems(final int dx) {
        View child = getChildAt(0);
        while (child != null && child.getRight() + dx <= 0) {
            mDisplayOffset += child.getMeasuredWidth();
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mLeftViewIndex++;
            child = getChildAt(0);

        }

        child = getChildAt(getChildCount() - 1);
        while (child != null && child.getLeft() + dx >= getWidth()) {
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mRightViewIndex--;
            child = getChildAt(getChildCount() - 1);
        }
    }

    private void positionItems(final int dx) {
        if (getChildCount() > 0) {
            mDisplayOffset += dx;
            int left = mDisplayOffset;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
                left += childWidth + child.getPaddingRight();
            }
        }
    }

    public synchronized void scrollTo(int x) {
        mScroller.startScroll(mNextX, 0, x - mNextX, 0);
        requestLayout();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled |= mGesture.onTouchEvent(ev);
        
       //here code to play the bounce animation.
        switch (ev.getAction()) {
          case MotionEvent.ACTION_DOWN:
              if (mLastDownX == 0 && mDistance == 0) {
                  mLastDownX = ev.getX();
              }
          case MotionEvent.ACTION_CANCEL:
              break;
          case MotionEvent.ACTION_UP:
              if (mDistance != 0) {
                  mStep = 1;
                  mPositive = (mDistance >= 0);
                  post(this);
                  return true;
              }
          case MotionEvent.ACTION_MOVE:
              if (mLastDownX != 0f) {
                  mDistance = (int) (mLastDownX - ev.getX());
                  if ((mDistance < 0 && mLeftViewIndex == -1 && getChildAt(0).getLeft() == 0)
                          ||
                          (mRightViewIndex == getCount() && mDistance > 0)) {
                      mDistance /= 3;
                      scrollTo(mDistance, 0);
                      return true;
                  }
              }
              break;
          default:
              break;
      }
        
        
        return handled;
    }

    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        synchronized (HorizontalListView.this) {
            mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
        }
        requestLayout();
        return true;
    }

    protected boolean onDown(MotionEvent e) {
        mScroller.forceFinished(true);
        return true;
    }

    private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            onTouchEvent(e);
            return HorizontalListView.this.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            onTouchEvent(e2);
            return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            onTouchEvent(e2);
            if(!mStartDrag){
                synchronized (HorizontalListView.this) {
                    mNextX += (int) distanceX;
                }
                requestLayout();
            }

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (mOnItemClicked != null) {
                        mOnItemClicked.onItemClick(HorizontalListView.this, child, mLeftViewIndex
                                + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    if (mOnItemSelected != null) {
                        mOnItemSelected.onItemSelected(HorizontalListView.this, child,
                                mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    break;
                }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (mOnItemLongClicked != null) {
                        mOnItemLongClicked.onItemLongClick(HorizontalListView.this, child,
                                mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    break;
                }

            }
        }

        private boolean isEventWithinView(MotionEvent e, View child) {
            Rect viewRect = new Rect();
            int[] childPosition = new int[2];
            child.getLocationOnScreen(childPosition);
            int left = childPosition[0];
            int right = left + child.getWidth();
            int top = childPosition[1];
            int bottom = top + child.getHeight();
            viewRect.set(left, top, right, bottom);
            return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Logger.d(TAG, "onTouchEvent()[mLeftViewIndex="
                + mLeftViewIndex + ",mRightViewIndex=" + mRightViewIndex
                + "\nevent.getAction()="+event.getAction()+"]");
        if (dragImageView != null
                && dragPosition != AdapterView.INVALID_POSITION)
        {
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                    if (((AppListAdapter) getAdapter()).getSelectedItem() != null) {
                        onDrag(x, y);
                    } else {
                        stopDrag();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    stopDrag();
                    onDrop(x, y);
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    public int getCount() {
        return getAdapter().getCount();
    }

    private void onDrag(int x, int y)
    {
        AppListAdapter adapter = (AppListAdapter) getAdapter();
        if(adapter.getSelectedItem() != null){
            adapter.getSelectedItem().setVisibility(View.INVISIBLE);
        }
        if (dragImageView != null)
        {
            Object obj = dragImageView.getTag();
            if(obj == null){
                startDrag();
            }
            windowParams.alpha = 0.95f;
            windowParams.x = x - dragPointX + dragOffsetX;
            windowParams.y = y - dragPointY + dragOffsetY;
            windowManager.updateViewLayout(dragImageView, windowParams);
        }
        if(x < dragLeftMargin){
            synchronized (HorizontalListView.this) {
                mNextX -= (int) itemHeight;
            }
            requestLayout();
        }
        if(x > dragRightMargin){
            synchronized (HorizontalListView.this) {
                mNextX += (int) itemHeight;
            }
            requestLayout();
        }
    }

    private void onDrop(int x, int y)
    {
        Logger.d(TAG, "x="+x+",y="+y);
        AppListAdapter adapter = (AppListAdapter) getAdapter();
        Logger.d(TAG, "onDrop=onDrop");
        int tempPosition = pointToPosition(x, y);
        if (tempPosition != AdapterView.INVALID_POSITION)
        {
            dropPosition = tempPosition;
        }
        ViewGroup endGroup = (ViewGroup) getChildAt(dropPosition);
        if(endGroup == null){
            return;
        }
        AppInfoView matchedView = null;
        for (int i = 0; i < endGroup.getChildCount(); i++) {
            ViewGroup subGroup = (ViewGroup) endGroup.getChildAt(i);
            if (subGroup != null) {
                AppInfoView leftItemView = (AppInfoView) subGroup
                        .findViewById(R.id.app_left_item);
                AppInfoView rightItemView = (AppInfoView) subGroup
                        .findViewById(R.id.app_right_item);
                AppInfoView oneLineItemView = (AppInfoView) subGroup
                        .findViewById(R.id.app_one_line_item);
                if (leftItemView.containsPonit(x, y)
                        && (leftItemView.getVisibility() == View.VISIBLE || leftItemView
                                .getVisibility() == View.INVISIBLE)) {
                    matchedView = leftItemView;
                    break;
                }
                if (oneLineItemView.containsPonit(x, y)
                        && (oneLineItemView.getVisibility() == View.VISIBLE || oneLineItemView
                                .getVisibility() == View.INVISIBLE)) {
                    matchedView = oneLineItemView;
                    break;
                }
                if (rightItemView.containsPonit(x, y)
                        && (rightItemView.getVisibility() == View.VISIBLE || rightItemView
                                .getVisibility() == View.INVISIBLE)) {
                    matchedView = rightItemView;
                    break;
                }
            }
        }
        boolean exchanged = false;
        if(matchedView != null && adapter.getSelectedItem() != null){
            DragItemInto src = adapter.getSelectedItem().getDragItemInfo();
            DragItemInto dst = matchedView.getDragItemInfo();
            if(src != null && dst != null && !src.equals(dst)){
                exchanged = adapter.exchange(adapter.getSelectedItem(), matchedView);
            }
        }
        if (!exchanged && adapter.getSelectedItem() != null) {
            adapter.getSelectedItem().setVisibility(View.VISIBLE);
        }
        adapter.clearSelected();
    }

    private void stopDrag()
    {
        if (dragImageView != null && dragImageView.getTag() != null)
        {
            windowManager.removeView(dragImageView);
        }
        if(dragImageView != null){
            dragImageView = null;
        }
        mStartDrag = false;
    }

    @Override
    public void run() {
        mDistance += mDistance > 0 ? -mStep : mStep;
        scrollTo(mDistance, 0);
        if ((mPositive && mDistance <= 0) || (!mPositive && mDistance >= 0)) {
            scrollTo(0, 0);
            mDistance = 0;
            mLastDownX = 0;
            return;
        }
        mStep += 1;
        postDelayed(this, 10);
    }

}

package com.dylan.uiparts.viewpager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.dylan.uiparts.viewpager.transforms.DefaultTransformer;

public class VerticalViewPager extends ViewPager {

    private boolean mCanScroll = true;
    public VerticalViewPager(Context context) {
        this(context, null);
    }
    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPageTransformer(false, new DefaultTransformer());
    }

    public void setCanScroll(boolean isCanScroll) {
        this.mCanScroll = isCanScroll;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCanScroll) {
            return super.onInterceptTouchEvent(event);
        } else {
            return true;
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mCanScroll) {
            boolean intercept = super.onInterceptTouchEvent(swapTouchEvent(event));
            swapTouchEvent(event);
            return intercept;
        } else {
            return false;
        }
    }
    private MotionEvent swapTouchEvent(MotionEvent event) {
        float width = getWidth();
        float height = getHeight();
        float swappedX = (event.getY() / height) * width;
        float swappedY = (event.getX() / width) * height;
        event.setLocation(swappedX, swappedY);
        return event;
    }
}

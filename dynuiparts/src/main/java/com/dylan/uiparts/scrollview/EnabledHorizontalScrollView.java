package com.dylan.uiparts.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class EnabledHorizontalScrollView extends HorizontalScrollView {

    public EnabledHorizontalScrollView(Context context) {
        super(context);
    }
    public EnabledHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EnabledHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    private boolean mEnable = true;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mEnable)return false;
        return super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mEnable)return false;
        return super.onTouchEvent(ev);
    }

    public void scrollX(int value) {
        super.scrollTo(value, getScrollY());
    }
    public void scroll(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    public void setScrollX(int value) {
        if (!mEnable)return;
        super.setScrollX(value);
    }
    @Override
    public void scrollTo(int x, int y) {
        if (!mEnable)return;
        super.scrollTo(x, y);
    }
    @Override
    public void scrollBy(int x, int y) {
        if (!mEnable)return;
        super.scrollBy(x, y);
    }
}

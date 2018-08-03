package com.dylan.uiparts.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class EnabledScrollView extends ScrollView {
    public EnabledScrollView(Context context) {
        super(context);
    }
    public EnabledScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EnabledScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
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
}

package com.dylan.uiparts.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class InnerViewPager extends ViewPager {

    public InnerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupMeasure();
    }
    public InnerViewPager(Context context) {
        super(context);
        setupMeasure();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isFocusable()) {
            return super.dispatchTouchEvent(ev);
        } else {
            return false;
        }
    }

    private void setupMeasure() {
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                requestLayout();
            }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;     //实现高度按照当前页面自动调整
        int index = getCurrentItem();
        View child = index >= 0 ? getChildAt(index) : null;
        if (child != null) {
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            height = child.getMeasuredHeight();
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

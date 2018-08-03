package com.dylan.uiparts.viewpager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class StaticViewPager extends ViewPager {

	private boolean mCanScroll = true;

	public StaticViewPager(Context context) {
		super(context);
	}

	public StaticViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setCanScroll(boolean isCanScroll) {
		this.mCanScroll = isCanScroll;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mCanScroll) {
			return super.onTouchEvent(event);
		} else {
			return true;
		}
	}
	
	@Override  
    public boolean onInterceptTouchEvent(MotionEvent arg0) {  
        if (mCanScroll)  
        	return super.onInterceptTouchEvent(arg0); 
        else  
        	return false;  
    }  
}

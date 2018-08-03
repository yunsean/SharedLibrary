package com.dylan.uiparts.pullable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class PullableScrollView extends ScrollView implements Pullable {

	private GestureDetector mGestureDetector;  
	public PullableScrollView(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(getContext(), new YScrollDetector());  
	}
	public PullableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(getContext(), new YScrollDetector());  
	}
	public PullableScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mGestureDetector = new GestureDetector(getContext(), new YScrollDetector());  
	}

	@Override
	public boolean canPullDown() {
		if (getScrollY() == 0)
			return true;
		else
			return false;
	}

	@Override
	public boolean canPullUp() {
		if (getScrollY() >= (getChildAt(0).getHeight() - getMeasuredHeight()))
			return true;
		else
			return false;
	}

	@Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {  
        boolean result = super.onInterceptTouchEvent(ev);  
        mGestureDetector.onTouchEvent(ev);
        return result;
    } 
	class YScrollDetector extends SimpleOnGestureListener {  
        @Override  
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {  
            if (Math.abs(distanceY) >= Math.abs(distanceX)) {  
                return true;  
            } else { 
            	return false; 
            }
        }  
    } 
}

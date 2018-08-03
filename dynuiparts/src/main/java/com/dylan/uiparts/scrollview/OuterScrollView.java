package com.dylan.uiparts.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ScrollView;

public class OuterScrollView extends ScrollView {
	private GestureDetector mGestureDetector;  

	public OuterScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public OuterScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(getContext(), new YScrollDetector());  
        setFadingEdgeLength(0);  
	}
	public OuterScrollView(Context context) {
		super(context);
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

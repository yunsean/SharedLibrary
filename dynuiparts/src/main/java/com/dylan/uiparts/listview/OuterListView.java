package com.dylan.uiparts.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ListView;

public class OuterListView extends ListView {
    private GestureDetector mGestureDetector;  

	public OuterListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public OuterListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(getContext(), new YScrollDetector());  
        setFadingEdgeLength(0);  
	}

	public OuterListView(Context context) {
		super(context);
	}
	
	@Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {  
        super.onInterceptTouchEvent(ev);  
        return mGestureDetector.onTouchEvent(ev);  
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

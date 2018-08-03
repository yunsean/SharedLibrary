package com.dylan.uiparts.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class InnerWebView extends WebView {
	private GestureDetector mGestureDetector = null;

    public InnerWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);
        
        getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);  
        setVerticalScrollBarEnabled(false);  
        setVerticalScrollbarOverlay(false);  
        setHorizontalScrollBarEnabled(false);  
        setHorizontalScrollbarOverlay(false);  
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    class YScrollDetector extends SimpleOnGestureListener {
    	@Override
    	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceY) > Math.abs(distanceX)) {
            	return true;
            }
            return false;
    	}
    }
}

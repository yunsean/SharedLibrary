package com.dylan.uiparts.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class InnerListView extends ListView {

	public InnerListView(Context context, AttributeSet attrs) { 
        super(context, attrs); 
    } 
    public InnerListView(Context context) { 
        super(context); 
    } 
    public InnerListView(Context context, AttributeSet attrs, int defStyle) { 
        super(context, attrs, defStyle); 
    } 

    @Override 
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST); 
        super.onMeasure(widthMeasureSpec, expandSpec); 
    } 
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    		if (isFocusable()) {
    			return super.dispatchTouchEvent(ev);
    		} else {
    			return false;
    		}
    }

    public static void setListViewHeightBasedOnItems(ListView listView) {  
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {  
            return;  
        }  
        int count = listAdapter.getCount();
        int totalHeight = 0;  
        for (int i = 0; i < count; i++) {  
            View item = listAdapter.getView(i, null, listView);  
            item.measure(0, 0);  
            int height = item.getMeasuredHeight();  
            totalHeight += height;  
            item = null;  
        }  
        ViewGroup.LayoutParams params = listView.getLayoutParams();  
        params.height = totalHeight + (listView.getDividerHeight() * (count - 1));
        params.height += listView.getPaddingTop() + listView.getPaddingBottom();
        listView.setLayoutParams(params); 
    }  
}

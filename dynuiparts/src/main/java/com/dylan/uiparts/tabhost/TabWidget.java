package com.dylan.uiparts.tabhost;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

public class TabWidget extends android.widget.TabWidget {
    private OnTabSelectionChanged mSelectionChangedListener;
    private int mSelectedTab = -1;
    
    public TabWidget(Context context) {
        super(context);
    }    
    public TabWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setTabSelectionListener(OnTabSelectionChanged listener) {
        mSelectionChangedListener = listener;
    }
    
    @Override
    public void setCurrentTab(int index) {
        super.setCurrentTab(index);
        mSelectedTab = index;
    }
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this && hasFocus && getTabCount() > 0) {
            getChildTabViewAt(mSelectedTab).requestFocus();
            return;
        }
        if (hasFocus) {
            int i = 0;
            int numTabs = getTabCount();
            while (i < numTabs) {
                if (getChildTabViewAt(i) == v) {
                    setCurrentTab(i);
                    mSelectionChangedListener.onTabSelectionChanged(i, false);
                    if (isShown())  {
                        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                    }
                    break;
                }
                i++;
            }
        }
    }
    
    public void addView(View child)  {
        super.addView(child);
        child.setOnClickListener(new TabClickListener(getTabCount() - 1));
        child.setOnFocusChangeListener(this);
    }
    
    private class TabClickListener implements OnClickListener {
        private final int mTabIndex;
        private TabClickListener(int tabIndex) {
            mTabIndex = tabIndex;
        }
        public void onClick(View v) {
            mSelectionChangedListener.onTabSelectionChanged(mTabIndex, true);
        }
    }
    
    public static interface OnTabSelectionChanged {
        void onTabSelectionChanged(int tabIndex, boolean clicked);
    }
}

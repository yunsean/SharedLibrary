package com.dylan.uiparts.gridview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;

import com.dylan.common.utils.Utility;

public class RowedGridView extends GridView {

    public RowedGridView(Context context) {
        super(context);
        init();
    }
    public RowedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public RowedGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private static enum ScrollDirection{None, Up, Down};
    private void init() {
        setOnScrollListener(new OnScrollListener() {
            private boolean afterTouchScroll = false;
            private int positionY = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (!afterTouchScroll)return;
                    afterTouchScroll = false;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ScrollDirection scrollDirection = ScrollDirection.None;
                            if (_getScrollY() > positionY)scrollDirection = ScrollDirection.Down;
                            else scrollDirection = ScrollDirection.Up;
                            int deltaY = _getScrollY() % _getRowHeight();
                            if (scrollDirection == ScrollDirection.Down) {
                                smoothScrollBy(_getRowHeight() - deltaY, 300);
                            } else {
                                if (deltaY > _getItemHeight())deltaY -= _getRowHeight();
                                smoothScrollBy(-deltaY, 300);
                            }
                        }
                    }, 100);
                } else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    afterTouchScroll = true;
                    positionY = _getScrollY();
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    public void scrollTo(int position) {
        int row = position / getNumColumns();
        int wantY = row * _getRowHeight();
        int realY = _getScrollY();
        smoothScrollBy(wantY - realY, 300);
    }

    private int _getScrollY() {
        if (getChildCount() < 1) return 0;
        View c = getChildAt(0);
        if (c == null) return 0;
        int firstVisiblePosition = getFirstVisiblePosition();
        int firstVisibleRow = firstVisiblePosition / getNumColumns();
        int top = c.getTop();
        return -top + firstVisibleRow * (c.getHeight() + _getVerticalSpacing());
    }
    private int _getRowHeight() {
        if (getChildCount() < 1) return 0;
        View c = getChildAt(0);
        if (c == null) return 0;
        return c.getHeight() + _getVerticalSpacing();
    }
    private int _getItemHeight() {
        if (getChildCount() < 1) return 0;
        View c = getChildAt(0);
        if (c == null) return 0;
        return c.getHeight();
    }
    private int _getVerticalSpacing() {
        int verticalSpacing = 0;
        if (Utility.isJellyBeanOrLater()) {
            verticalSpacing = getVerticalSpacing();
        }
        return verticalSpacing;
    }
}

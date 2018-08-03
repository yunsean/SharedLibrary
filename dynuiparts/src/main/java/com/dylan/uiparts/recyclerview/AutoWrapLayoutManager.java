package com.dylan.uiparts.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class AutoWrapLayoutManager extends RecyclerView.LayoutManager {

    public AutoWrapLayoutManager() {
        super();
        setAutoMeasureEnabled(true);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        int sumWidth = getWidth();
        int curLineWidth = 0;
        int curLineTop = 0;
        int lastLineMaxHeight = 0;
        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);
            curLineWidth += width;
            if (curLineWidth <= sumWidth) {
                layoutDecorated(view, curLineWidth - width, curLineTop, curLineWidth, curLineTop + height);
                lastLineMaxHeight = Math.max(lastLineMaxHeight, height);
            } else {
                curLineWidth = width;
                if (lastLineMaxHeight == 0) lastLineMaxHeight = height;
                curLineTop += lastLineMaxHeight;
                layoutDecorated(view, 0, curLineTop, width, curLineTop + height);
                lastLineMaxHeight = height;
            }
        }
    }
}

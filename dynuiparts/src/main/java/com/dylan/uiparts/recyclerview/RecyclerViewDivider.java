package com.dylan.uiparts.recyclerview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RecyclerViewDivider extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL = LinearLayoutManager.VERTICAL;

    private Paint paint;
    private int orientation;
    private int color;
    private int size;
    private int marginStart;
    private int marginEnd;
    private boolean drawFirstLine = true;
    private boolean drawLastLine = true;

    public RecyclerViewDivider() {
        this(VERTICAL);
    }
    public RecyclerViewDivider(int orientation) {
        this.orientation = orientation;
        paint = new Paint();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }

    public RecyclerViewDivider setColor(int color) {
        this.color = color;
        paint.setColor(color);
        return this;
    }
    public RecyclerViewDivider setSize(int size) {
        this.size = size;
        return this;
    }
    public RecyclerViewDivider setMarginStart(int marginStart) {
        this.marginStart = marginStart;
        return this;
    }
    public RecyclerViewDivider setMarginEnd(int marginEnd) {
        this.marginEnd = marginEnd;
        return this;
    }
    public RecyclerViewDivider setMargin(int marginStart, int marginEnd) {
        this.marginStart = marginStart;
        this.marginEnd = marginEnd;;
        return this;
    }

    public RecyclerViewDivider setDrawFirstLine(boolean drawFirstLine) {
        this.drawFirstLine = drawFirstLine;
        return this;
    }
    public RecyclerViewDivider setDrawLastLine(boolean drawLastLine) {
        this.drawLastLine = drawLastLine;
        return this;
    }
    public RecyclerViewDivider setDrawLine(boolean drawFirstLine, boolean drawLastLine) {
        this.drawLastLine = drawLastLine;
        this.drawFirstLine = drawFirstLine;
        return this;
    }

    protected void drawVertical(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop() + marginStart;
        final int bottom = parent.getHeight() - parent.getPaddingBottom() - marginEnd;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + size;
            c.drawRect(left, top, right, bottom, paint);
        }
    }

    protected void drawHorizontal(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft() + marginStart;
        final int right = parent.getWidth() - parent.getPaddingRight() - marginEnd;
        final int childCount = parent.getChildCount();
        for (int i = (drawFirstLine ? 0 : 1); i < (drawLastLine ? childCount : childCount - 1); i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + size;
            c.drawRect(left, top, right, bottom, paint);
        }
    }
}

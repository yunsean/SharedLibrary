package com.dylan.uiparts.gridview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import com.dylan.uiparts.R;

public class LineGridView extends GridView {
	
	private int mLineColor = Color.LTGRAY;
    private boolean mDrawLeft = false;
    private boolean mDrawRight = false;
    private boolean mDrawSpace = false;
    private boolean mAutoHeight = true;
	public LineGridView(Context context) {
        this(context, null);
    }
    public LineGridView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.LineGridViewStyle);
    }
    public LineGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LineGridView, defStyle, 0);
        mLineColor = a.getColor(R.styleable.LineGridView_lgv_lineColor, Color.LTGRAY);
		a.recycle();
    }
    public void setLineColor(int color) {
    	mLineColor = color;
    }
    public void setDrawLeft(boolean drawLeft) {
        this.mDrawLeft = drawLeft;
    }
    public void setDrawRight(boolean drawRight) {
        this.mDrawRight = drawRight;
    }
    public void setDrawSpace(boolean drawSpace) {
        this.mDrawSpace = drawSpace;
    }
    public void setAutoHeight(boolean autoHeight) {
        this.mAutoHeight = autoHeight;
    }

    @Override
    protected void dispatchDraw(Canvas canvas){
        super.dispatchDraw(canvas);
        View localView1 = getChildAt(0);
        int column = (localView1 != null && localView1.getWidth() > 0) ? (getWidth() / localView1.getWidth()) : getWidth();
        int childCount = getChildCount();
        Paint localPaint;
        localPaint = new Paint();
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setColor(mLineColor);
        for (int i = 0;i < childCount;i++){
            View cellView = getChildAt(i);
            if (i < column) {                //第一行
                canvas.drawLine(cellView.getLeft(), cellView.getTop() + 1, cellView.getRight(), cellView.getTop() + 1, localPaint);
            }
            if (mDrawLeft && (i % column == 0)) {   //第一列
                canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getLeft(), cellView.getBottom(), localPaint);
            }
            if ((i + 1) % column == 0) {    //最后一列
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
                if (mDrawRight) canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint);
            } else if ((i + 1) > (childCount - (childCount % column))) {    //最后一行
                canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint);
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
            } else {
                canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint);
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
            }
        }
        if (mDrawSpace && childCount % column != 0){
            for (int j = 0; j <= (column - childCount % column); j++){
                View lastView = getChildAt(childCount - 1);
                if (mDrawRight || j != (column - childCount % column)) canvas.drawLine(lastView.getRight() + lastView.getWidth() * j, lastView.getTop(), lastView.getRight() + lastView.getWidth() * j, lastView.getBottom(), localPaint);
                canvas.drawLine(lastView.getLeft() + lastView.getWidth() * j, lastView.getBottom(), lastView.getRight() + lastView.getWidth() * j, lastView.getBottom(), localPaint);
            }
        }
    }
    @Override 
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAutoHeight) {
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}

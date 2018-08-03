package com.dylan.player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

public class VUMeterView extends View {
    private boolean mVertical = false;
    private int mGapWidth = 2;
    private int mBarWidth = 4;
    private int mMargin = 0;
    private boolean mSmallGap = true;
    private ShapeDrawable mDrawable = null;
    private Paint mPaint = null;
    private double mLevel = 0.0;
    final int[] segmentColors = {
            0xff5555ff,
            0xff5555ff,
            0xff00ff00,
            0xff00ff00,
            0xff00ff00,
            0xff00ff00,
            0xffffff00,
            0xffffff00,
            0xffff0000,
            0xffff0000};
    final int segmentOffColor = 0xff777777;
    public VUMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public VUMeterView(Context context) {
        super(context);
    }

    public void setVertical(boolean vertical) {
        mVertical = vertical;
    }
    public void setGapWidth(int gapWidth) {
        mGapWidth = gapWidth / 2;
    }
    public void setMargin(int margin) {
        this.mMargin = margin;
    }
    public void setLevel(double level) {
        mLevel = level;
        invalidate();
    }
    public double getLevel() {
        return mLevel;
    }

    private int latestEndSegment = -1;
    private void drawBar(Canvas canvas) {
        if (mVertical) {
            int height = getHeight();
            int width = getWidth () - (mMargin << 1);
            int segmentHeight = (int) (Math.floor((getHeight() + mGapWidth) / segmentColors.length)) - mGapWidth;
            if (mDrawable == null) {
                mDrawable = new ShapeDrawable(new RectShape());
                mPaint = mDrawable.getPaint();
            }
            int y = height;
            for (int i = 0; i < segmentColors.length; i++) {
                if (mSmallGap) {
                    int minHeight = y - segmentHeight;
                    while (y > minHeight) {
                        if ((mLevel * height) < y) mPaint.setColor(segmentColors[i]);
                        else mPaint.setColor(segmentOffColor);
                        mDrawable.setBounds(mMargin, y - mBarWidth, mMargin + width, y);
                        mDrawable.draw(canvas);
                        y -= mBarWidth + mGapWidth;
                    }
                } else {
                    if ((mLevel * segmentColors.length) > (i + 0.5)) mPaint.setColor(segmentColors[i]);
                    else mPaint.setColor(segmentOffColor);
                    mDrawable.setBounds(mMargin, y - segmentHeight, mMargin + width, y);
                    mDrawable.draw(canvas);
                    y -= segmentHeight + mGapWidth;
                }
            }
        } else {
            int width = getWidth();
            int height = getHeight() - (mMargin << 1);
            int segmentWidth = (int) (Math.floor((getWidth() + mGapWidth) / segmentColors.length)) - mGapWidth;
            if (mDrawable == null) {
                mDrawable = new ShapeDrawable(new RectShape());
                mPaint = mDrawable.getPaint();
            }
            int x = 0;
            for (int i = 0; i < segmentColors.length; i++) {
                if (mSmallGap) {
                    int maxWidth = x + segmentWidth;
                    while (x < maxWidth) {
                        if ((mLevel * width) > x) mPaint.setColor(segmentColors[i]);
                        else mPaint.setColor(segmentOffColor);
                        mDrawable.setBounds(x, mMargin, x + mBarWidth, mMargin + height);
                        mDrawable.draw(canvas);
                        x += mBarWidth + mGapWidth;
                    }
                } else {
                    if ((mLevel * segmentColors.length) > (i + 0.5))
                        mPaint.setColor(segmentColors[i]);
                    else mPaint.setColor(segmentOffColor);
                    mDrawable.setBounds(x, mMargin, x + segmentWidth, mMargin + height);
                    mDrawable.draw(canvas);
                    x += segmentWidth + mGapWidth;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBar(canvas);
    }
}

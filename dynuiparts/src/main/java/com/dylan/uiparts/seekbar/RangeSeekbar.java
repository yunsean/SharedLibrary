package com.dylan.uiparts.seekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.dylan.common.utils.Utility;

public class RangeSeekbar extends AbsSeekbar {
    private Drawable leftThumbDrawable;
    private Drawable rightThumbDrawable;
    private Rect sharedTextBounds = new Rect();
    private Rect leftThumbBounds = new Rect();
    private Rect rightThumbBounds = new Rect();
    private int activeThumb = -1;
    private float minValue = 0f;
    private float maxValue = 100f;

    @Nullable
    private OnValueChangeListener valueSetListener;

    private static final int THUMB_LEFT = 0;
    private static final int THUMB_RIGHT = 1;
    private final int TEXT_TOP = 0;
    private final int TEXT_BOTTOM = 1;
    private static int LEFT_TEXT_POSITION = 0;
    private static int RIGHT_TEXT_POSITION = 0;
    private float x1;
    private float x2;

    public RangeSeekbar(Context context) {
        super(context);
        init(context, null);
    }

    public RangeSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RangeSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        minValue = rangeMinValue;
        maxValue = rangeMaxValue;
        leftThumbDrawable = getThumbDrawable();
        rightThumbDrawable = leftThumbDrawable.getConstantState()
                .newDrawable();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                return handleDownEvent(event);
            case MotionEvent.ACTION_MOVE:
                x2 = event.getX();
                if (x1 - x2 > 1) {
                    if (leftThumbBounds.centerX() == rightThumbBounds.centerX()) {
                        activeThumb = THUMB_LEFT;
                        x1 = x2;
                    }
                }
                if (x2 - x1 > 1) {
                    if (leftThumbBounds.centerX() == rightThumbBounds.centerX()) {
                        activeThumb = THUMB_RIGHT;
                        x1 = x2;
                    }
                }
                return handleMoveEvent(event);
            case MotionEvent.ACTION_UP:
                return handleUpEvent();
            default:
                return super.onTouchEvent(event);
        }
    }

    private boolean handleUpEvent() {
        if (valueSetListener != null) {
            if (activeThumb == THUMB_LEFT) {
                valueSetListener.onMinValueChange(minValue);
            } else if (activeThumb == THUMB_RIGHT) {
                valueSetListener.onMaxValueChange(maxValue);
            }
        }

        activeThumb = -1;
        invalidate();
        return true;
    }

    private boolean handleMoveEvent(MotionEvent event) {
        int radius = leftThumbBounds.width() / 2;
        float newX = (int) event.getX() - radius;

        if (activeThumb == THUMB_LEFT) {
            Rect destination = rightThumbBounds;
            setRectXPosition(leftThumbBounds, (int) newX);
            if (leftThumbBounds.right >= destination.right) {
                setRectXPosition(leftThumbBounds, destination.left);
                minValue = calculateValue((int) (leftThumbBounds.centerX() - getTrackBounds().left));
            } else {
                if (leftThumbBounds.right >= destination.left) {
                    LEFT_TEXT_POSITION = TEXT_BOTTOM;
                } else {
                    LEFT_TEXT_POSITION = TEXT_TOP;
                }
                if (leftThumbBounds.centerX() <= getTrackBounds().left) {
                    setRectXPosition(leftThumbBounds, (int) (getTrackBounds().left - (leftThumbBounds.width() / 2)));
                }
                minValue = Math.max(calculateValue((int) (leftThumbBounds.centerX() - getTrackBounds().left)), rangeMinValue);
            }
        }

        if (activeThumb == THUMB_RIGHT) {
            Rect destination = leftThumbBounds;
            setRectXPosition(rightThumbBounds, (int) newX);
            if (rightThumbBounds.left <= destination.left) {
                setRectXPosition(rightThumbBounds, destination.left);
                maxValue = calculateValue((int) (rightThumbBounds.centerX() - getTrackBounds().left));
            } else {
                if (rightThumbBounds.left <= destination.right) {
                    LEFT_TEXT_POSITION = TEXT_BOTTOM;
                } else {
                    LEFT_TEXT_POSITION = TEXT_TOP;
                }
                if (rightThumbBounds.centerX() >= getTrackBounds().right) {
                    setRectXPosition(rightThumbBounds, (int) (getTrackBounds().right - (rightThumbBounds.width() / 2)));
                }
                maxValue = Math.min(calculateValue((int) (rightThumbBounds.centerX() - getTrackBounds().left)), rangeMaxValue);
            }
        }

        invalidate();
        return true;
    }

    static boolean withinBounds(float x, float y, Rect bounds) {
        return (x > bounds.left && x < bounds.right) &&
                (y > bounds.top && y < bounds.bottom);
    }
    static Rect expandRect(Rect rect, int value) {
        return new Rect(rect.left - value, rect.top - value, rect.right + value, rect.bottom + value);
    }
    private boolean handleDownEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (withinBounds(x, y, expandRect(leftThumbBounds, (int) Utility.dip2px(getContext(), 0)))) {
            activeThumb = THUMB_LEFT;
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
            invalidate();
            return true;
        }
        if (withinBounds(x, y, expandRect(rightThumbBounds, (int) Utility.dip2px(getContext(), 0)))) {
            activeThumb = THUMB_RIGHT;
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        RectF trackBounds = getTrackBounds();
        int rightThumbSize = Math.max(rightThumbDrawable.getIntrinsicWidth(), rightThumbDrawable.getIntrinsicHeight());
        rightThumbBounds.set(rightThumbBounds.left, rightThumbBounds.top, rightThumbBounds.left + rightThumbSize, leftThumbBounds.top + rightThumbSize);
        float v = (maxValue - rangeMinValue) / (rangeMaxValue - rangeMinValue) * trackBounds.width();
        int centerX = (int) (v + trackBounds.left);
        int centerY = (int) trackBounds.centerY();
        setRectCenterX(rightThumbBounds, centerX);
        setRectCenterY(rightThumbBounds, centerY);
        int leftThumbSize = Math.max(leftThumbDrawable.getIntrinsicWidth(), leftThumbDrawable.getIntrinsicHeight());
        leftThumbBounds.set(leftThumbBounds.left, leftThumbBounds.top, leftThumbBounds.left + leftThumbSize, leftThumbBounds.top + leftThumbSize);
        float v1 = (minValue - rangeMinValue) / (rangeMaxValue - rangeMinValue) * trackBounds.width();
        centerX = (int) (v1 + trackBounds.left);
        centerY = (int) trackBounds.centerY();
        setRectCenterX(leftThumbBounds, centerX);
        setRectCenterY(leftThumbBounds, centerY);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDrawThumb(canvas);
        String minValueText = formatValue((int) minValue);
        measureText(minValueText, sharedTextBounds);
        setRectXPosition(sharedTextBounds, leftThumbBounds.centerX() - (sharedTextBounds.width() / 2));
        if (LEFT_TEXT_POSITION == TEXT_TOP) {
            setRectYPosition(sharedTextBounds, leftThumbBounds.top - getValueTextPadding() * 4);
        } else {
            setRectYPosition(sharedTextBounds, leftThumbBounds.bottom + getValueTextPadding());
        }
        drawValueText(canvas, minValueText, sharedTextBounds);
        String maxValueText = formatValue((int) maxValue);
        measureText(maxValueText, sharedTextBounds);
        setRectXPosition(sharedTextBounds, rightThumbBounds.centerX() - (sharedTextBounds.width() / 2));
        if (RIGHT_TEXT_POSITION == TEXT_TOP) {
            setRectYPosition(sharedTextBounds, rightThumbBounds.top - getValueTextPadding() * 4);
        } else {
            setRectYPosition(sharedTextBounds, rightThumbBounds.bottom + getValueTextPadding());
        }
        drawValueText(canvas, maxValueText, sharedTextBounds);
    }

    private void onDrawThumb(Canvas canvas) {
        rightThumbDrawable.setBounds(rightThumbBounds);
        rightThumbDrawable.setState(activeThumb == THUMB_RIGHT ? STATE_PRESSED : STATE_DEFAULT);
        rightThumbDrawable.draw(canvas);
        leftThumbDrawable.setBounds(leftThumbBounds);
        leftThumbDrawable.setState(activeThumb == THUMB_LEFT ? STATE_PRESSED : STATE_DEFAULT);
        leftThumbDrawable.draw(canvas);
    }

    public void setCurrentRange(float min, float max) {
        minValue = Math.max(min, rangeMinValue);
        maxValue = Math.min(max, rangeMaxValue);
        if (valueSetListener != null) {
            valueSetListener.onMinValueChange(minValue);
            valueSetListener.onMaxValueChange(maxValue);
        }

        try {
            requestLayout();
            invalidate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setOnValueChangeListener(@Nullable OnValueChangeListener valueSetListener) {
        this.valueSetListener = valueSetListener;
    }

    @Override
    protected void onDrawTrackDecoration(Canvas canvas, Paint sharedPaint) {
        sharedPaint.reset();
        sharedPaint.setAntiAlias(true);
        sharedPaint.setColor(getTrackFillColor());
        RectF fill = new RectF(leftThumbBounds.centerX(), getTrackBounds().top, rightThumbBounds.centerX(), getTrackBounds().bottom);
        canvas.drawRoundRect(fill, getTrackHeight() / 2, getTrackHeight() / 2, sharedPaint);
    }

    public interface OnValueChangeListener {
        void onMinValueChange(float value);
        void onMaxValueChange(float value);
    }

    @Override
    public void setRangeValue(float min, float max) {
        super.setRangeValue(min, max);
        setCurrentRange(min, max);
    }

    public float getMinValue() {
        return minValue;
    }
    public float getMaxValue() {
        return maxValue;
    }
}
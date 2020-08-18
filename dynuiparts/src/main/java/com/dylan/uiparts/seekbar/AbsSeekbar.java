package com.dylan.uiparts.seekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;

public abstract class AbsSeekbar extends View {
    private Drawable thumbDrawable;
    private int trackHeight = 0;
    private int labelTextPadding = 0;
    private int labelTextSize = 0;
    private int valueTextPadding = 0;
    protected Rect sharedTextBounds = new Rect();
    private RectF trackBounds = new RectF();
    private Rect minLabelBounds = new Rect();
    private Rect maxLabelBounds = new Rect();
    private Paint valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Paint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Paint sharedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int trackColor = 0;
    private int trackFillColor = 0;
    private float progress = 0f;
    private String minLabelText;
    private String maxLabelText;
    static final int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};
    static final int[] STATE_DEFAULT = new int[]{};

    protected float rangeMinValue = 0f;
    protected float rangeMaxValue = 100f;

    private ValueFormatter valueFormatter = new ValueFormatter() {
        @Override
        public String formatValue(float value) {
            return String.valueOf(value);
        }
        @Override
        public String formatValue(int value) {
            return String.valueOf(value);
        }
    };

    public AbsSeekbar(Context context) {
        super(context);
        init(context, null);
    }
    public AbsSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AbsSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        applyAttributes(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        labelPaint.getTextBounds(minLabelText, 0, minLabelText.length(), minLabelBounds);
        labelPaint.getTextBounds(maxLabelText, 0, maxLabelText.length(), maxLabelBounds);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        trackBounds.left = getTrackLeftOffset();
        trackBounds.top = (getMeasuredHeight() / 2) - trackHeight;
        trackBounds.right = getMeasuredWidth() - getTrackRightOffset();
        trackBounds.bottom = trackBounds.top + trackHeight;
        minLabelBounds.offsetTo(labelTextPadding, (int) trackBounds.centerY() - (minLabelBounds.height() / 2));
        maxLabelBounds.offsetTo((getMeasuredWidth() - maxLabelBounds.width()) - labelTextPadding, (int) (trackBounds.centerY() - (minLabelBounds.height() / 2)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
        onDrawTrackDecoration(canvas, sharedPaint);
        drawLabel(canvas, minLabelText, minLabelBounds);
        drawLabel(canvas, maxLabelText, maxLabelBounds);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1f : 0.5f);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        return super.dispatchTouchEvent(event);
    }

    public void setTrackColor(@ColorInt int color) {
        trackColor = color;
    }
    public void setTrackFillColor(@ColorInt int color) {
        trackFillColor = color;
    }

    public void setTrackHeight(int heightInPx) {
        this.trackHeight = heightInPx;
    }

    public void setMinLabelText(@StringRes int text) {
        setMinLabelText(getResources().getString(text));
    }

    public void setMinLabelText(@NonNull String text) {
        minLabelText = text;
    }

    public void setMaxLabelText(@StringRes int text) {
        setMaxLabelText(getResources().getString(text));
    }

    public void setMaxLabelText(@NonNull String text) {
        maxLabelText = text;
    }

    private void setLabelTextSize(float textSize) {
        labelPaint.setTextSize(textSize);
    }

    public void setValueFormatter(@NonNull ValueFormatter formatter) {
        this.valueFormatter = formatter;
    }

    public void setLabelTextPadding(int paddingInPx) {
        labelTextPadding = paddingInPx;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public int getTrackFillColor() {
        return trackFillColor;
    }

    public int getTrackHeight() {
        return trackHeight;
    }

    public RectF getTrackBounds() {
        return trackBounds;
    }

    public void setValueTextPadding(int paddingInPx) {
        valueTextPadding = paddingInPx;
    }

    public void setValueTextSize(float textSize) {
        valuePaint.setTextSize(textSize);
    }

    public int getValueTextPadding() {
        return valueTextPadding;
    }

    public void setThumbDrawable(@DrawableRes int drawable) {
        setThumbDrawable(getResources().getDrawable(drawable));
    }

    public void setThumbDrawable(Drawable drawable) {
        thumbDrawable = drawable;
    }

    public Drawable getThumbDrawable() {
        return thumbDrawable;
    }

     private void applyAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
        String minLabelText = null;
        String maxLabelText = null;
        float labelTextSize = 0;
        int labelTextPadding = 0;
        int trackHeight = 0;
        int trackColor = 0;
        int trackFillColor = 0;
        float valueTextSize = 0;
        int valueTextPadding = 0;
        Drawable thumbDrawable = null;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AbsSeekbar);
            int count = ta.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attr = ta.getIndex(i);
                if (attr == R.styleable.AbsSeekbar_asb_minLabelText) {
                    minLabelText = ta.getString(attr);
                } else if (attr == R.styleable.AbsSeekbar_asb_maxLabelText) {
                    maxLabelText = ta.getString(attr);
                } else if (attr == R.styleable.AbsSeekbar_asb_rangeMin) {
                    rangeMinValue = ta.getFloat(attr, 0);
                } else if (attr == R.styleable.AbsSeekbar_asb_rangeMax) {
                    rangeMaxValue = ta.getFloat(attr, 100);
                } else if (attr == R.styleable.AbsSeekbar_asb_labelTextSize) {
                    labelTextSize = ta.getDimension(attr, Utility.dip2px(getContext(), 12));
                } else if (attr == R.styleable.AbsSeekbar_asb_labelTextPadding) {
                    labelTextPadding = ta.getDimensionPixelSize(attr, (int) Utility.dip2px(getContext(), 4));
                } else if (attr == R.styleable.AbsSeekbar_asb_trackHeight) {
                    trackHeight = ta.getDimensionPixelSize(attr, (int) Utility.dip2px(getContext(), 3));
                } else if (attr == R.styleable.AbsSeekbar_asb_trackColor) {
                    trackColor = ta.getColor(attr, Color.BLACK);
                } else if (attr == R.styleable.AbsSeekbar_asb_trackFillColor) {
                    trackFillColor = ta.getColor(attr, Color.BLACK);
                } else if (attr == R.styleable.AbsSeekbar_asb_valueTextSize) {
                    valueTextSize = ta.getDimension(attr, Utility.dip2px(getContext(), 14));
                } else if (attr == R.styleable.AbsSeekbar_asb_valueTextPadding) {
                    valueTextPadding = ta.getDimensionPixelSize(attr, (int) Utility.dip2px(getContext(), 4));
                } else if (attr == R.styleable.AbsSeekbar_asb_thumbDrawable) {
                    thumbDrawable = ta.getDrawable(attr);
                }
            }
            ta.recycle();
        }

        if (minLabelText == null) minLabelText = "";
        if (maxLabelText == null) maxLabelText = "";
        if (labelTextSize == 0) labelTextSize = Utility.dip2px(getContext(), 12);
        if (labelTextPadding == 0) labelTextPadding = (int) Utility.dip2px(getContext(), 4);
        if (trackHeight == 0) trackHeight = (int) Utility.dip2px(getContext(), 3);
        if (trackColor == 0) trackColor = Color.BLACK;
        if (trackFillColor == 0) trackFillColor = Color.BLACK;
        if (valueTextSize == 0) valueTextSize = Utility.dip2px(getContext(), 14);
        if (valueTextPadding == 0) valueTextPadding = (int) Utility.dip2px(getContext(), 4);
        if (thumbDrawable == null) thumbDrawable = getResources().getDrawable(R.drawable.seekbar_ic_thumb);
        setMinLabelText(minLabelText);
        setMaxLabelText(maxLabelText);
        setLabelTextSize(labelTextSize);
        setLabelTextPadding(labelTextPadding);
        setTrackHeight(trackHeight);
        setTrackColor(trackColor);
        setTrackFillColor(trackFillColor);
        setValueTextSize(valueTextSize);
        setValueTextPadding(valueTextPadding);
        setThumbDrawable(thumbDrawable);
    }

    private void drawLabel(Canvas canvas, String text, Rect bounds) {
        canvas.drawText(text, bounds.left, bounds.bottom, labelPaint);
    }

    private void drawTrack(Canvas canvas) {
        sharedPaint.reset();
        sharedPaint.setAntiAlias(true);
        sharedPaint.setColor(trackColor);
        canvas.drawRoundRect(trackBounds, trackHeight / 2, trackHeight / 2, sharedPaint);
    }

    protected String formatValue(float value) {
        return valueFormatter.formatValue(value);
    }

    protected String formatValue(int value) {
        return valueFormatter.formatValue(value);
    }
    protected void setRectXPosition(Rect rect, int x) {
        int width = rect.width();
        rect.right = (rect.left = x) + width;
    }
    protected void setRectYPosition(Rect rect, int y) {
        int height = rect.height();
        rect.bottom = (rect.top = y) + height;
    }

    protected void setRectCenterX(Rect rect, int newCenterX) {
        int width = rect.width();
        int currentCenterX = rect.left + width / 2;
        int diff = newCenterX - currentCenterX;
        setRectXPosition(rect, rect.left + diff);
    }

    protected void setRectCenterY(Rect rect, int newCenterY) {
        int height = rect.height();
        int currentCenterY = rect.top + height / 2;
        int diff = newCenterY - currentCenterY;
        setRectYPosition(rect, rect.top + diff);
    }

    protected void onDrawTrackDecoration(Canvas canvas, Paint sharedPaint) {
        sharedPaint.reset();
        sharedPaint.setAntiAlias(true);
        sharedPaint.setColor(trackFillColor);
        RectF fill = new RectF(0, trackBounds.top, getProgress(), trackBounds.bottom);
        canvas.drawRoundRect(fill, trackHeight / 2, trackHeight / 2, sharedPaint);
    }

    void measureText(String text, Rect outRect) {
        valuePaint.getTextBounds(text, 0, text.length(), outRect);
    }

    float calculateValue(int x) {
        float trueValue = x / trackBounds.width() * rangeMaxValue;
        return x / trackBounds.width() * (rangeMaxValue - rangeMinValue) + rangeMinValue;
    }

    void drawValueText(Canvas canvas, String text, Rect bounds) {
        canvas.drawText(text, bounds.left, bounds.bottom, valuePaint);
    }

    private int getTrackLeftOffset() {
        return (getThumbSize() / 2) + minLabelBounds.width() + (labelTextPadding * 2);
    }

    private int getTrackRightOffset() {
        return (getThumbSize() / 2) + maxLabelBounds.width() + (labelTextPadding * 2);
    }

    private int getThumbSize() {
        if (thumbDrawable == null) return 0;
        return Math.max(thumbDrawable.getIntrinsicWidth(), thumbDrawable.getIntrinsicHeight());
    }

    public interface ValueFormatter {
        String formatValue(float value);
        String formatValue(int value);
    }

    public void setRangeValue(float min, float max) {
        rangeMinValue = min;
        rangeMaxValue = max;
    }
}
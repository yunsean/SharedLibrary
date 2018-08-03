package com.dylan.uiparts.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dylan.uiparts.R;

public class RingProgressBar extends View {

    private Paint ringPaint;
    private Paint ringProgressPaint;
    private Paint textPaint;

    private int ringColor;
    private int ringProgressColor;
    private int textColor;
    private float textSize;
    private float ringWidth;
    private int max;
    private int progress;

    private boolean textIsDisplayable;
    private Context mContext;

    private int style;
    public static final int STROKE = 0;
    public static final int FILL = 1;

    public RingProgressBar(Context context) {
        this(context, null);
    }

    public RingProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context;
        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressBar);
        ringColor = mTypedArray.getColor(R.styleable.RingProgressBar_rpb_ringColor, Color.GRAY);
        ringProgressColor = mTypedArray.getColor(R.styleable.RingProgressBar_rpb_ringProgressColor, Color.GREEN);
        textColor = mTypedArray.getColor(R.styleable.RingProgressBar_rpb_textColor, Color.GREEN);
        textSize = mTypedArray.getDimension(R.styleable.RingProgressBar_rpb_textSize, 16);
        ringWidth = mTypedArray.getDimension(R.styleable.RingProgressBar_rpb_ringWidth, 5);
        max = mTypedArray.getInteger(R.styleable.RingProgressBar_rpb_max, 100);
        textIsDisplayable = mTypedArray.getBoolean(R.styleable.RingProgressBar_rpb_textIsDisplayable, true);
        style = mTypedArray.getInt(R.styleable.RingProgressBar_rpb_style, 0);
        mTypedArray.recycle();
    }

    private void initPaint() {
        ringPaint = new Paint();
        ringPaint.setColor(ringColor); //设置圆环的颜色
        ringPaint.setStyle(Paint.Style.STROKE); //设置空心
        ringPaint.setStrokeWidth(ringWidth); //设置圆环的宽度
        ringPaint.setAntiAlias(true);  //消除锯齿

        ringProgressPaint = new Paint();
        ringProgressPaint.setColor(ringProgressColor); //设置圆环的颜色
        ringProgressPaint.setStrokeWidth(ringWidth); //设置圆环的宽度
        ringProgressPaint.setAntiAlias(true);  //消除锯齿
        switch (style) {
            case STROKE:
                ringProgressPaint.setStyle(Paint.Style.STROKE);
                break;
            case FILL:
                ringProgressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                break;
        }

        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD); //设置字体
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mXCenter = getWidth() / 2;
        int mYCenter = getHeight() / 2;
        int radius = (int) (mXCenter - ringWidth / 2); //圆环的半径
        canvas.drawCircle(mXCenter, mYCenter, radius, ringPaint);
        RectF oval = new RectF(mXCenter - radius, mYCenter - radius, mXCenter + radius, mYCenter + radius);  //用于定义的圆弧的形状和大小的界限
        switch (style) {
            case STROKE:
                canvas.drawArc(oval, -90, 360 * progress / max, false, ringProgressPaint);  //根据进度画圆弧
                break;
            case FILL:
                if (progress != 0)
                    canvas.drawArc(oval, -90, 360 * progress / max, true, ringProgressPaint);  //根据进度画圆弧
                break;
        }

        String txt = progress + "%";
        float mTxtWidth = textPaint.measureText(txt, 0, txt.length());
        Log.e("tag", textIsDisplayable + "," + progress + (style == STROKE));
        if (textIsDisplayable && progress != 0 && style == STROKE) {
            canvas.drawText(txt, mXCenter - mTxtWidth / 2, mYCenter + textSize / 2, textPaint);
        }
    }

    public synchronized int getMax() {
        return max;
    }

    public synchronized void setMax(int max) {
        if (max < 0) throw new IllegalArgumentException("max not less than 0");
        this.max = max;
    }

    public synchronized int getProgress() {
        return progress;
    }

    public synchronized void setProgress(int progress) {
        if (progress < 0) throw new IllegalArgumentException("progress not less than 0");
        if (progress > max) progress = max;
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }
    }

    public int getRingColor() {
        return ringColor;
    }

    public void setRingColor(int ringColor) {
        this.ringColor = ringColor;
        if (ringPaint != null) ringPaint.setColor(ringColor);
        postInvalidate();
    }

    public int getRingProgressColor() {
        return ringProgressColor;
    }

    public void setRingProgressColor(int ringProgressColor) {
        this.ringProgressColor = ringProgressColor;
        if (ringProgressPaint != null) ringProgressPaint.setColor(ringProgressColor);
        postInvalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        if (textPaint != null) textPaint.setColor(textColor);
        postInvalidate();
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        if (textPaint != null) textPaint.setTextSize(textSize);
        postInvalidate();
    }

    public float getRingWidth() {
        return ringWidth;
    }
    public void setRingWidth(float roundWidth) {
        this.ringWidth = roundWidth;
        if (ringPaint != null) ringPaint.setStrokeWidth(roundWidth);
        postInvalidate();
    }
}

package com.dylan.uiparts.progressbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.dylan.uiparts.R;

public class ArcProgressBar extends View {

    private Paint mArcPaint;
    private Paint mDottedLinePaint;
    private Paint mRonudRectPaint;
    private Paint mProgressPaint;
    private RectF mRountRect;
    private RectF mArcRect;
    private float mArcWidth = 20.0f;
    private int mArcBgColor = 0xFFFF916D;
    private int mDottedDefaultColor = 0xFF8D99A1;
    private int mDottedRunColor = 0xFFf0724f;
    private int mPdDistance = 50;
    private int mDottedLineCount = 100;
    private int mDottedLineWidth = 20;
    private int mDottedLineHeight = 5;
    private int mLineDistance = 20;
    private int mProgressMax = 100;
    private int mProgressTextSize = 18;
    private int mProgress;
    private float mExternalDottedLineRadius;
    private float mInsideDottedLineRadius;
    private int mArcCenterX;
    private int mArcRadius;
    private double bDistance;
    private double aDistance;
    private boolean isRestart = false;
    private int mRealProgress;
    private boolean mDrawCircle = true;

    public ArcProgressBar(Context context) {
        this(context, null, 0);
    }

    public ArcProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intiAttributes(context, attrs);
        initView();
    }

    private void intiAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcProgressBar);
        mPdDistance = a.getInteger(R.styleable.ArcProgressBar_apb_arcDistance, mPdDistance);
        mArcBgColor = a.getColor(R.styleable.ArcProgressBar_apb_arcBgColor, mArcBgColor);
        mDottedDefaultColor = a.getColor(R.styleable.ArcProgressBar_apb_dottedDefaultColor, mDottedDefaultColor);
        mDottedRunColor = a.getColor(R.styleable.ArcProgressBar_apb_dottedRunColor, mDottedRunColor);
        mDottedLineCount = a.getInteger(R.styleable.ArcProgressBar_apb_dottedLineCount, mDottedLineCount);
        mDottedLineWidth = a.getInteger(R.styleable.ArcProgressBar_apb_dottedLineWidth, mDottedLineWidth);
        mDottedLineHeight = a.getInteger(R.styleable.ArcProgressBar_apb_dottedLineHeight, mDottedLineHeight);
        mLineDistance = a.getInteger(R.styleable.ArcProgressBar_apb_lineDistance, mLineDistance);
        mProgressMax = a.getInteger(R.styleable.ArcProgressBar_apb_progressMax, mProgressMax);
        mProgressTextSize = a.getInteger(R.styleable.ArcProgressBar_apb_progressTextSize, mProgressTextSize);
        mDrawCircle = a.getBoolean(R.styleable.ArcProgressBar_apb_drawCircle, mDrawCircle);
        a.recycle();
    }

    private void initView() {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);
        mArcPaint.setColor(mArcBgColor);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mDottedLinePaint = new Paint();
        mDottedLinePaint.setAntiAlias(true);
        mDottedLinePaint.setStrokeWidth(mDottedLineHeight);
        mDottedLinePaint.setColor(mDottedDefaultColor);

        mRonudRectPaint = new Paint();
        mRonudRectPaint.setAntiAlias(true);
        mRonudRectPaint.setColor(mDottedRunColor);
        mRonudRectPaint.setStyle(Paint.Style.FILL);

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setColor(mDottedRunColor);
        mProgressPaint.setTextSize(dp2px(getResources(), mProgressTextSize));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width > height) width = height;
        else height = width;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mArcCenterX = (int) (w / 2.f);

        mArcRect = new RectF();
        mArcRect.top = 0;
        mArcRect.left = 0;
        mArcRect.right = w;
        mArcRect.bottom = h;

        mArcRect.inset(mArcWidth / 2, mArcWidth / 2);
        mArcRadius = (int) (mArcRect.width() / 2);
        bDistance = Math.cos(Math.PI * 45 / 180) * mArcRadius;
        aDistance = Math.sin(Math.PI * 45 / 180) * mArcRadius;
        mExternalDottedLineRadius = mArcRadius - mArcWidth / 2 - mLineDistance;
        mInsideDottedLineRadius = mExternalDottedLineRadius - mDottedLineWidth;

        mRountRect = new RectF();
        mRountRect.left = (float) (2 * mArcCenterX - 2 * aDistance) / 2 - mArcWidth / 2 + 40;
        mRountRect.top = (float) (mArcCenterX + bDistance) - 20;
        mRountRect.right = (float) (2 * mArcCenterX - (2 * mArcCenterX - 2 * aDistance) / 2) - mArcWidth / 2 - 40;
        mRountRect.bottom = (float) (mArcRadius + mArcRadius) - 20;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawCircle) canvas.drawArc(mArcRect, 135, 270, false, mArcPaint);
        String text = " " + this.mRealProgress + "%";
        canvas.drawText(text, mArcRadius - mProgressPaint.measureText(text) / 2, (float) (mArcRadius + bDistance) - (mProgressPaint.descent() + mProgressPaint.ascent()), mProgressPaint);
        drawDottedLineArc(canvas);
        drawRunDottedLineArc(canvas);
        if (isRestart) drawDottedLineArc(canvas);
    }

    public void restart() {
        isRestart = true;
        this.mRealProgress = 0;
        invalidate();
    }

    public void setMaxProgress(int max) {
        this.mProgressMax = max;
    }
    public void setProgress(int progress) {
        // 进度100% = 控件的75%
        this.mRealProgress = progress;
        isRestart = false;
        this.mProgress = ((mDottedLineCount * 3 / 4) * progress) / mProgressMax;
        postInvalidate();
    }

    private void drawRunDottedLineArc(Canvas canvas) {
        mDottedLinePaint.setColor(mDottedRunColor);
        float evenryDegrees = (float) (2.0f * Math.PI / mDottedLineCount);
        float startDegress = (float) (225 * Math.PI / 180) + evenryDegrees / 2;
        for (int i = 0; i < mProgress; i++) {
            float degrees = i * evenryDegrees + startDegress;
            float startX = mArcCenterX + (float) Math.sin(degrees) * mInsideDottedLineRadius;
            float startY = mArcCenterX - (float) Math.cos(degrees) * mInsideDottedLineRadius;
            float stopX = mArcCenterX + (float) Math.sin(degrees) * mExternalDottedLineRadius;
            float stopY = mArcCenterX - (float) Math.cos(degrees) * mExternalDottedLineRadius;
            canvas.drawLine(startX, startY, stopX, stopY, mDottedLinePaint);
        }
    }

    private void drawDottedLineArc(Canvas canvas) {
        mDottedLinePaint.setColor(mDottedDefaultColor);
        // 360 * Math.PI / 180
        float evenryDegrees = (float) (2.0f * Math.PI / mDottedLineCount);
        float startDegress = (float) (135 * Math.PI / 180);
        float endDegress = (float) (225 * Math.PI / 180);
        for (int i = 0; i < mDottedLineCount; i++) {
            float degrees = i * evenryDegrees;
            // 过滤底部90度的弧长
            if (degrees > startDegress && degrees < endDegress) {
                continue;
            }
            float startX = mArcCenterX + (float) Math.sin(degrees) * mInsideDottedLineRadius;
            float startY = mArcCenterX - (float) Math.cos(degrees) * mInsideDottedLineRadius;
            float stopX = mArcCenterX + (float) Math.sin(degrees) * mExternalDottedLineRadius;
            float stopY = mArcCenterX - (float) Math.cos(degrees) * mExternalDottedLineRadius;
            canvas.drawLine(startX, startY, stopX, stopY, mDottedLinePaint);
        }
    }
    private float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
}

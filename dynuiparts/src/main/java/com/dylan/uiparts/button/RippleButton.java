package com.dylan.uiparts.button;

import com.dylan.uiparts.R;
import com.dylan.uiparts.fontable.FontableButton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class RippleButton extends FontableButton {

    /*起始点*/
    private int mInitX;
    private int mInitY;

    private float mCurrentX;
    private float mCurrentY;

    /*绘制的半径*/
    private float mRadius;
    private float mStepRadius;
    private float mStepOriginX;
    private float mStepOriginY;
    private float mDrawRadius;

    private boolean mDrawFinish;

    private final int DURATION = 150;
    private final int FREQUENCY = 10;
    private float mCycle;
    private final Rect mRect = new Rect();
    private boolean mPressUp = false;
    private Paint mRevealPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private int mRippleColor = 0x25000000;
    private int mFocusColor = 0x15000000;

    public RippleButton(Context context) {
    	this(context, null, 0);
    }
    public RippleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public RippleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs == null)return;
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RippleView, defStyleAttr, 0);
		if (a != null) {
			mRippleColor = a.getColor(R.styleable.RippleView_rv_rippleColor, mRippleColor);
			mFocusColor = a.getColor(R.styleable.RippleView_rv_focusColor, mRippleColor);
			a.recycle();
		}
        initView(context);
    }

    private void initView(Context context) {
        mRevealPaint.setColor(mRippleColor);
        mCycle = DURATION / FREQUENCY;
        final float density = getResources().getDisplayMetrics().density ;
        mCycle = (density*mCycle);
        mDrawFinish = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	if (!isClickable()) {
    		super.onDraw(canvas);
    		return;
    	}
        if (mDrawFinish) {
            super.onDraw(canvas);
            return;
        }
        canvas.drawColor(mFocusColor);
        super.onDraw(canvas);
        if (mStepRadius == 0) {
            return;
        }
        mDrawRadius = mDrawRadius + mStepRadius;
        mCurrentX = mCurrentX + mStepOriginX;
        mCurrentY = mCurrentY + mStepOriginY;
        if (mDrawRadius > mRadius) {
            mDrawRadius = 0;
            canvas.drawCircle(mRect.width() / 2, mRect.height() / 2, mRadius, mRevealPaint);
            mDrawFinish = true;
            if (mPressUp)
                invalidate();
            return;
        }
        canvas.drawCircle(mCurrentX, mCurrentY, mDrawRadius, mRevealPaint);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    private void updateDrawData() {
        mRadius = (float) Math.sqrt(mRect.width() / 2 * mRect.width() / 2 + mRect.height() / 2 * mRect.height() / 2);
        mStepRadius = mRadius / mCycle;
        mStepOriginX = (mRect.width() / 2 - mInitX) / mCycle;
        mStepOriginY = (mRect.height() / 2 - mInitY) / mCycle;
        mCurrentX = mInitX;
        mCurrentY = mInitY;
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (isClickable()) {
            final int action = MotionEventCompat.getActionMasked(event);
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mPressUp = false;
                    mDrawFinish = false;
                    int index = MotionEventCompat.getActionIndex(event);
                    int eventId = MotionEventCompat.getPointerId(event, index);
                    if (eventId != -1) {
                        mInitX = (int) MotionEventCompat.getX(event, index);
                        mInitY = (int) MotionEventCompat.getY(event, index);
                        updateDrawData();
                        invalidate();
                    }
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mStepRadius = (int) (5 * mStepRadius);
                    mStepOriginX = (int) (5 * mStepOriginX);
                    mStepOriginY = (int) (5 * mStepOriginY);
                    mPressUp = true;
                    invalidate();
                    break;
            }
    	}
        return super.onTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean performClick() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                RippleButton.super.performClick();
            }
        }, 150);
        return true;

    }
}

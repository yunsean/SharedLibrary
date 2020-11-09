package com.dylan.uiparts.checkbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.CheckBox;

import com.dylan.common.sketch.Bitmaps;
import com.dylan.uiparts.R;

public class SwitchButton extends CheckBox {
    private Paint mPaint;
    private ViewParent mParent;
    private Bitmap mBottom;
    private Bitmap mCurBtnPic;
    private Bitmap mBtnPressed;
    private Bitmap mBtnNormal;
    private Bitmap mFrame;
    private Bitmap mMask;
    private RectF mSaveLayerRectF;
    private PorterDuffXfermode mXfermode;
    private float mFirstDownY;
    private float mFirstDownX;
    private float mRealPos;
    private float mBtnPos;
    private float mBtnOnPos; 
    private float mBtnOffPos;
    private float mMaskWidth;
    private float mMaskHeight;
    private float mBtnWidth;
    private float mBtnInitPos;
    private int mClickTimeout;
    private int mTouchSlop;
    private final int MAX_ALPHA = 255;
    private int mAlpha = MAX_ALPHA;
    private boolean mChecked = false;
    private boolean mBroadcasting;
    private boolean mTurningOn;
    private PerformClick mPerformClick;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
    private boolean mAnimating;
    private final float VELOCITY = 350;
    private float mVelocity;
    private final float EXTENDED_OFFSET_Y = 0;
    private float mExtendOffsetY;
    private float mAnimationPosition;
    private float mAnimatedVelocity;
    private boolean mFlipped = false;

    public SwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }
    public SwitchButton(Context context) {
        this(context, null);
    }
    public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        Resources resources = context.getResources();
        mClickTimeout = ViewConfiguration.getPressedStateDuration() + ViewConfiguration.getTapTimeout();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mBottom = BitmapFactory.decodeResource(resources, R.drawable.switchbutton_bottom);
        mBtnPressed = BitmapFactory.decodeResource(resources, R.drawable.switchbutton_pressed);
        mBtnNormal = BitmapFactory.decodeResource(resources, R.drawable.switchbutton_unpressed);
        mFrame = BitmapFactory.decodeResource(resources, R.drawable.switchbutton_frame);
        mMask = BitmapFactory.decodeResource(resources, R.drawable.switchbutton_mask);

        mChecked = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "checked", false); 
    	if (attrs != null) {
    		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton, defStyle, 0);
    		if (a != null) {
    			if (a.hasValue(R.styleable.SwitchButton_sb_flip)) {
    				mFlipped = a.getBoolean(R.styleable.SwitchButton_sb_flip, false);
    			}
    			if (a.hasValue(R.styleable.SwitchButton_sb_backgroundColor)) {
    				mPaint.setColor(a.getColor(R.styleable.SwitchButton_sb_backgroundColor, Color.WHITE));
    			}
    			if (a.hasValue(R.styleable.SwitchButton_sb_background)) {
    				mBottom = ((BitmapDrawable)a.getDrawable(R.styleable.SwitchButton_sb_background)).getBitmap();
    			}
    			if (a.hasValue(R.styleable.SwitchButton_sb_thumbNormal)) {
    				mBtnNormal = ((BitmapDrawable)a.getDrawable(R.styleable.SwitchButton_sb_thumbNormal)).getBitmap();
    			}
    			if (a.hasValue(R.styleable.SwitchButton_sb_thumbPressed)) {
    				mBtnPressed = ((BitmapDrawable)a.getDrawable(R.styleable.SwitchButton_sb_thumbPressed)).getBitmap();
    			}
    			if (a.hasValue(R.styleable.SwitchButton_sb_frame)) {
    				mFrame = ((BitmapDrawable)a.getDrawable(R.styleable.SwitchButton_sb_frame)).getBitmap();
    			}
    			if (a.hasValue(R.styleable.SwitchButton_sb_mask)) {
    				mMask = ((BitmapDrawable)a.getDrawable(R.styleable.SwitchButton_sb_mask)).getBitmap();
    			}
    			a.recycle();
    		}
    	}
    	if (mFlipped) {
    		mBottom = Bitmaps.flipHorizontal(mBottom);
    		mFrame = Bitmaps.flipHorizontal(mFrame);
    		mMask = Bitmaps.flipHorizontal(mMask);
    	}
		
        mCurBtnPic = mBtnNormal;
        mBtnWidth = mBtnPressed.getWidth();
        mMaskWidth = mMask.getWidth();
        mMaskHeight = mMask.getHeight();
        mBtnOffPos = mBtnWidth / 2;
        mBtnOnPos = mMaskWidth - mBtnWidth / 2;
        if (mFlipped) {
        	mBtnPos = mChecked ? mBtnOffPos : mBtnOnPos;
        } else {
        	mBtnPos = mChecked ? mBtnOnPos : mBtnOffPos;
        }
        mRealPos = getRealPos(mBtnPos);
        final float density = getResources().getDisplayMetrics().density;
        mVelocity = (int) (VELOCITY * density + 0.5f);
        mExtendOffsetY = (int) (EXTENDED_OFFSET_Y * density + 0.5f);
        mSaveLayerRectF = new RectF(0, mExtendOffsetY, mMask.getWidth(), mMask.getHeight() + mExtendOffsetY);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mAlpha = enabled ? MAX_ALPHA : MAX_ALPHA / 2;
        super.setEnabled(enabled);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    private void setCheckedDelayed(final boolean checked) {
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                superPerformClick();
                setChecked(checked);
            }
        }, 10);
    }
    private void superPerformClick() {
        super.performClick();
    }

    public void setChecked(boolean checked) {

        if (mChecked != checked) {
            mChecked = checked;

            if (mFlipped) {
            	mBtnPos = checked ? mBtnOffPos : mBtnOnPos;
            } else {
            	mBtnPos = checked ? mBtnOnPos : mBtnOffPos;
            }
            mRealPos = getRealPos(mBtnPos);
            invalidate();
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(SwitchButton.this, mChecked);
            }
            if (mOnCheckedChangeWidgetListener != null) {
                mOnCheckedChangeWidgetListener.onCheckedChanged(SwitchButton.this, mChecked);
            }

            mBroadcasting = false;
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    public void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeWidgetListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        float deltaX = Math.abs(x - mFirstDownX);
        float deltaY = Math.abs(y - mFirstDownY);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                attemptClaimDrag();
                mFirstDownX = x;
                mFirstDownY = y;
                mCurBtnPic = mBtnPressed;
                mBtnInitPos = mChecked ? mBtnOnPos : mBtnOffPos;
                break;
            case MotionEvent.ACTION_MOVE:
                float time = event.getEventTime() - event.getDownTime();
                mBtnPos = mBtnInitPos + event.getX() - mFirstDownX;
                if (mBtnPos >= mBtnOffPos) {
                    mBtnPos = mBtnOffPos;
                }
                if (mBtnPos <= mBtnOnPos) {
                    mBtnPos = mBtnOnPos;
                }
                mTurningOn = mBtnPos > (mBtnOffPos - mBtnOnPos) / 2 + mBtnOnPos;

                mRealPos = getRealPos(mBtnPos);
                break;
            case MotionEvent.ACTION_UP:
                mCurBtnPic = mBtnNormal;
                time = event.getEventTime() - event.getDownTime();
                if (deltaY < mTouchSlop && deltaX < mTouchSlop && time < mClickTimeout) {
                    if (mPerformClick == null) {
                        mPerformClick = new PerformClick();
                    }
                    if (!post(mPerformClick)) {
                        performClick();
                    }
                } else {
                    startAnimation(!mTurningOn);
                }
                break;
        }

        invalidate();
        return isEnabled();
    }

    private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean performClick() {
        startAnimation(!mChecked);
        return true;
    }

    private void attemptClaimDrag() {
        mParent = getParent();
        if (mParent != null) {
            mParent.requestDisallowInterceptTouchEvent(true);
        }
    }
    
    private float getRealPos(float btnPos) {
        return btnPos - mBtnWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayerAlpha(mSaveLayerRectF, mAlpha, Canvas.ALL_SAVE_FLAG);
        canvas.drawBitmap(mMask, 0, mExtendOffsetY, mPaint);
        mPaint.setXfermode(mXfermode);
        canvas.drawBitmap(mBottom, mRealPos, mExtendOffsetY, mPaint);
        mPaint.setXfermode(null);
        canvas.drawBitmap(mFrame, 0, mExtendOffsetY, mPaint);
        canvas.drawBitmap(mCurBtnPic, mRealPos, mExtendOffsetY, mPaint);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mMaskWidth, (int) (mMaskHeight + 2 * mExtendOffsetY));
    }

    private void startAnimation(boolean turnOn) {
        mAnimating = true;
        if (mFlipped) {
            mAnimatedVelocity = turnOn ? mVelocity : -mVelocity;
        } else {
            mAnimatedVelocity = turnOn ? -mVelocity : mVelocity;
        }
        mAnimationPosition = mBtnPos;
        new SwitchAnimation().run();
    }

    private void stopAnimation() {
        mAnimating = false;
    }

    private final class SwitchAnimation implements Runnable {
        @Override
        public void run() {
            if (!mAnimating) {
                return;
            }
            doAnimation();
            FrameAnimationController.requestAnimationFrame(this);
        }
    }

    private void doAnimation() {
        mAnimationPosition += mAnimatedVelocity * FrameAnimationController.ANIMATION_FRAME_DURATION / 1000;
        if (mAnimationPosition <= mBtnOnPos) {
            stopAnimation();
            mAnimationPosition = mBtnOnPos;
            setCheckedDelayed(mFlipped ? false : true);
        } else if (mAnimationPosition >= mBtnOffPos) {
            stopAnimation();
            mAnimationPosition = mBtnOffPos;
            setCheckedDelayed(mFlipped ? true : false);
        }
        moveView(mAnimationPosition);
    }

    private void moveView(float position) {
        mBtnPos = position;
        mRealPos = getRealPos(mBtnPos);
        invalidate();
    }
}

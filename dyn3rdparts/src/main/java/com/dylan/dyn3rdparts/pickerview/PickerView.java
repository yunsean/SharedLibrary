package com.dylan.dyn3rdparts.pickerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.dylan.dyn3rdparts.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PickerView extends View {
    public interface LoopListener {
        void onItemSelect(int item);
    }

    private static final String TAG = PickerView.class.getSimpleName();
    public static final int MSG_INVALIDATE = 1000;
    public static final int MSG_SCROLL_LOOP = 2000;
    public static final int MSG_SELECTED_ITEM = 3000;

    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduledFuture;
    private int mTotalScrollY;
    private LoopListener mLoopListener;
    private GestureDetector mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mOnGestureListener;
    private Context mContext;
    private Paint mTopBottomTextPaint;
    private Paint mCenterTextPaint;
    private Paint mCenterLinePaint;
    private List mDataList = new ArrayList();
    private int mTextSize;
    private int mMaxTextWidth;
    private int mMaxTextHeight;
    private int mTopBottomTextColor;
    private int mCenterTextColor;
    private int mCenterLineColor;
    private float lineSpacingMultiplier;
    private boolean mCanLoop;
    private int mTopLineY;
    private int mBottomLineY;
    private int mCurrentIndex;
    private int mInitPosition;
    private int mPaddingLeftRight;
    private int mPaddingTopBottom;
    private float mItemHeight;
    private int mDrawItemsCount;
    private int mCircularDiameter;
    private int mWidgetHeight;
    private int mCircularRadius;
    private int mWidgetWidth;
    private String mPrefix = "";
    private String mPostfix = "";
    private boolean mLeftAlign = false;

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_INVALIDATE)
                invalidate();
            if (msg.what == MSG_SCROLL_LOOP)
                startSmoothScrollTo();
            else if (msg.what == MSG_SELECTED_ITEM)
                itemSelected();
            return false;
        }
    });

    public PickerView(Context context) {
        this(context, null);
    }

    public PickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,attrs);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context,attrs);
    }

    public void setStyle(int topBottomTextColor, int centerTextColor, int centerLineColor, boolean canLoop, int drawItemsCount, int textSize) {
        mTopBottomTextColor = topBottomTextColor;
        mCenterTextColor = centerTextColor;
        mCenterLineColor = centerLineColor;
        mCanLoop = canLoop;
        mDrawItemsCount = drawItemsCount;
        mTextSize = sp2px(getContext(), 16);
        initData();
        invalidate();
    }
    private void initView(Context context,AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PickerView);
        if (array != null) {
            mTopBottomTextColor = array.getColor(R.styleable.PickerView_pv_topBottomTextColor, 0xffafafaf);
            mCenterTextColor = array.getColor(R.styleable.PickerView_pv_centerTextColor, 0xff313131);
            mCenterLineColor = array.getColor(R.styleable.PickerView_pv_lineColor, 0xffc5c5c5);
            mCanLoop = array.getBoolean(R.styleable.PickerView_pv_canLoop, true);
            mInitPosition = array.getInt(R.styleable.PickerView_pv_initPosition, -1);
            mTextSize = array.getDimensionPixelSize(R.styleable.PickerView_pv_textSize, sp2px(context, 16));
            mDrawItemsCount = array.getInt(R.styleable.PickerView_pv_drawItemCount, 7);
            mPrefix = array.getString(R.styleable.PickerView_pv_prefix);
            if (mPrefix == null) mPrefix = "";
            mPostfix = array.getString(R.styleable.PickerView_pv_postfix);
            if (mPostfix == null) mPostfix = "";
            mLeftAlign = array.getBoolean(R.styleable.PickerView_pv_leftAlign, false);
            array.recycle();
        }

        lineSpacingMultiplier = 2.0F;
        this.mContext = context;
        mOnGestureListener = new PickerViewGestureListener();
        mTopBottomTextPaint = new Paint();
        mCenterTextPaint = new Paint();
        mCenterLinePaint = new Paint();
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        mGestureDetector = new GestureDetector(context, mOnGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);
    }

    private void initData() {
        if (mDataList == null) mDataList = new ArrayList();
        mTopBottomTextPaint.setColor(mTopBottomTextColor);
        mTopBottomTextPaint.setAntiAlias(true);
        mTopBottomTextPaint.setTypeface(Typeface.MONOSPACE);
        mTopBottomTextPaint.setTextSize(mTextSize);

        mCenterTextPaint.setColor(mCenterTextColor);
        mCenterTextPaint.setAntiAlias(true);
        mCenterTextPaint.setTextScaleX(1.05F);
        mCenterTextPaint.setTypeface(Typeface.MONOSPACE);
        mCenterTextPaint.setTextSize(mTextSize);

        mCenterLinePaint.setColor(mCenterLineColor);
        mCenterLinePaint.setAntiAlias(true);
        mCenterLinePaint.setTypeface(Typeface.MONOSPACE);
        mCenterLinePaint.setTextSize(mTextSize);

        measureTextWidthHeight();
        int mHalfCircumference = (int) (mMaxTextHeight * lineSpacingMultiplier * (mDrawItemsCount - 1));
        mCircularDiameter = (int) ((mHalfCircumference * 2) / Math.PI);
        mCircularRadius = (int) (mHalfCircumference / Math.PI);
        if (mInitPosition == -1) {
            if (mCanLoop) mInitPosition = (mDataList.size() + 1) / 2;
            else mInitPosition = 0;
        }
        if (mInitPosition >= mDataList.size()) mInitPosition = -1;
        mCurrentIndex = -1;
        setCurrentIndex(mInitPosition);
        mPaddingLeftRight = mLeftAlign ? 10 : (mWidgetWidth - mMaxTextWidth) / 2;
        mPaddingTopBottom = (mWidgetHeight - mCircularDiameter) / 2;
        invalidate();
    }
    private void measureTextWidthHeight() {
        Rect rect = new Rect();
        mMaxTextWidth = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            String s1 = mDataList.get(i).toString();
            mCenterTextPaint.getTextBounds(s1, 0, s1.length(), rect);
            int textWidth = rect.width();
            if (textWidth > mMaxTextWidth) {
                mMaxTextWidth = textWidth;
            }
        }
        mCenterTextPaint.getTextBounds("\u661F\u671F", 0, 2, rect);
        mMaxTextHeight = rect.height();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = mCircularDiameter + mMaxTextHeight;
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }
        setMeasuredDimension(width, height);

        mWidgetWidth = width;
        mWidgetHeight = height;
        mItemHeight = lineSpacingMultiplier * mMaxTextHeight;
        mPaddingLeftRight = mLeftAlign ? 10 : (mWidgetWidth - mMaxTextWidth) / 2;
        mPaddingTopBottom = (mWidgetHeight - mCircularDiameter) / 2;
        mTopLineY = (int) ((mCircularDiameter - mItemHeight) / 2.0F) + mPaddingTopBottom;
        mBottomLineY = (int) ((mCircularDiameter + mItemHeight) / 2.0F) + mPaddingTopBottom;
    }

    public void setLeftAlign(boolean leftAlign) {
        this.mLeftAlign = leftAlign;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDataList == null) {
            super.onDraw(canvas);
            return;
        }
        super.onDraw(canvas);
        int mChangingItem = (int) (mTotalScrollY / (mItemHeight));
        int index = mDataList.size() > 0 ? mInitPosition + mChangingItem % mDataList.size() : 0;
        if (!mCanLoop) {
            if (index < 0) index = 0;
            if (index > mDataList.size() - 1) index = mDataList.size() - 1;
        } else {
            if (index < 0) index = mDataList.size() + mCurrentIndex;
            if (index > mDataList.size() - 1) index = mCurrentIndex - mDataList.size();
        }
        setCurrentIndex(index);

        int count = 0;
        Object itemCount[] = new Object[mDrawItemsCount];
        while (count < mDrawItemsCount) {
            int templateItem = mCurrentIndex - (mDrawItemsCount / 2 - count);
            if (mCanLoop) {
                if (templateItem < 0) {
                    templateItem = templateItem + mDataList.size();
                }
                if (templateItem > mDataList.size() - 1) {
                    templateItem = templateItem - mDataList.size();
                }
            }
            if (templateItem < 0) {
                itemCount[count] = null;
            } else if (templateItem > mDataList.size() - 1) {
                itemCount[count] = null;
            } else {
                itemCount[count] = mDataList.get(templateItem);
            }
            count++;
        }
        canvas.drawLine(0.0F, mTopLineY, mWidgetWidth, mTopLineY, mCenterLinePaint);
        canvas.drawLine(0.0F, mBottomLineY, mWidgetWidth, mBottomLineY, mCenterLinePaint);
        count = 0;
        int changingLeftY = (int) (mTotalScrollY % (mItemHeight));
        while (count < mDrawItemsCount) {
            canvas.save();
            float itemHeight = mMaxTextHeight * lineSpacingMultiplier;
            double radian = (itemHeight * count - changingLeftY) / mCircularRadius;
            float angle = (float) (radian * 180 / Math.PI);
            if (angle >= 180F || angle <= 0F) {
                canvas.restore();
            } else {
                int translateY = (int) (mCircularRadius - Math.cos(radian) * mCircularRadius - (Math.sin(radian) * mMaxTextHeight) / 2) + mPaddingTopBottom;
                canvas.translate(0.0F, translateY);
                canvas.scale(1.0F, (float) Math.sin(radian));
                if (translateY <= mTopLineY) {
                    canvas.save();
                    canvas.clipRect(0, 0, mWidgetWidth, mTopLineY - translateY);
                    canvas.drawText(getDisplay(itemCount[count]), mPaddingLeftRight, mMaxTextHeight, mTopBottomTextPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, mTopLineY - translateY, mWidgetWidth, (int) (itemHeight));
                    canvas.drawText(getDisplay(itemCount[count]), mPaddingLeftRight, mMaxTextHeight, mCenterTextPaint);
                    canvas.restore();
                } else if (mMaxTextHeight + translateY >= mBottomLineY) {
                    canvas.save();
                    canvas.clipRect(0, 0, mWidgetWidth, mBottomLineY - translateY);
                    canvas.drawText(getDisplay(itemCount[count]), mPaddingLeftRight, mMaxTextHeight, mCenterTextPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, mBottomLineY - translateY, mWidgetWidth, (int) (itemHeight));
                    canvas.drawText(getDisplay(itemCount[count]), mPaddingLeftRight, mMaxTextHeight, mTopBottomTextPaint);
                    canvas.restore();
                } else if (translateY >= mTopLineY && mMaxTextHeight + translateY <= mBottomLineY) {
                    canvas.clipRect(0, 0, mWidgetWidth, (int) (itemHeight));
                    canvas.drawText(mPrefix + getDisplay(itemCount[count]) + mPostfix, mPaddingLeftRight, mMaxTextHeight, mCenterTextPaint);
                    setCurrentIndex(mDataList.indexOf(itemCount[count]));
                }
                canvas.restore();
            }
            count++;
        }
    }
    private String getDisplay(Object object) {
        if (onGetDisplayListener != null) return onGetDisplayListener.getDisplay(object);
        else if (object != null) return object.toString();
        else return "";
    }
    private void setCurrentIndex(int index) {
        if (mCurrentIndex == index) return;
        if (onValueChangedListener != null) {
            if (index < 0 || index >= mDataList.size()) onValueChangedListener.onValueChanged(-1, null);
            else onValueChangedListener.onValueChanged(index, mDataList.get(index));
        }
        mCurrentIndex = index;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
        switch (motionevent.getAction()) {
            case MotionEvent.ACTION_UP:
            default:
                if (!mGestureDetector.onTouchEvent(motionevent)) {
                    startSmoothScrollTo();
                }
        }
        return true;
    }
    public final void setCyclic(boolean canLoop) {
        mCanLoop = canLoop;
        invalidate();
    }
    public final void setTextSize(float size) {
        if (size > 0) {
            mTextSize = sp2px(mContext, size);
        }
    }

    public void setPrefix(String prefix) {
        this.mPrefix = prefix == null ? "" : prefix;
    }
    public void setPostfix(String postfix) {
        this.mPostfix = postfix == null ? "" : postfix;
    }

    public void setCurrentItem(int position) {
        if (position < 0 || position >= mDataList.size()) position = 0;
        this.mInitPosition = position;
        mTotalScrollY = 0;
        startSmoothScrollTo();
        invalidate();
    }
    public void setListener(LoopListener LoopListener) {
        mLoopListener = LoopListener;
    }
    public final void setDataList(List list) {
        this.mDataList = list;
        initData();
    }
    public int getCurrentItem() {
        if (mCurrentIndex <= 0) return 0;
        return mCurrentIndex;
    }
    public Object getCurrentObject() {
        if (mCurrentIndex < 0 || mCurrentIndex >= mDataList.size()) return null;
        return mDataList.get(mCurrentIndex);
    }
    private void itemSelected() {
        if (mLoopListener != null) {
            postDelayed(new SelectedRunnable(), 200L);
        }
    }
    private void cancelSchedule() {
        if (mScheduledFuture != null && !mScheduledFuture.isCancelled()) {
            mScheduledFuture.cancel(true);
            mScheduledFuture = null;
        }
    }
    private void startSmoothScrollTo() {
        int offset = (int) (mTotalScrollY % (mItemHeight));
        cancelSchedule();
        mScheduledFuture = mExecutor.scheduleWithFixedDelay(new HalfHeightRunnable(offset), 0, 10, TimeUnit.MILLISECONDS);
    }

    private void startSmoothScrollTo(float velocityY) {
        cancelSchedule();
        int velocityFling = 20;
        mScheduledFuture = mExecutor.scheduleWithFixedDelay(new FlingRunnable(velocityY), 0, velocityFling, TimeUnit.MILLISECONDS);
    }

    class PickerViewGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public final boolean onDown(MotionEvent motionevent) {
            cancelSchedule();
            return true;
        }
        @Override
        public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            startSmoothScrollTo(velocityY);
            return true;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mTotalScrollY = (int) ((float) mTotalScrollY + distanceY);
            if (!mCanLoop) {
                int initPositionCircleLength = (int) (mInitPosition * (mItemHeight));
                int initPositionStartY = -1 * initPositionCircleLength;
                if (mTotalScrollY < initPositionStartY) {
                    mTotalScrollY = initPositionStartY;
                }
                int circleLength = (int) ((float) (mDataList.size() - 1 - mInitPosition) * (mItemHeight));
                if (mTotalScrollY >= circleLength) {
                    mTotalScrollY = circleLength;
                }
            }
            invalidate();
            return true;
        }
    }

    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    class SelectedRunnable implements Runnable {
        @Override
        public final void run() {
            LoopListener listener = PickerView.this.mLoopListener;
            int selectedItem = getCurrentItem();
            mDataList.get(selectedItem);
            listener.onItemSelect(selectedItem);
        }
    }

    class HalfHeightRunnable implements Runnable {
        int realTotalOffset;
        int realOffset;
        int offset;
        public HalfHeightRunnable(int offset) {
            this.offset = offset;
            realTotalOffset = Integer.MAX_VALUE;
            realOffset = 0;
        }
        @Override
        public void run() {
            if (realTotalOffset == Integer.MAX_VALUE) {
                if ((float) offset > mItemHeight / 2.0F) {
                    realTotalOffset = (int) (mItemHeight - (float) offset);
                } else {
                    realTotalOffset = -offset;
                }
            }
            realOffset = (int) ((float) realTotalOffset * 0.1F);
            if (realOffset == 0) {
                if (realTotalOffset < 0) {
                    realOffset = -1;
                } else {
                    realOffset = 1;
                }
            }
            if (Math.abs(realTotalOffset) <= 0) {
                cancelSchedule();
                mHandler.sendEmptyMessage(MSG_SELECTED_ITEM);
                return;
            } else {
                mTotalScrollY = mTotalScrollY + realOffset;
                mHandler.sendEmptyMessage(MSG_INVALIDATE);
                realTotalOffset = realTotalOffset - realOffset;
                return;
            }
        }
    }

    class FlingRunnable implements Runnable {
        float velocity;
        final float velocityY;
        FlingRunnable(float velocityY) {
            this.velocityY = velocityY;
            velocity = Integer.MAX_VALUE;
        }
        @Override
        public void run() {
            if (velocity == Integer.MAX_VALUE) {
                if (Math.abs(velocityY) > 2000F) {
                    if (velocityY > 0.0F) {
                        velocity = 2000F;
                    } else {
                        velocity = -2000F;
                    }
                } else {
                    velocity = velocityY;
                }
            }
            if (Math.abs(velocity) >= 0.0F && Math.abs(velocity) <= 20F) {
                cancelSchedule();
                mHandler.sendEmptyMessage(MSG_SCROLL_LOOP);
                return;
            }
            int i = (int) ((velocity * 10F) / 1000F);
            mTotalScrollY = mTotalScrollY - i;
            if (!mCanLoop) {
                float itemHeight = lineSpacingMultiplier * mMaxTextHeight;
                if (mTotalScrollY <= (int) ((float) (-mInitPosition) * itemHeight)) {
                    velocity = 40F;
                    mTotalScrollY = (int) ((float) (-mInitPosition) * itemHeight);
                } else if (mTotalScrollY >= (int) ((float) (mDataList.size() - 1 - mInitPosition) * itemHeight)) {
                    mTotalScrollY = (int) ((float) (mDataList.size() - 1 - mInitPosition) * itemHeight);
                    velocity = -40F;
                }
            }
            if (velocity < 0.0F) {
                velocity = velocity + 20F;
            } else {
                velocity = velocity - 20F;
            }
            mHandler.sendEmptyMessage(MSG_INVALIDATE);
        }
    }

    public interface OnValueChangedListener {
        void onValueChanged(int index, Object picked);
    }
    public interface OnGetDisplayListener {
        String getDisplay(Object item);
    }
    private OnValueChangedListener onValueChangedListener = null;
    private OnGetDisplayListener onGetDisplayListener = null;
    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }
    public void setOnGetDisplayListener(OnGetDisplayListener onGetDisplayListener) {
        this.onGetDisplayListener = onGetDisplayListener;
    }
}

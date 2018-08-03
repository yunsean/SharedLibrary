package com.dylan.uiparts.layout;

import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;


public class ScrollViewContainer extends RelativeLayout {

    public static final int AUTO_UP = 0;
    public static final int AUTO_DOWN = 1;
    public static final int DONE = 2;
    public static final float SPEED = 10.0f;
    private boolean isMeasured = false;
    private VelocityTracker vt;

    private int mViewHeight;
    private int mViewWidth;

    private View topView;
    private View bottomView;

    private boolean canPullDown = true;
    private boolean canPullUp;
    private int state = DONE;
    private int mCurrentViewIndex = 0;
    private float mMoveLen;
    private MyTimer mTimer;
    private float mLastY;
    private int mEvents;

    @SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
        @SuppressWarnings("static-access")
		@Override
        public void handleMessage(Message msg) {
            if (mMoveLen != 0) {
                if (state == AUTO_UP) {
                    mMoveLen -= SPEED;
                    if (mMoveLen <= -mViewHeight) {
                        mMoveLen = -mViewHeight;
                        state = DONE;
                        mCurrentViewIndex = 1;
                        if (l != null) {
                            l.onSecondViewShow();
                        }
                    }
                } else if (state == AUTO_DOWN) {
                    mMoveLen += SPEED;
                    if (mMoveLen >= 0) {
                        mMoveLen = 0;
                        state = DONE;
                        mCurrentViewIndex = 0;
                        if (upl != null) {
                            upl.onUpPullHintListener(upl.STATE_DWON);
                        }
                    }
                } else {
                    mTimer.cancel();
                }
            }
            requestLayout();
        }

    };

    public ScrollViewContainer(Context context) {
        super(context);
        init();
    }
    public ScrollViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ScrollViewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init() {
        mTimer = new MyTimer(handler);
    }    
    
    public boolean isShowBottomView() {
    	return mCurrentViewIndex == 1;
    }
    public void showTopView() {
    	mMoveLen = 0;
    	mCurrentViewIndex = 0;
    	requestLayout();
    }
    
    @SuppressLint("Recycle")
	@SuppressWarnings("static-access")
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (vt == null)
                    vt = VelocityTracker.obtain();
                else
                    vt.clear();
                mLastY = ev.getY();
                vt.addMovement(ev);
                mEvents = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                mEvents = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                vt.addMovement(ev);
                if (canPullUp && mCurrentViewIndex == 0 && mEvents > 0) {
                    mMoveLen += (ev.getY() - mLastY);
                    // ��ֹ����Խ��
                    if (mMoveLen > 0) {
                        mMoveLen = 0;
                        mCurrentViewIndex = 0;
                    } else if (mMoveLen < -mViewHeight / 5) {
                        mMoveLen = -mViewHeight / 5;
                        mCurrentViewIndex = 1;
                        if (upl != null) {
                            upl.onUpPullHintListener(upl.STATE_UP);
                        }
                    }
                    if (mMoveLen < -8) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                } else if (canPullDown && mCurrentViewIndex == 1 && mEvents == 0) {
                    mMoveLen += (ev.getY() - mLastY);
                    if (mMoveLen < -mViewHeight) {
                        mMoveLen = -mViewHeight;
                        mCurrentViewIndex = 1;
                    } else if (mMoveLen > 0) {
                        mMoveLen = 0;
                        mCurrentViewIndex = 0;
                    }
                    if (mMoveLen > 8 - mViewHeight) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                } else
                    mEvents++;
                mLastY = ev.getY();
                requestLayout();
                break;
            case MotionEvent.ACTION_UP:
                mLastY = ev.getY();
                vt.addMovement(ev);
                vt.computeCurrentVelocity(700);
                float mYV = vt.getYVelocity();
                if (mMoveLen == 0 || mMoveLen == -mViewHeight)
                    break;
                if (Math.abs(mYV) < 500) {
                    if (mMoveLen <= -mViewHeight / 2) {
                        state = AUTO_UP;
                    } else if (mMoveLen > -mViewHeight / 2) {
                        state = AUTO_DOWN;
                    }
                } else {
                    if (mYV < 0)
                        state = AUTO_UP;
                    else
                        state = AUTO_DOWN;
                }
                mTimer.schedule(2);
                break;
        }
        try {
            super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        topView.layout(0, (int) mMoveLen, mViewWidth, topView.getMeasuredHeight() + (int) mMoveLen);
        bottomView.layout(0, topView.getMeasuredHeight() + (int) mMoveLen, mViewWidth, topView.getMeasuredHeight() + (int) mMoveLen + bottomView.getMeasuredHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isMeasured) {
            isMeasured = true;
            mViewHeight = getMeasuredHeight();
            mViewWidth = getMeasuredWidth();
            topView = getChildAt(0);
            bottomView = getChildAt(1);
            bottomView.setOnTouchListener(bottomViewTouchListener);
            topView.setOnTouchListener(topViewTouchListener);
        }
    }

    private OnTouchListener topViewTouchListener = new OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
		@Override
        public boolean onTouch(View v, MotionEvent event) {
            ScrollView sv = (ScrollView) v;
            if (sv.getScrollY() == (sv.getChildAt(0).getMeasuredHeight() - sv.getMeasuredHeight()) && mCurrentViewIndex == 0)
                canPullUp = true;
            else
                canPullUp = false;
            return false;
        }
    };
    @SuppressLint("ClickableViewAccessibility")
	private OnTouchListener bottomViewTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ScrollView sv = (ScrollView) v;
            if (sv.getScrollY() == 0 && mCurrentViewIndex == 1) {
                canPullDown = true;
            } else {
                canPullDown = false;
            }
            return false;
        }
    };

    class MyTimer {
        private Handler handler;
        private Timer timer;
        private MyTask mTask;

        public MyTimer(Handler handler) {
            this.handler = handler;
            timer = new Timer();
        }

        @SuppressLint("ClickableViewAccessibility")
		public void schedule(long period) {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
            mTask = new MyTask(handler);
            timer.schedule(mTask, 0, period);
        }

        public void cancel() {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
        }

        class MyTask extends TimerTask {
            private Handler handler;
            public MyTask(Handler handler) {
                this.handler = handler;
            }
            @Override
            public void run() {
                handler.obtainMessage().sendToTarget();
            }
        }
    }

    private OnUpPullHintListener upl;
    private OnSecondViewShowListener l;
    public void setOnSecondViewShowListener(OnSecondViewShowListener l) {
        this.l = l;
    }
    public void setOnUpPullHintListener(OnUpPullHintListener upl) {
        this.upl = upl;
    }
    
    public interface OnUpPullHintListener {
        public static final int STATE_UP = 1; 
        public static final int STATE_DWON = 2; 
        public void onUpPullHintListener(int state);
    }
    public interface OnSecondViewShowListener {
        public void onSecondViewShow();
    }
}

package com.dylan.uiparts.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class FlipGesturer{

	private int FLING_MIN_DISTANCEX = 50;     
	private int FLING_MIN_DISTANCEY = 50;   
    private int FLING_MIN_VELOCITYY = 100;   
    private int FLING_MIN_VELOCITYX = 100;
    private boolean FLIING_DISABLEX = false;
    private boolean FLIING_ENABLEY = false;
    private GestureDetector mGesturer = null;
	public FlipGesturer(Activity context, OnFilpListener listener) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		FLING_MIN_DISTANCEX = width / 4;
		FLING_MIN_DISTANCEY = height / 4;
		onFilpListener = listener;
		init(context);
	}
	public FlipGesturer(final View view, OnFilpListener listener) {
		ViewTreeObserver vto = view.getViewTreeObserver();  
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){ 
			@SuppressWarnings("deprecation")
			@Override
		    public void onGlobalLayout() {
				if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
					removeOnGlobalLayoutListener();
				} else {
					view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
				int height =view.getMeasuredHeight();
				int width =view.getMeasuredWidth();
				FLING_MIN_DISTANCEX = width / 4;
				FLING_MIN_DISTANCEY = height / 4;
			}
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			private void removeOnGlobalLayoutListener() {
				view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
		onFilpListener = listener;
		init(view.getContext());
	}

	public void setOnFilpListener(OnFilpListener onFilpListener) {
		this.onFilpListener = onFilpListener;
	}

	private void init(Context context) {
		mGesturer = new GestureDetector(context, new OnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}
			@Override
			public void onShowPress(MotionEvent e) {
			}
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				return false;
			}
			@Override
			public void onLongPress(MotionEvent e) {
			}
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (e1 == null || e2 == null)return false;
				boolean enableX = false;
				boolean enableY = false;
				if (FLIING_ENABLEY && FLIING_DISABLEX) {
					enableY = true;
				} else if (!FLIING_ENABLEY && !FLIING_DISABLEX) {
					enableX = true;
				} else if (Math.abs(e1.getRawY() - e2.getRawY()) > Math.abs(e1.getRawX() - e2.getRawX())){
					enableY = true;
				} else {
					enableX = true;
				}
				if (enableY) {
					if (e1.getRawY()-e2.getRawY() > FLING_MIN_DISTANCEY && Math.abs(velocityY) > FLING_MIN_VELOCITYY) {
						if (onFilpListener != null) {
							onFilpListener.onFlipUp();
							e2.setAction(MotionEvent.ACTION_CANCEL);
						}
					} else if (e2.getRawY()-e1.getRawY() > FLING_MIN_DISTANCEY && Math.abs(velocityY) > FLING_MIN_VELOCITYY) {
						if (onFilpListener != null) {
							onFilpListener.onFlipDown();
							e2.setAction(MotionEvent.ACTION_CANCEL);
						}
				    }
				}
				if (enableX) {
					if (e1.getRawX()-e2.getRawX() > FLING_MIN_DISTANCEX && Math.abs(velocityX) > FLING_MIN_VELOCITYX) {
						if (onFilpListener != null) {
							onFilpListener.onFlipRight();
							e2.setAction(MotionEvent.ACTION_CANCEL);
						}
					} else if (e2.getRawX()-e1.getRawX() > FLING_MIN_DISTANCEX && Math.abs(velocityX) > FLING_MIN_VELOCITYX) {
						if (onFilpListener != null) {
							onFilpListener.onFlipLeft();
							e2.setAction(MotionEvent.ACTION_CANCEL);
						}
				    } 
				}
		        return false;
			}
			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}
		}); 
	}
	public void setSensitivity(View view) {
		if (view == null)return;
		int width = view.getLayoutParams().width;
		int height = view.getLayoutParams().height;
		if (width > 0)FLING_MIN_DISTANCEX = width / 4;
		if (height > 0)FLING_MIN_DISTANCEY = height / 4;
	}
	public void setSensitivity(int distanceX, int distanceY, int velocity) {
		if (distanceX > 0)FLING_MIN_DISTANCEX = distanceX;
		if (distanceY > 0)FLING_MIN_DISTANCEY = distanceY;
		if (velocity > 0)FLING_MIN_VELOCITYY = velocity;
		if (velocity > 0)FLING_MIN_VELOCITYX = velocity;
	}
	public void setSensitivity(int distanceX, int distanceY, int velocityX, int velocityY) {
		if (distanceX > 0)FLING_MIN_DISTANCEX = distanceX;
		if (distanceY > 0)FLING_MIN_DISTANCEY = distanceY;
		if (velocityX > 0)FLING_MIN_VELOCITYX = velocityX;
		if (velocityY > 0)FLING_MIN_VELOCITYY = velocityY;
	}
	public void setDisableX(boolean disable) {
		FLIING_DISABLEX = disable;
	}
	public void setEnableY(boolean enable) {
		FLIING_ENABLEY = enable;
	}
	public boolean onTouchEvent(MotionEvent event) {
		if (mGesturer != null) {
			return mGesturer.onTouchEvent(event);
		} else {
			return false;
		}
	}
	
	private OnFilpListener onFilpListener;
	public interface OnFilpListener {
		void onFlipLeft();
		void onFlipRight();
		void onFlipUp();
		void onFlipDown();
	}
}

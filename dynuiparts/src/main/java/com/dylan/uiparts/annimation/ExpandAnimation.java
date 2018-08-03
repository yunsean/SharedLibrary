package com.dylan.uiparts.annimation;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ExpandAnimation extends Animation {
	private View mAnimatedView;
	private ViewGroup.MarginLayoutParams mViewLayoutParams;
	private int mMarginStart, mMarginEnd;
	private boolean mIsVisibleAfter = false;
	private boolean mWasEndedAlready = false;

	public ExpandAnimation(View view) {
		mAnimatedView = view;
		mViewLayoutParams = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		mIsVisibleAfter = view.getVisibility() != View.VISIBLE;
		if (mIsVisibleAfter) {
			mMarginStart = -view.getHeight();
			mMarginEnd = 0;			
		} else {
			mMarginStart = 0;
			mMarginEnd = -view.getHeight();
		}
		mViewLayoutParams.bottomMargin = mMarginStart;
		mAnimatedView.requestLayout();
		view.setVisibility(View.VISIBLE);
	}
	public ExpandAnimation(View view, int showHeight) {
		mAnimatedView = view;
		mViewLayoutParams = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		mIsVisibleAfter = view.getVisibility() != View.VISIBLE;
		if (mIsVisibleAfter) {
			mMarginStart = -showHeight;
			mMarginEnd = 0;			
		} else {
			mMarginStart = 0;
			mMarginEnd = -showHeight;
		}
		mViewLayoutParams.bottomMargin = mMarginStart;
		mAnimatedView.requestLayout();
		view.setVisibility(View.VISIBLE);
	}
	public boolean isVisibleAfter() {
		return mIsVisibleAfter;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		if (interpolatedTime < 1.0f) {
			mViewLayoutParams.bottomMargin = mMarginStart + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);
			mAnimatedView.requestLayout();
		} else if (!mWasEndedAlready) {
			mViewLayoutParams.bottomMargin = mMarginEnd;
			mAnimatedView.requestLayout();
			if (!mIsVisibleAfter) {
				mAnimatedView.setVisibility(View.GONE);
			}
			mWasEndedAlready = true;
		}
	}
}

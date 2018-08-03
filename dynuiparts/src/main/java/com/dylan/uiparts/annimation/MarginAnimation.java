package com.dylan.uiparts.annimation;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class MarginAnimation extends Animation {
	private View mAnimatedView;
	private ViewGroup.MarginLayoutParams mViewLayoutParams;
	private int mMarginStart, mMarginEnd;
	private boolean mWasEndedAlready = false;
	private Margin mMargin = Margin.Bottom;

	public enum Margin{Top, Bottom, Left, Right};
	public MarginAnimation(View view, int endMargin) {
		mAnimatedView = view;
		mViewLayoutParams = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		mMarginStart = ((MarginLayoutParams)view.getLayoutParams()).bottomMargin;
		mMarginEnd = endMargin;
		mViewLayoutParams.bottomMargin = mMarginStart;
		mAnimatedView.requestLayout();
	}
	public MarginAnimation(View view, Margin margin, int endMargin) {
		mAnimatedView = view;
		mViewLayoutParams = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		mMarginEnd = endMargin;
		mMargin = margin;
		switch (mMargin) {
		case Top:
			mMarginStart = view.getTop();
			break;
		case Bottom:
			mMarginStart = view.getBottom();
			break;
		case Left:
			mMarginStart = view.getLeft();
			break;
		case Right:
			mMarginStart = view.getRight();
			break;
		}
		mAnimatedView.requestLayout();
	}
	public MarginAnimation(View view, Margin margin, int beginMargin, int endMargin) {
		mAnimatedView = view;
		mViewLayoutParams = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		mMarginStart = beginMargin;
		mMarginEnd = endMargin;
		mMargin = margin;
		switch (mMargin) {
		case Top:
			mViewLayoutParams.topMargin = mMarginStart;			
			break;
		case Bottom:
			mViewLayoutParams.bottomMargin = mMarginStart;
			break;
		case Left:
			mViewLayoutParams.leftMargin = mMarginStart;
			break;
		case Right:
			mViewLayoutParams.rightMargin = mMarginStart;
			break;
		}
		mAnimatedView.requestLayout();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		if (interpolatedTime < 1.0f) {
			switch (mMargin) {
			case Top:
				mViewLayoutParams.topMargin = mMarginStart + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);;			
				break;
			case Bottom:
				mViewLayoutParams.bottomMargin = mMarginStart + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);;
				break;
			case Left:
				mViewLayoutParams.leftMargin = mMarginStart + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);;
				break;
			case Right:
				mViewLayoutParams.rightMargin = mMarginStart + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);;
				break;
			}
			mAnimatedView.requestLayout();
		} else if (!mWasEndedAlready) {
			switch (mMargin) {
			case Top:
				mViewLayoutParams.topMargin = mMarginEnd;			
				break;
			case Bottom:
				mViewLayoutParams.bottomMargin = mMarginEnd;
				break;
			case Left:
				mViewLayoutParams.leftMargin = mMarginEnd;
				break;
			case Right:
				mViewLayoutParams.rightMargin = mMarginEnd;
				break;
			}
			mAnimatedView.requestLayout();
			mWasEndedAlready = true;
		}
	}
}

package com.dylan.uiparts.annimation;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class SizeAnimation extends Animation {
	public static class Size {
		public int width;
		public int height;
		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}
	public static enum Mode{Width, Height};

	private View mAnimatedView;
	private ViewGroup.LayoutParams mViewLayoutParams;
	private Size mSizeStart;
	private Size mSizeEnd;
	private boolean mWasEndedAlready = false;

	public SizeAnimation(View view, Mode mode, int endSize) {
		mViewLayoutParams = view.getLayoutParams();
		int other = mode == Mode.Width ? mViewLayoutParams.height : mViewLayoutParams.width;
		if (mode == Mode.Width) {
			set(view, new Size(mViewLayoutParams.width, other), new Size(endSize, other));
		} else {
			set(view, new Size(other, mViewLayoutParams.height), new Size(other, endSize));
		}
	}
	public SizeAnimation(View view, Mode mode, int beginSize, int endSize) {
		mViewLayoutParams = view.getLayoutParams();
		int other = mode == Mode.Width ? mViewLayoutParams.height : mViewLayoutParams.width;
		if (mode == Mode.Width) {
			set(view, new Size(beginSize, other), new Size(endSize, other));
		} else {
			set(view, new Size(beginSize, other), new Size(endSize, other));
		}
	}
	public SizeAnimation(View view, Size endSize) {
		mViewLayoutParams = view.getLayoutParams();
		Size beginSize = new Size(mViewLayoutParams.width, mViewLayoutParams.height);
		set(view, beginSize, endSize);
	}
	public SizeAnimation(View view, Size beginSize, Size endSize) {
		mViewLayoutParams = view.getLayoutParams();
		set(view, beginSize, endSize);
	}
	private void set(View view, Size beginSize, Size endSize) {
		mAnimatedView = view;
		mViewLayoutParams = view.getLayoutParams();
		mSizeStart = beginSize;
		mSizeEnd = endSize;
		mViewLayoutParams.width = beginSize.width;
		mViewLayoutParams.height = beginSize.height;
		mAnimatedView.requestLayout();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		if (interpolatedTime < 1.0f) {
			mViewLayoutParams.width = mSizeStart.width + (int) ((mSizeEnd.width - mSizeStart.width) * interpolatedTime);
			mViewLayoutParams.height = mSizeStart.height + (int) ((mSizeEnd.height - mSizeStart.height) * interpolatedTime);
			mAnimatedView.requestLayout();
		} else if (!mWasEndedAlready) {
			mViewLayoutParams.width = mSizeEnd.width;
			mViewLayoutParams.height = mSizeEnd.height;
			mAnimatedView.requestLayout();
			mWasEndedAlready = true;
		}
	}
}

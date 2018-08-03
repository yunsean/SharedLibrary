package com.dylan.uiparts.imageview;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AnimateImageView extends ImageView {

	public AnimateImageView(Context context) {
		super(context);
	}

	public AnimateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public AnimateImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr) {
		mAnimateHandler = new AnimateHandler(this);
	}

	private int[] mAnimateImageResId = null;
	private int mCurrentImageResId = 0;
	private int mSwitchImageDelay = 300;

	public void setAnimateResId(int[] resIds) {
		mAnimateImageResId = resIds;
	}

	public void startAnimate() {
		if (mAnimateImageResId.length < 1)
			return;
		setImageResource(mAnimateImageResId[0]);
		mCurrentImageResId = 1;
		if (mSwitchImageDelay < 100)
			mSwitchImageDelay = 100;
		mAnimateHandler.postDelayed(mAnimateRunable, mSwitchImageDelay);
	}

	public void stopAnimate() {
		mAnimateHandler.removeCallbacks(mAnimateRunable);
	}

	Handler mAnimateHandler = null;

	private static class AnimateHandler extends Handler {
		private WeakReference<AnimateImageView> mView;

		public AnimateHandler(AnimateImageView view) {
			mView = new WeakReference<AnimateImageView>(view);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mView.get() != null) {
				mView.get().animateHandle();
			}
		}
	}

	private void animateHandle() {
		if (mAnimateImageResId.length < 1) {
			return;
		}
		mCurrentImageResId++;
		if (mCurrentImageResId >= mAnimateImageResId.length) {
			mCurrentImageResId = 0;
		}

		setImageResource(mAnimateImageResId[mCurrentImageResId]);
		mAnimateHandler.postDelayed(mAnimateRunable, mSwitchImageDelay);
	}

	Runnable mAnimateRunable = new Runnable() {
		@Override
		public void run() {
			mAnimateHandler.sendEmptyMessage(0);
		}
	};
}

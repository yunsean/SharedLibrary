package com.dylan.uiparts.listview;

import com.dylan.uiparts.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

class DragListViewHeader extends LinearLayout {
	private View mContentView;
	private LinearLayout mContainer;
	private ImageView mArrowImageView;
	private ProgressBar mProgressBar;
	private TextView mHintTextView;
	private int mState = STATE_NORMAL;

	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;
	
	private final int ROTATE_ANIM_DURATION = 180;
	
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_REFRESHING = 2;

	public DragListViewHeader(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public DragListViewHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	@SuppressWarnings("deprecation")
	public void setBackgroundDrawable(Drawable bg, Drawable arrow) {
		if (mContentView != null && bg != null)mContentView.setBackgroundDrawable(bg);
		if (mArrowImageView != null && arrow != null)mArrowImageView.setImageDrawable(arrow);
	}
	public void setBackgroundResource(int bgid, int arrowid) {
		if (mContentView != null && bgid != 0)mContentView.setBackgroundResource(bgid);
		if (mArrowImageView != null && arrowid != 0)mArrowImageView.setImageResource(arrowid);
	}
	public void setBackgroundColor(int color) {
		if (mContentView != null)mContentView.setBackgroundColor(color);
	}

	@SuppressLint("InflateParams")
	private void initView(Context context) {
		// 初始情况，设置下拉刷新view高度
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
		mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.darglistview_header, null);
		addView(mContainer, lp);
		setGravity(Gravity.BOTTOM);

		mContentView = (View)findViewById(R.id.draglistview_header_content);
		mArrowImageView = (ImageView)findViewById(R.id.draglistview_header_arrow);
		mHintTextView = (TextView)findViewById(R.id.draglistview_header_hint_textview);
		mProgressBar = (ProgressBar)findViewById(R.id.draglistview_header_progressbar);
		
		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);
	}

	public void setState(int state) {
		if (state == mState) return ;
		
		if (state == STATE_REFRESHING) {	// 显示进度
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
		} else {	// 显示箭头图片
			mArrowImageView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
		}
		
		switch(state){
		case STATE_NORMAL:
			if (mState == STATE_READY) {
				mArrowImageView.startAnimation(mRotateDownAnim);
			}
			if (mState == STATE_REFRESHING) {
				mArrowImageView.clearAnimation();
			}
			mHintTextView.setText(R.string.draglistview_header_hint_normal);
			break;
		case STATE_READY:
			if (mState != STATE_READY) {
				mArrowImageView.clearAnimation();
				mArrowImageView.startAnimation(mRotateUpAnim);
				mHintTextView.setText(R.string.draglistview_header_hint_ready);
			}
			break;
		case STATE_REFRESHING:
			mHintTextView.setText(R.string.draglistview_header_hint_loading);
			break;
			default:
		}
		
		mState = state;
	}
	
	public void setVisiableHeight(int height) {
		if (height < 0)
			height = 0;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer
				.getLayoutParams();
		lp.height = height;
		mContainer.setLayoutParams(lp);
	}

	public int getVisiableHeight() {
		return mContainer.getHeight();
	}

}

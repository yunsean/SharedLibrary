package com.dylan.uiparts.listview;

import com.dylan.uiparts.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

class DragListViewFooter extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_LOADING = 2;

	private Context mContext;

	private View mContentView;
	private View mProgressBar;
	private TextView mHintView;
	
	public DragListViewFooter(Context context) {
		super(context);
		initView(context);
	}
	
	public DragListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	@SuppressWarnings("deprecation")
	public void setBackgroundDrawable(Drawable bg) {
		if (mContentView != null)mContentView.setBackgroundDrawable(bg);
	}
	public void setBackgroundResource(int resid) {
		if (mContentView != null)mContentView.setBackgroundResource(resid);
	}
	public void setBackgroundColor(int color) {
		if (mContentView != null)mContentView.setBackgroundColor(color);
	}
	
	public void setState(int state) {
		mHintView.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		mHintView.setVisibility(View.INVISIBLE);
		if (state == STATE_READY) {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText(R.string.draglistview_footer_hint_ready);
		} else if (state == STATE_LOADING) {
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText(R.string.draglistview_footer_hint_normal);
		}
	}
	
	public void setBottomMargin(int height) {
		if (height < 0) return ;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}
	
	public int getBottomMargin() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		return lp.bottomMargin;
	}
	
	/**
	 * normal status
	 */
	public void normal() {
		mHintView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}
	
	
	/**
	 * loading status 
	 */
	public void loading() {
		mHintView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}
	
	/**
	 * show footer
	 */
	public void show() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
	}
	
	@SuppressLint("InflateParams")
	private void initView(Context context) {
		mContext = context;
		LinearLayout moreView = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.draglistview_footer, null);
		addView(moreView);
		moreView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		mContentView = moreView.findViewById(R.id.draglistview_footer_content);
		mProgressBar = moreView.findViewById(R.id.draglistview_footer_progressbar);
		mHintView = (TextView)moreView.findViewById(R.id.draglistview_footer_hint_textview);
	}
	
	
}

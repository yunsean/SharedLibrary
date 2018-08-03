package com.dylan.uiparts.textview;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.dylan.uiparts.annimation.ExpandAnimation;
import com.dylan.uiparts.R;

public class ExpandTextView extends TextView implements OnClickListener {

	public enum IconPostion{Left, Right, Top, Bottom};
	
	public ExpandTextView(Context context) {
		this(context, null, 0);
	}
	public ExpandTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public ExpandTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mExpand = getContext().getResources().getDrawable(R.drawable.expandbutton_expand);
		mShrink = getContext().getResources().getDrawable(R.drawable.expandbutton_shrink);
		mExpand.setBounds(0, 0, mExpand.getMinimumWidth(), mExpand.getMinimumHeight());
		mShrink.setBounds(0, 0, mShrink.getMinimumWidth(), mShrink.getMinimumHeight());
		updateIcon(mExpand);
		setOnClickListener(this);
	}
	
	public void setIconPostion(IconPostion pos) {
		mPostion = pos;
		updateIcon(mExpand);
	}
	public static void bindView(View parent, int button, int view) {
		ExpandTextView eb = (ExpandTextView)parent.findViewById(button);
		eb.setExpandView(parent.findViewById(view));
	}
	public static void bindView(Activity parent, int button, int view) {
		ExpandTextView eb = (ExpandTextView)parent.findViewById(button);
		eb.setExpandView(parent.findViewById(view));
	}
	
	public void setExpandView(View view) {
		mExpandView = view;
	}
	public void setExpandView(View view, boolean hide) {
		mExpandView = view;
		if (hide)mExpandView.setVisibility(View.GONE);
		else mExpandView.setVisibility(View.VISIBLE);
	} 
	public void setExpandResId(int resId) {
		mExpand = getContext().getResources().getDrawable(resId);
		mExpand.setBounds(0, 0, mExpand.getMinimumWidth(), mExpand.getMinimumHeight());
		updateIcon();
	}
	public void setShrinkResId(int resId) {
		mShrink = getContext().getResources().getDrawable(resId);
		mShrink.setBounds(0, 0, mShrink.getMinimumWidth(), mShrink.getMinimumHeight());
		updateIcon();
	}
	public void setResId(int expand, int shrink) {
		setExpandResId(expand);
		setShrinkResId(shrink);
	}
	public void setExpandDrawable(Drawable drawable) {
		mExpand = drawable;
		mExpand.setBounds(0, 0, mExpand.getMinimumWidth(), mExpand.getMinimumHeight());
		updateIcon();
	}
	public void setShrinkDrawable(Drawable drawable) {
		mShrink = drawable;
		mShrink.setBounds(0, 0, mShrink.getMinimumWidth(), mShrink.getMinimumHeight());
		updateIcon();
	}
	public void setDrawable(Drawable expand, Drawable shrink) {
		setExpandDrawable(expand);
		setShrinkDrawable(shrink);
	}
	public void expand() {
		if (mExpandView == null)return;
		LayoutParams params = (LayoutParams)mExpandView.getLayoutParams();
		if (params.bottomMargin != 0) {
			onClick(null);
		}
	}
	public void shrink() {
		if (mExpandView == null)return;
		LayoutParams params = (LayoutParams)mExpandView.getLayoutParams();
		if (params.bottomMargin == 0) {
			onClick(null);
		}
	}
	public boolean isExpanded() {
		if (mExpandView == null)return false;
		if (!mExpandView.isShown())return false;
		LayoutParams params = (LayoutParams)mExpandView.getLayoutParams();
		if (params.bottomMargin == 0)return true;
		return false;
	}
	public void setOnClickListener(OnClickListener listener) {
		mListener = listener;
	}
	
	@Override
	public void onClick(View v) {
		if (mExpandView == null)return;
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mExpandView.getLayoutParams();
		if (params.bottomMargin != 0 && mListener != null) {
			mListener.onWillExpand(this);
		} else if (params.bottomMargin == 0 && mListener != null) {
			mListener.onWillShrink(this);
		}
		ExpandAnimation expandAni = new ExpandAnimation(mExpandView, 500);
		mExpandView.startAnimation(expandAni);
		if (expandAni.isVisibleAfter()) {
			updateIcon(mShrink);
		} else {
			updateIcon(mExpand);
		}
	}
	private void updateIcon(Drawable drawable) {
		if (mPostion == IconPostion.Left) {
			setCompoundDrawables(drawable, null, null, null);
		} else if (mPostion == IconPostion.Right) {
			setCompoundDrawables(null, null, drawable, null);
		} else if (mPostion == IconPostion.Bottom) {
			setCompoundDrawables(null, null, null, drawable);
		} else {
			setCompoundDrawables(null, drawable, null, null);
		}
	}
	private void updateIcon() {
		if (!isExpanded()) {
			updateIcon(mExpand);
		} else {
			updateIcon(mShrink);
		}
	}
	
	public interface OnClickListener {
		void onWillExpand(ExpandTextView view);
		void onWillShrink(ExpandTextView view);
	};
	
	private View mExpandView = null;
	private Drawable mExpand = null;
	private Drawable mShrink = null;
	private IconPostion mPostion = IconPostion.Right;
	private OnClickListener mListener = null;
}

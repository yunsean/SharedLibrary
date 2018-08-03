package com.dylan.uiparts.textview;

import com.dylan.common.data.DimeUtil;
import com.dylan.common.data.StrUtil;
import com.dylan.uiparts.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

public class AspectTextView extends TextView {

	private float mSizeAspect = -1;
	private int mMinHeight = 0;
	public AspectTextView(Context context) {
		this(context, null);
	}
	public AspectTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public AspectTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectLayout, defStyleAttr, 0);
			mSizeAspect = a.getFloat(R.styleable.AspectLayout_al_aspect, -1.f);
			a.recycle();
			String minHeight = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "minHeight");
			if (StrUtil.isNotBlank(minHeight)) {
				mMinHeight = (int) DimeUtil.dime2px(context, minHeight);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mSizeAspect > 0.01f) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = (int) (width * mSizeAspect);
			if (mMinHeight > height)height = mMinHeight;
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(widthMeasureSpec));
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);	
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);			
		}
	}

}

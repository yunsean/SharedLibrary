package com.dylan.uiparts.layout;

import com.dylan.uiparts.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AspectRelativeLayout extends RelativeLayout {

	private float mSizeAspect = -1;
	public AspectRelativeLayout(Context context) {
		this(context, null);
	}
	public AspectRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public AspectRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectLayout, defStyleAttr, 0);
			mSizeAspect = a.getFloat(R.styleable.AspectLayout_al_aspect, -1.f);
			a.recycle();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mSizeAspect > 0.01f) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = (int) (width * mSizeAspect);
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(widthMeasureSpec));
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);	
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);			
		}
	}

}

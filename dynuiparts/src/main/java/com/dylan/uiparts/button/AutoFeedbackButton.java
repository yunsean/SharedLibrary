package com.dylan.uiparts.button;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;

import com.dylan.common.sketch.Drawables;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import com.dylan.uiparts.fontable.FontableButton;

public class AutoFeedbackButton extends FontableButton {
	public AutoFeedbackButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	public AutoFeedbackButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NetImageView, defStyle, 0);
			int bgColor = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "background", 0); 
			int bgRes = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "background", 0);
			if (bgColor != 0) {
				int normal = bgColor;
				int pressed = bgColor & 0xff000000;
				for (int i = 0; i < 24; i += 8) {
					pressed += (int)(((bgColor >> i) & 0xff) * 0.8) << i;
				}
				StateListDrawable csl = Drawables.createStateListDrawable(normal, pressed);
				if (Utility.isJellyBeanOrLater()) {
					setBackground(csl);
				} else {
					setBackgroundDrawable(csl);
				}
			} else if (bgRes != 0) {
				Drawable normal = getResources().getDrawable(bgRes);
				Drawable pressed = normal;
				pressed.setColorFilter(0xffdfdfdf, PorterDuff.Mode.MULTIPLY);
				StateListDrawable csl = Drawables.createStateListDrawable(normal, pressed);
				if (Utility.isJellyBeanOrLater()) {
					setBackground(csl);
				} else {
					setBackgroundDrawable(csl);
				}
			}
			a.recycle();
		}
	}
}

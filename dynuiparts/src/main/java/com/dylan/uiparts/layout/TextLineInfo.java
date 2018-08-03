package com.dylan.uiparts.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dylan.common.data.DimeUtil;
import com.dylan.common.sketch.Sketch;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import com.dylan.uiparts.edittext.CleanableEditText;

public class TextLineInfo extends LinearLayout {

	private TextView mValue = null;
	private TextView mName = null;
	public TextLineInfo(Context context) {
		this(context, null);
	}
	public TextLineInfo(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public TextLineInfo(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if (attrs == null)return;
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextLineView, defStyleAttr, 0);
		boolean editable = false;
		boolean customizeLayout = false;
		if (a != null)editable = a.getBoolean(R.styleable.TextLineView_tl_editable, false);
		int layoutResId = editable ? R.layout.control_line_edit : R.layout.control_line_info;
		if (a.hasValue(R.styleable.TextLineView_tl_layoutResId)) {
			layoutResId = a.getResourceId(R.styleable.TextLineView_tl_layoutResId, layoutResId);
			customizeLayout = true;
		}
		inflate(context, layoutResId, this);
		mName = (TextView) findViewById(R.id.tv_name);
		mValue = (TextView) findViewById(R.id.tv_value);
		if (a != null) {
			mName.setText(a.getString(R.styleable.TextLineView_tl_name));
			mValue.setHint(a.getString(R.styleable.TextLineView_tl_hint));
			mValue.setText(a.getString(R.styleable.TextLineView_tl_value));
		}
		if (!customizeLayout && attrs != null) {
			int color = a.getColor(R.styleable.TextLineView_tl_color, 0xff555555);
			mName.setTextColor(a.getColor(R.styleable.TextLineView_tl_colorName, color));
			mValue.setTextColor(a.getColor(R.styleable.TextLineView_tl_colorValue, color));
			if (a.hasValue(R.styleable.TextLineView_tl_size)) {
				float size = a.getDimension(R.styleable.TextLineView_tl_size, Utility.sp2px(context, 14.f));
				mName.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
				mValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
			}
			mValue.setHintTextColor(a.getColor(R.styleable.TextLineView_tl_colorHint, 0xff777777));
			if (editable)((CleanableEditText)mValue).setEnableClean(a.getBoolean(R.styleable.TextLineView_tl_cleanIcon, false));
			if (a.hasValue(R.styleable.TextLineView_tl_background)) {
				setBackground(a.getDrawable(R.styleable.TextLineView_tl_background));
			}
			mName.setMinWidth((int) a.getDimension(R.styleable.TextLineView_tl_minLeftWidth, 0));
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mValue.getLayoutParams();
			layoutParams.leftMargin = (int) a.getDimension(R.styleable.TextLineView_tl_paddingBetween, 0);
			if (a.hasValue(R.styleable.TextLineView_tl_widthRatio)) {
				float widthRatio = a.getFloat(R.styleable.TextLineView_tl_widthRatio, 0.f);
				layoutParams = (LinearLayout.LayoutParams) mValue.getLayoutParams();
				layoutParams.width = 0;
				layoutParams.weight = (int)(widthRatio * 100);
				layoutParams = (LinearLayout.LayoutParams) mName.getLayoutParams();
				layoutParams.width = 0;
				layoutParams.weight = 100 - (int)(widthRatio * 100);
			}

			if (editable && !isInEditMode()) {
				((CleanableEditText)mValue).setInputType(attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "inputType", 0));
			}
			int resId = 0;
			resId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "drawableLeft", 0);
			if (resId != 0)Sketch.set_leftDrawable(mName, resId);
			resId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "drawableRight", 0);
			if (resId != 0)Sketch.set_rightDrawable(mValue, resId);
			String value = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "drawablePadding");
			int diem = (int)DimeUtil.dime2px(context, value);
			mName.setCompoundDrawablePadding(diem);
			mValue.setCompoundDrawablePadding(diem);
			if (!isInEditMode()) {
				mValue.setGravity(attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "gravity", Gravity.RIGHT));
			} else {
				value = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "gravity");
				if (value != null && value.equals("left"))mValue.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				else if (value != null && value.equals("center"))mValue.setGravity(Gravity.CENTER);
				else mValue.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			}
		}
	}

	public void setText(String value) {
		mValue.setText(value);
	}
	public String getText() {
		return mValue.getText().toString();
	}
	public void setName(String name) {
		mName.setText(name);
	}
	
	public TextView name() {
		return mName;
	}
	public TextView value() {
		return mValue;
	}
}

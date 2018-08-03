package com.dylan.uiparts.fontable;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.dylan.uiparts.R;

public class FontableButton extends Button {

	public FontableButton(Context context) {
		this(context, null);
	}
	public FontableButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public FontableButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontableView, defStyle, 0);
		String family = a.getString(R.styleable.FontableView_fv_family);
		boolean bold = a.getBoolean(R.styleable.FontableView_fv_bold, false);
		boolean strike = a.getBoolean(R.styleable.FontableView_fv_strikeThru, false);
		boolean underline = a.getBoolean(R.styleable.FontableView_fv_underline, false);
		a.recycle();
		
		if (family != null && family.length() > 0) {
			AssetManager mgr = getContext().getAssets();
			Typeface tf = Typeface.createFromAsset(mgr, family);
			setTypeface(tf);
		} else if (!isInEditMode()) {
			Application app = (Application) getContext().getApplicationContext();
			if (app instanceof com.dylan.common.application.Application) {
				Typeface tf = ((com.dylan.common.application.Application)app).getTypeface();
				setTypeface(tf);
			}
		}
		if (bold) {
			setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			getPaint().setFakeBoldText(true);
		}
		if (strike) {
			getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		}
		if (underline) {
			getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		}
	}

}

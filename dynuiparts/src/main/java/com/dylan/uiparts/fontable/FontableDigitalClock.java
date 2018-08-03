package com.dylan.uiparts.fontable;

import com.dylan.uiparts.R;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.DigitalClock;

@SuppressWarnings("deprecation")
public class FontableDigitalClock extends DigitalClock {

	public FontableDigitalClock(Context context) {
		this(context, null);
	}
	public FontableDigitalClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontableView);
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

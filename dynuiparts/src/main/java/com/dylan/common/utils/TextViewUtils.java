package com.dylan.common.utils;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextViewUtils {
	public static final void setBold(TextView tv) {
		tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		tv.getPaint().setFakeBoldText(true);
	}
	public static final void setFontFamily(TextView tv, String font) {
		AssetManager mgr = tv.getContext().getAssets();
		Typeface tf = Typeface.createFromAsset(mgr, font);
		tv.setTypeface(tf);
	}
	public static final void setFontFamily(Activity activity, String font) {
		View view = activity.getWindow().getDecorView().getRootView();
		setFontFamily(view, font);
	}
	public static final void setFontFamily(View parent, String font) {
		AssetManager mgr = parent.getContext().getAssets();
		Typeface tf = Typeface.createFromAsset(mgr, font);
		if (parent instanceof ViewGroup) {
			ViewGroup vp = (ViewGroup) parent;
			setFontFamily(tf, vp);
		}
	}
	private static final void setFontFamily(Typeface tf, ViewGroup vg) {
		for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof TextView) {
            	((TextView)child).setTypeface(tf);
            } else if (child instanceof ViewGroup) {
            	setFontFamily(tf, (ViewGroup)child);
            }
        }
	}
}

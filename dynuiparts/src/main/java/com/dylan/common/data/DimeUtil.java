package com.dylan.common.data;

import java.security.InvalidParameterException;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class DimeUtil {

	public static int screenWidth(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}
	public static int screenHeight(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
	
	public static float dime2px(Context context, String dime) {
		if (dime == null || dime.length() < 1) {
			return 0.0f;
		}
		try {
			String number = "";
			String unit = "";
			for (int i = 0; i < dime.length(); i++) {
				char c = dime.charAt(i);
				if (c >= '0' && c <= '9') {
					number += c;
				} else if (c == '.') {
					number += c; 
				} else {
					unit = dime.substring(i);
					break;
				}
			}
			float num = Float.valueOf(number);
			if (unit.equals("dip") || unit.equals("dp")) {
				return dip2px(context, num);
			} else if (unit.equals("px")) {
				return num;
			} else if (unit.equals("sp")) {
				return sp2px(context, num);
			}
		} catch (Exception ex) {
			return .0f;
		}
		throw new InvalidParameterException();
	}
}

package com.dylan.uiparts.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dylan.uiparts.R;

public class ToastEx {
	public final static int LENGTH_SHORT = Toast.LENGTH_SHORT;
	public final static int LENGTH_LONG = Toast.LENGTH_LONG; 
	
	private static boolean mStylized = false;
	private static int mGravity = Gravity.BOTTOM;
	private static int mOffsetX = 0;
	private static int mOffsetY = 0;
	@SuppressLint("ShowToast")
	public static Toast makeText(Context context, CharSequence text, int duration) {
		Toast toast = Toast.makeText(context, text, duration);
		if (!mStylized) {
			Application app = (Application) context.getApplicationContext();
			if (app instanceof com.dylan.common.application.Application) {
				com.dylan.common.application.Application appex = (com.dylan.common.application.Application)app;
				mGravity = appex.getToastGravity();
				mOffsetX = appex.getToastOffsetX();
				mOffsetY = appex.getToastOffsetY();
				mStylized = true;
			}
		}
		toast.setGravity(mGravity, mOffsetX, mOffsetY);
		return toast;
	}
	public static Toast makeText(Context context, CharSequence text, int duration, int gravity) {
		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(gravity, 0, 0);
		return toast;
	}
	public static Toast makeText(Context context, CharSequence text, int duration, int gravity, int offsetX, int offsetY) {
		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(gravity, offsetX, offsetY);
		return toast;
	}

	public interface SetupContentViewListener {
		void setupContentView(View contentView);
	}
	public static Toast makeToast(Context context, int resId, int duration, SetupContentViewListener listener) {
		Toast toast = Toast.makeText(context, "", duration);
		View content = LayoutInflater.from(context).inflate(resId, null);
		if (listener != null) {
			listener.setupContentView(content);
		}
		toast.setView(content);
		if (!mStylized) {
			Application app = (Application) context.getApplicationContext();
			if (app instanceof com.dylan.common.application.Application) {
				com.dylan.common.application.Application appex = (com.dylan.common.application.Application)app;
				mGravity = appex.getToastGravity();
				mOffsetX = appex.getToastOffsetX();
				mOffsetY = appex.getToastOffsetY();
				mStylized = true;
			}
		}
		toast.setGravity(mGravity, mOffsetX, mOffsetY);
		return toast;
	}
	public static Toast makeToast(Context context, int resId, int duration) {
		return makeToast(context, resId, duration, null);
	}

	@SuppressLint("InflateParams")
	public static void showToast(Activity context, String tips, int duration) {
		LayoutInflater inflater = context.getLayoutInflater();
		View layout = inflater.inflate(R.layout.layout_toast, null);
		if (tips != null) {
			((TextView) layout.findViewById(R.id.infos)).setText(tips);
		}
		Toast toast = new Toast(context);
		toast.setView(layout);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(duration);
		toast.show();
	}

	public static void showToast(Activity context, String tips) {
		showToast(context, tips, Toast.LENGTH_SHORT);
	}

	@SuppressLint("InflateParams")
	public static Toast makeToast(Activity context, String tips, int duration) {
		LayoutInflater inflater = context.getLayoutInflater();
		View layout = inflater.inflate(R.layout.layout_toast, null);
		if (tips != null) {
			((TextView) layout.findViewById(R.id.infos)).setText(tips);
		}
		Toast toast = new Toast(context);
		toast.setView(layout);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(duration);
		return toast;
	}

	public static Toast makeToast(Activity context, String tips) {
		return makeToast(context, tips, Toast.LENGTH_SHORT);
	}

}

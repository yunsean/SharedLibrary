package com.dylan.common.sketch;

import com.dylan.common.utils.Utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

public class Observers {

	public interface OnLayoutChangedListener {
		boolean onLayoutChanged(View v);		//Will stop observe while return false.
	}		
	public static void observeLayout(final View view, final OnLayoutChangedListener listener) {
		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if (listener != null && !listener.onLayoutChanged(view)) {
					if (Utility.isJellyBeanOrLater()) {
						view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			}
		});
	}
	public static void observeLayout(final Activity activity, final OnLayoutChangedListener listener) {
		observeLayout(activity.getWindow().getDecorView().getRootView(), listener);
	}
	
	public static void observeTv(View view, TextWatcher watcher) {
		if (view != null && view instanceof TextView) {
			((TextView)view).addTextChangedListener(watcher);
		}
	}
	public static void observeTv(Activity activity, int resId, TextWatcher watcher) {
		observeTv(activity.findViewById(resId), watcher);
	}
	public static void observeTv(View parent, int resId, TextWatcher watcher) {
		observeTv(parent.findViewById(resId), watcher);
	}
	public static void observeTv(TextWatcher watcher, View...views) {
		for (int i = 0; i < views.length; i++) {
			observeTv(views[i], watcher);
		}
	}
	public static void observeTv(Activity activity, TextWatcher watcher, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			observeTv(activity.findViewById(resIds[i]), watcher);
		}
	}
	public static void observeTv(View parent, TextWatcher watcher, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			observeTv(parent.findViewById(resIds[i]), watcher);
		}
	}
	
	public interface OnTextChangedListener {
		void afterTextChanged(Editable s);
	}
	public static void observeTv(View view, final OnTextChangedListener watcher) {
		if (view != null && view instanceof TextView) {
			((TextView)view).addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					watcher.afterTextChanged(s);
				}
			});
		}
	}
	public static void observeTv(Activity activity, int resId, OnTextChangedListener watcher) {
		observeTv(activity.findViewById(resId), watcher);
	}
	public static void observeTv(View parent, int resId, OnTextChangedListener watcher) {
		observeTv(parent.findViewById(resId), watcher);
	}
	public static void observeTv(OnTextChangedListener watcher, View...views) {
		for (int i = 0; i < views.length; i++) {
			observeTv(views[i], watcher);
		}
	}
	public static void observeTv(Activity activity, OnTextChangedListener watcher, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			observeTv(activity.findViewById(resIds[i]), watcher);
		}
	}
	public static void observeTv(View parent, OnTextChangedListener watcher, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			observeTv(parent.findViewById(resIds[i]), watcher);
		}
	}
}

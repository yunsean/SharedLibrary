package com.dylan.common.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dylan.uiparts.R;
import com.dylan.uiparts.annimation.SizeAnimation;
import com.dylan.uiparts.imageview.AutoAdjustImageView;

public class LoadIndicator {
	public enum AnimationMode {Alpha, Rotate};
	public enum LayoutMode {None, Vertical, Horizontal, Override};
	
	public static void showLoading(Activity activity) {
		showLoading(activity.getWindow().getDecorView().getRootView(), null, null);
	}
	public static void showLoading(Activity activity, String tips) {
		showLoading(activity.getWindow().getDecorView().getRootView(), tips, null);
	}
	public static void showLoading(Activity activity, String tips, Drawable background) {
		showLoading(activity.getWindow().getDecorView().getRootView(), tips, background);
	}
	public static void showLoading(View parent) {
		showLoading(parent, null, null);
	}
	public static void showLoading(View parent, String tips, Drawable background) {
		if (parent == null)return;
		final View loading = parent.findViewById(R.id.loading_panel);
		if (loading == null) return;
		if (!(loading.getParent() instanceof LinearLayout)) {
			if (loading.getTag() == null || !(loading.getTag() instanceof Integer) || ((Integer)loading.getTag()).intValue() != 0xffffffff) {
				loading.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
							removeOnGlobalLayoutListener();
						} else {
							loading.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
						if (loading.getTag() == null)loading.setTag(0xffffffff);
						if (loading.getParent() instanceof ViewGroup) {
							ViewGroup parent = (ViewGroup) loading.getParent();
							int height = parent.getHeight();
							loading.getLayoutParams().height = height;
							loading.requestLayout();
						}
					}
					@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
					private void removeOnGlobalLayoutListener() {
						loading.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
				});
			}
			LayoutParams lp = loading.getLayoutParams();
			lp.height = Utility.getScreenHeight(parent.getContext());
			loading.requestLayout();
		}
		loading.setVisibility(View.VISIBLE);
		loading.setOnClickListener(null);
		if (parent.findViewById(R.id.content_panel) != null) {
			parent.findViewById(R.id.content_panel).setVisibility(View.GONE);
		}
		ImageButton indicator = (ImageButton)parent.findViewById(R.id.indicator);
		if (indicator != null) {
			AnimationMode mode = AnimationMode.Alpha;
			int loadingResId = R.drawable.net_loading;
			int backgroundResId = 0;
			Application app = (Application) parent.getContext().getApplicationContext();
			if (app instanceof com.dylan.common.application.Application) {
				com.dylan.common.application.Application appex = (com.dylan.common.application.Application)app;
				mode = appex.getLoadAnimation();
				loadingResId = appex.getLoadingIcon();
				backgroundResId = appex.getLoadingBackground();
			}
			AutoAdjustImageView bg = (AutoAdjustImageView) loading.findViewById(R.id.background);
			if (background != null) {
				bg.setVisibility(View.VISIBLE);
				bg.setBackgroundDrawable(background);
			} else if (backgroundResId != 0) {
				bg.setVisibility(View.VISIBLE);
				bg.setBackgroundResource(backgroundResId);
			} else {
				bg.setVisibility(View.GONE);
			}
			indicator.setImageResource(loadingResId);
			Animation anim = null;
			switch (mode) {
			case Alpha:
				anim = new AlphaAnimation(0.2f, 0.8f);
				anim.setRepeatMode(Animation.REVERSE);
				break;
			case Rotate:
				anim = new RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				anim.setRepeatMode(Animation.INFINITE);
				break;
			} 
			anim.setRepeatCount(-1);
			anim.setDuration(2000);
			anim.setInterpolator(new LinearInterpolator());
			indicator.startAnimation(anim);
		}
		if (parent.findViewById(R.id.net_tips) != null) {
			TextView atips = (TextView)parent.findViewById(R.id.net_tips);
			if (tips != null) {
				atips.setText(tips);
			} else {
				atips.setText(R.string.net_loading);
			}
		}
	}
	public static void showNetError(Activity activity, String reason, View.OnClickListener retry) {
		showNetError(activity.getWindow().getDecorView().getRootView(), reason, retry);
	}
	public static void showNetError(View parent, String reason, View.OnClickListener retry) {
		if (parent == null)return;
		View loading = parent.findViewById(R.id.loading_panel);
		if (loading == null)return;
		loading.setVisibility(View.VISIBLE);
		loading.setOnClickListener(retry);
		if (parent.findViewById(R.id.content_panel) != null) {
			parent.findViewById(R.id.content_panel).setVisibility(View.GONE);
		}
		ImageButton indicator = (ImageButton)parent.findViewById(R.id.indicator);
		if (indicator != null) {
			int netErrorResId = R.drawable.net_error;
			Application app = (Application) parent.getContext().getApplicationContext();
			if (app instanceof com.dylan.common.application.Application) {
				com.dylan.common.application.Application appex = (com.dylan.common.application.Application)app;
				netErrorResId = appex.getNetErrorIcon();
			}
			indicator.clearAnimation();
			indicator.setImageResource(netErrorResId);
		}
		if (parent.findViewById(R.id.net_tips) != null) {
			TextView atips = (TextView)parent.findViewById(R.id.net_tips);
			if (reason != null) {
				atips.setText(reason);
			} else {
				atips.setText(R.string.net_error);
			}
		}
	}
	public static void hideLoading(Activity activity, LayoutMode mode) {
		hideLoading(activity.getWindow().getDecorView().getRootView(), mode);
	}
	public static void hideLoading(View parent, LayoutMode mode) {
		if (parent == null)return;
		final View loading = parent.findViewById(R.id.loading_panel);
		if (loading == null)return;
		loading.setOnClickListener(null);
		if (parent.findViewById(R.id.content_panel) != null) {
			parent.findViewById(R.id.content_panel).setVisibility(View.VISIBLE);
		}
		switch (mode) {
		case Horizontal: {
			SizeAnimation anim = new SizeAnimation(loading, SizeAnimation.Mode.Width, loading.getWidth(), 0);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					loading.setVisibility(View.GONE);
					ImageButton indicator = (ImageButton)loading.findViewById(R.id.indicator);
					if (indicator != null) {
						indicator.clearAnimation();
					}
					LayoutParams lp = (LayoutParams)loading.getLayoutParams();
					lp.width = LayoutParams.MATCH_PARENT;
				}
			});
			loading.clearAnimation();
			loading.setAnimation(anim);
			anim.setDuration(500);
			anim.setFillAfter(true);
			loading.setAnimation(anim);
			break;
		}
		case Vertical: {
			SizeAnimation anim = new SizeAnimation(loading, SizeAnimation.Mode.Height, loading.getHeight(), 0);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					loading.setVisibility(View.GONE);
					ImageButton indicator = (ImageButton)loading.findViewById(R.id.indicator);
					if (indicator != null) {
						indicator.clearAnimation();
					}
					LayoutParams lp = (LayoutParams)loading.getLayoutParams();
					lp.height = LayoutParams.MATCH_PARENT;
				}
			});
			loading.clearAnimation();
			loading.setAnimation(anim);
			anim.setDuration(500);
			anim.setFillAfter(true);
			loading.setAnimation(anim);
			loading.setAnimation(anim);
			break;
		}
		case Override: {
			AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					loading.setVisibility(View.GONE);
					ImageButton indicator = (ImageButton)loading.findViewById(R.id.indicator);
					if (indicator != null)indicator.clearAnimation();
				}
			});
			anim.setDuration(500);
			anim.setFillAfter(false);
			loading.startAnimation(anim);
			break;
		}
		default: {
			loading.setVisibility(View.GONE);
			ImageButton indicator = (ImageButton)loading.findViewById(R.id.indicator);
			if (indicator != null) {
				indicator.clearAnimation();
			}
			break;
		}
		}
	}
	public static void hideLoading(Activity activity) {
		hideLoading(activity.getWindow().getDecorView().getRootView());
	}
	public static void hideLoading(View parent) {
		hideLoading(parent, LayoutMode.None);
	}
}

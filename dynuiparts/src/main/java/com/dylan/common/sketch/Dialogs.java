package com.dylan.common.sketch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;

public class Dialogs {
	public static void showMessage(Context ctx, String msg) {
		new AlertDialog.Builder(ctx)
			.setTitle("错误")
			.setCancelable(false)
			.setMessage(msg)
			.setPositiveButton("确定", null)
			.show();
	}
	public static void showMessage(Context ctx, String title, String msg) {
		new AlertDialog.Builder(ctx)
			.setTitle(title)
			.setCancelable(false)
			.setMessage(msg)
			.setPositiveButton("确定", null)
			.show();
	}
	public static void showMessage(Context ctx, String title, String msg, DialogInterface.OnClickListener listener) {
		new AlertDialog.Builder(ctx)
			.setTitle(title)
			.setCancelable(false)
			.setMessage(msg)
			.setPositiveButton("确定", listener)
			.show();
	}
	public static void showMessage(Context ctx, String title, String msg, String btn1, String btn2, DialogInterface.OnClickListener listener1, DialogInterface.OnClickListener listener2) {
		new AlertDialog.Builder(ctx)
			.setTitle(title)
			.setCancelable(false)
			.setMessage(msg)
			.setPositiveButton(btn1, listener1)
			.setNegativeButton(btn2, listener2)
			.show();
	}

	public static class WaitingDialog extends Dialog {
		private int mResId = R.layout.dialog_wating;
		public WaitingDialog(Context context) {
			super(context, R.style.waitingDialogStyle);
		}
		public WaitingDialog(Context context, int stylable) {
			super(context, stylable == 0 ? R.style.waitingDialogStyle : stylable);
		}
		public WaitingDialog(Context context, int stylable, int resId) {
			super(context, stylable == 0 ? R.style.waitingDialogStyle : stylable);
			if (resId != 0)mResId = resId;
		}
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(mResId);
			LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.LinearLayout);
			if (linearLayout != null)linearLayout.getBackground().setAlpha(210);
			this.setCanceledOnTouchOutside(false);
		}
		public WaitingDialog setTips(String tips) {
			TextView tv = (TextView) this.findViewById(R.id.tips);
			if (tv != null)tv.setText(tips);
			return this;
		}
	}

	public static WaitingDialog showWait(Context context, @LayoutRes int resId, int style, String tips, final OnCancelListener onCancel) {
		final WaitingDialog dialog = new WaitingDialog(context, style, resId);
		dialog.setCancelable(false);
		dialog.show();
		if (tips != null) {
			dialog.setTips(tips);
		}
		if (onCancel != null) {
			dialog.findViewById(R.id.cancel).setVisibility(View.VISIBLE);
			dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onCancel.onCancel(dialog);
				}
			});
		} else {
			dialog.findViewById(R.id.cancel).setVisibility(View.GONE);
		}
		return dialog;
	}
	public static WaitingDialog showWait(Context context, int resId, String tips, OnCancelListener onCancel) {
		return showWait(context, resId, 0, tips, onCancel);
	}
	public static WaitingDialog showWait(Context context, String tips, OnCancelListener onCancel) {
		return showWait(context, R.layout.dialog_wating, tips, onCancel);
	}
	public static WaitingDialog showWait(Context context, String tips) {
		return showWait(context, tips, null);
	}
	public static WaitingDialog showWait(Context context, int resId, int style) {
		return showWait(context, resId, style, null, null);
	}
	public static WaitingDialog showWait(Context context, int resId) {
		return showWait(context, resId, null, null);
	}
	public static WaitingDialog showWait(Context context) {
		return showWait(context, null, null);
	}

	public interface OnSettingDialogListener {
		void onSettingDialog(Dialog dialog, View contentView);
	}
	public interface OnDialogItemClickedListener {
		void onClick(Dialog dialog, View contentView, View clickedView);
	}
	public interface OnDismissListener {
		void onDismiss();
	}
	public static Dialog createDialog(Context context, int dialogResId, OnSettingDialogListener setting, int[] clickableResId, final OnDialogItemClickedListener clicked) {
		return createDialog(context, dialogResId, setting, clickableResId, clicked, 0);
	}
	public static Dialog createDialog(Context context, int dialogResId, OnSettingDialogListener setting, int[] clickableResId, final OnDialogItemClickedListener clicked, OnDismissListener dismiss) {
		return createDialog(context, dialogResId, setting, clickableResId, clicked, dismiss, 0, 0, R.style.CenterDialog);
	}
	public static Dialog createDialog(Context context, int dialogResId, OnSettingDialogListener setting, int dismissDelay, final OnDismissListener dismiss) {
		return createDialog(context, dialogResId, setting, null, null, dismiss, dismissDelay, 0, R.style.CenterDialog);
	}
	public static Dialog createDialog(Context context, int dialogResId, OnSettingDialogListener setting, int[] clickableResId, final OnDialogItemClickedListener clicked, int dialogWidth) {
		return createDialog(context, dialogResId, setting, clickableResId, clicked, dialogWidth, R.style.CenterDialog);
	}
	public static Dialog createDialog(Context context, int dialogResId, OnSettingDialogListener setting, int[] clickableResId, final OnDialogItemClickedListener clicked, int dialogWidth, int styleResId) {
		return createDialog(context, dialogResId, setting, clickableResId, clicked, null, -1, dialogWidth, styleResId);
	}
	public static Dialog createDialog(Context context, int dialogResId, OnSettingDialogListener setting, int[] clickableResId, final OnDialogItemClickedListener clicked, final OnDismissListener dismiss, int dismissDelay, int dialogWidth, int styleResId) {
		final View view = LayoutInflater.from(context).inflate(dialogResId, null);
		final Dialog dialog = new Dialog(context, styleResId);
		if (setting != null)setting.onSettingDialog(dialog, view);
		if (clicked != null) {
			for (int i : clickableResId) {
				view.findViewById(i).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						clicked.onClick(dialog, view, v);
					}
				});
			}
		}
		dialog.setTitle(null);
		dialog.setCancelable(true);
		if (dismiss != null) {
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					dismiss.onDismiss();
				}
			});
		}
		dialog.setContentView(view);
		if (dialogWidth == 0) {
			if (Utility.isTabletDevice(context)) {
				dialogWidth = Utility.screenWidth(context) * 4 / 10;
			} else {
				dialogWidth = Utility.screenWidth(context) * 8 / 10;
			}
		}
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.width = dialogWidth;
		window.setWindowAnimations(R.style.DialogBottomAnimate);
		dialog.onWindowAttributesChanged(wl);
		if (dismissDelay > 0) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					dialog.dismiss();
				}
			}, dismissDelay);
		}
		return dialog;
	}
	public static Dialog createBottomDialog(Context context, int dialogResId, OnSettingDialogListener setting, int[] clickableResId, final OnDialogItemClickedListener clicked) {
		return createBottomDialog(context, dialogResId, setting, clickableResId, clicked, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}
	public static Dialog createBottomDialog(Context context, int dialogResId, OnSettingDialogListener setting, int[] clickableResId, final OnDialogItemClickedListener clicked, int dialogWidth, int dialogHeight) {
		final View view = LayoutInflater.from(context).inflate(dialogResId, null);
		final Dialog dialog = new Dialog(context, R.style.BottomDialog);
		if (setting != null)setting.onSettingDialog(dialog, view);
		dialog.setTitle(null);
		dialog.setContentView(view);
		dialog.setCancelable(true);
		if (clicked != null) {
			for (int i : clickableResId) {
				view.findViewById(i).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						clicked.onClick(dialog, view, v);
					}
				});
			}
		}
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = Utility.getScreenHeight(context);
		wl.width = dialogWidth;
		wl.height = dialogHeight;
		window.setWindowAnimations(R.style.DialogBottomAnimate);
		dialog.onWindowAttributesChanged(wl);
		return dialog;
	}
}

package com.dylan.common.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogEx {
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
}

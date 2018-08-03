package com.dylan.common.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dylan.uiparts.R;

class WaitingDialog extends Dialog {
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

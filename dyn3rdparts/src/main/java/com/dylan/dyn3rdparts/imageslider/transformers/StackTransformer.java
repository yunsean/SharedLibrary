package com.dylan.dyn3rdparts.imageslider.transformers;

import android.view.View;

import com.dylan.dyn3rdparts.imageslider.helper.ViewHelper;

public class StackTransformer extends BaseTransformer {

	@Override
	protected void onTransform(View view, float position) {
		ViewHelper.setTranslationX(view,position < 0 ? 0f : -view.getWidth() * position);
	}

}

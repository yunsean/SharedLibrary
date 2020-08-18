package com.dylan.common.sketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.dylan.common.utils.Utility;

public class Resources {

	public static Drawable getDrawable(Context ctx, int resId) {
		Drawable drawable = ctx.getResources().getDrawable(resId);
		Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
		drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
		return drawable;
	}

	public static final int getColor(Context context, int id) {
		if (Utility.isMOrLater()) {
			return ContextCompat.getColor(context, id);
		} else {
			//noinspection deprecation
			return context.getResources().getColor(id);
		}
	}
}

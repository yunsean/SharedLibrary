package com.dylan.common.sketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.text.TextPaint;

import com.dylan.common.utils.Utility;

import java.io.FileNotFoundException;

public class Drawables {

	public static StateListDrawable createStateListDrawable(int normal, int pressed, int focused, int unable) {  
        return createStateListDrawable(new ColorDrawable(normal), new ColorDrawable(pressed), new ColorDrawable(focused), new ColorDrawable(unable));
    }  
	public static StateListDrawable createStateListDrawable(int normal, int pressed) {  
        return createStateListDrawable(new ColorDrawable(normal), new ColorDrawable(pressed));
    }  
	public static StateListDrawable createStateListDrawable(Drawable normal, Drawable pressed, Drawable focused, Drawable unable) {  
        StateListDrawable drawableList = new StateListDrawable();   
        drawableList.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, pressed);  
        drawableList.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_focused }, focused);  
        drawableList.addState(new int[] { android.R.attr.state_enabled }, normal);  
        drawableList.addState(new int[] { android.R.attr.state_focused }, focused);
        drawableList.addState(new int[] { android.R.attr.state_window_focused }, unable);  
        drawableList.addState(new int[] {}, normal);  
        return drawableList;  
    }  
	public static StateListDrawable createStateListDrawable(Drawable normal, Drawable pressed, Drawable focused, Drawable unable, Drawable checked) {  
        StateListDrawable drawableList = new StateListDrawable();  
        drawableList.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, pressed);  
        drawableList.addState(new int[] { android.R.attr.state_checked, android.R.attr.state_enabled }, checked);  
        drawableList.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_focused }, focused);  
        drawableList.addState(new int[] { android.R.attr.state_enabled }, normal);  
        drawableList.addState(new int[] { android.R.attr.state_focused }, focused);
        drawableList.addState(new int[] { android.R.attr.state_window_focused }, unable);  
        drawableList.addState(new int[] {}, normal);  
        return drawableList;  
    }  
	public static StateListDrawable createStateListDrawable(Drawable normal, Drawable pressed) {
		StateListDrawable drawableList = new StateListDrawable();   
        drawableList.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, pressed);   
        drawableList.addState(new int[] { android.R.attr.state_enabled }, normal); 
        drawableList.addState(new int[] {}, normal);  
        return drawableList;  
	}
	
	public static ShapeDrawable createRoundCornerShapeDrawable(float radius, float borderLength, int borderColor) {
        float[] outerRadii = new float[8];
        float[] innerRadii = new float[8];
        for (int i = 0; i < 8; i++) {
            outerRadii[i] = radius + borderLength;
            innerRadii[i] = radius;
        } 
        ShapeDrawable sd = new ShapeDrawable(new RoundRectShape(outerRadii, new RectF(borderLength, borderLength, borderLength, borderLength), innerRadii));
        sd.getPaint().setColor(borderColor); 
        return sd;
    }
		
	@SuppressWarnings("deprecation")
	public static Drawable getDrawable(Context context, int resId) {
		Drawable d = null;
		if (Utility.isLollipopOrLater()) {
			d = context.getDrawable(resId);
		} else {
			d = context.getResources().getDrawable(resId);
		}
		if (d == null)return null;
		if (d instanceof StateListDrawable) {
			StateListDrawable sld = (StateListDrawable)d;
			Drawable cd = sld.getCurrent();
			if (cd != null) {
				Bitmap b = ((BitmapDrawable)cd).getBitmap();
				if (b != null) {
					sld.setBounds(0, 0, b.getWidth(), b.getHeight());
				}
			}
		} else if (d instanceof BitmapDrawable) {
			Bitmap b = ((BitmapDrawable)d).getBitmap();
			if (b != null) {
				d.setBounds(0, 0, b.getWidth(), b.getHeight());
			}
		}
		return d;
	}

	public static Drawable getDrawable(Drawable d, int width) {
		if (d == null)return null;
		Bitmap b = ((BitmapDrawable)d).getBitmap();
		if (b != null) {
			int w = b.getWidth();
			int h = b.getHeight();
			int height = h * width / w;
			d.setBounds(0, 0, width, height);
		} 
		return d;
	}
    public static Drawable getDrawable(Bitmap bitmap) {
        BitmapDrawable bitmapDrawable = new BitmapDrawable(null, bitmap);
        return bitmapDrawable;
    }
	public static Drawable getDrawable(Bitmap b, int width) {
		if (b == null)return null;
		Drawable d = new BitmapDrawable(null, b);
		if (d != null) {
			int w = b.getWidth();
			int h = b.getHeight();
			int height = h * width / w;
			d.setBounds(0, 0, width, height);
		} 
		return d;
	}
	private static Drawable mCleanDrawable = new ColorDrawable();
	public static Drawable getClear() {
		return mCleanDrawable;
	}

	public static Bitmap decodeUriAsBitmap(Context context, Uri uri){
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
		if (image != null) {
			int width = image.getWidth();
			int height = image.getHeight();
			if ((maxWidth != 0 && width > maxWidth) || (maxHeight != 0 && height > maxHeight)) {
				float scaleX = (float) maxWidth / width;
				float scaleY = (float) maxHeight / height;
				float scale;
				if (maxWidth < 1) scale = scaleY;
				else if (maxHeight < 1) scale = scaleX;
				else scale = Math.min(scaleX, scaleY);
				Matrix matrix = new Matrix();
				matrix.postScale(scale, scale);
				image = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
				return image;
			} else {
				return image;
			}
		} else {
			return image;
		}
	}

	public static Bitmap roundCornerBitmap(Bitmap bitmap, int radius) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, radius, radius, paint);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			canvas.drawBitmap(bitmap, src, rect, paint);
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}

	public static Bitmap createBlankBitmap(int width, int height, int color) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		TextPaint paint = new TextPaint();
		paint.setColor(color);
		canvas.drawRect(0, 0, width, height, paint);
		return bitmap;
	}

	public static Bitmap overlay(Bitmap back, Bitmap logo, int offsetX, int offsetY) {
		Bitmap newBitmap = Bitmap.createBitmap(back);
		Canvas canvas = new Canvas(newBitmap);
		Paint paint = new Paint();
		int w = back.getWidth();
		int h = back.getHeight();
		int w2 = logo.getWidth();
		int h2 = logo.getHeight();
		int left = (w - w2) / 2 + offsetX;
		int top = (h - h2) / 2 + offsetY;
		canvas.drawBitmap(logo, left, top, paint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newBitmap;
	}
}

package com.dylan.uiparts.button;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

public class IrregularButton extends ImageButton {

	public IrregularButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public IrregularButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
    public boolean onTouchEvent(MotionEvent event) {
		if (isTouchPointInView(event.getX(),event.getY()) || event.getAction() != MotionEvent.ACTION_DOWN) {
			return super.onTouchEvent(event);
		} else {
			return false;
		}
    }
	
	protected boolean isTouchPointInView(float localX, float localY) {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		draw(canvas);
		
	    int x = (int)localX;
	    int y = (int)localY;
	    if (x < 0 || x >= getWidth()) {
	        return false;
	    }
	    if (y < 0 || y >= getHeight()) {
	        return false;
	    }
	    int pixel = bitmap.getPixel(x,y);
	    if ((pixel & 0xff000000) != 0){
	        return true;
	    } else {
	        return false;
	    }
	}
}

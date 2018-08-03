package com.dylan.uiparts.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AutoAdjustWidthImageView extends ImageView {
	
	public AutoAdjustWidthImageView(Context context) {
		super(context);
		recalWithImage();
	}
    public AutoAdjustWidthImageView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        recalWithImage();
    } 
	public AutoAdjustWidthImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle); 
		recalWithImage(); 
	}

	private int imageWidth;  
    private int imageHeight;  
    public void recalcWithBackground() {
    	Drawable background = this.getBackground();  
        if (background == null) return;  
        Bitmap bitmap = ((BitmapDrawable)background).getBitmap();  
        imageWidth = bitmap.getWidth();  
        imageHeight = bitmap.getHeight();  
    }
    public void recalWithImage() {
    	Drawable image = this.getDrawable();
    	if (image == null)return;
        Bitmap bitmap = ((BitmapDrawable)image).getBitmap();  
        imageWidth = bitmap.getWidth();  
        imageHeight = bitmap.getHeight(); 
    }
  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		imageWidth = 0;
		imageHeight = 0;
    	if (imageWidth == 0 || imageHeight == 0) {
    		recalWithImage();
    	}
    	if (imageWidth == 0 || imageHeight == 0) {
    		recalcWithBackground();
    	}
    	if (imageWidth == 0 || imageHeight == 0) {
    		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	} else {
	        int height = MeasureSpec.getSize(heightMeasureSpec);  
	        int width = height  * imageWidth / imageHeight;  
	        this.setMeasuredDimension(width, height);  
    	}
    }  
}

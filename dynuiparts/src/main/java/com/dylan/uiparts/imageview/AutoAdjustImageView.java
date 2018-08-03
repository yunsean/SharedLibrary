package com.dylan.uiparts.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AutoAdjustImageView extends ImageView {
	
	public AutoAdjustImageView(Context context) {
		super(context);
		recalcWithBackground();
	}
    public AutoAdjustImageView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        recalcWithBackground();
    } 
	public AutoAdjustImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle); 
		recalcWithBackground(); 
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
    public void setBackgroundResource(int resId) {
    	super.setBackgroundResource(resId);
    	Drawable background = getResources().getDrawable(resId);
    	setBackgroundDrawable(background);
    }
    @SuppressWarnings("deprecation")
	public void setBackgroundDrawable(Drawable background) {
    	super.setBackgroundDrawable(background);
    	Bitmap bitmap = ((BitmapDrawable)background).getBitmap();
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();
        if (width != imageWidth || height != imageHeight) {
        	imageWidth = width;
        	imageHeight = height;
        	requestLayout();
        }
    }
    public void setImageResource(int resId) {
    	super.setImageResource(resId);
    	Drawable drawable = getResources().getDrawable(resId);
    	setImageDrawable(drawable);
    }
    public void setImageDrawable(Drawable drawable) {
    	super.setImageDrawable(drawable);
    	Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
    	int width = bitmap.getWidth();  
        int height = bitmap.getHeight();
        if (width != imageWidth || height != imageHeight) {
        	imageWidth = width;
        	imageHeight = height;
        	requestLayout();
        }
    }
    public void setImageBitmap(Bitmap bm) {
    	super.setImageBitmap(bm);
    	int width = bm.getWidth();  
        int height = bm.getHeight();
        if (width != imageWidth || height != imageHeight) {
        	imageWidth = width;
        	imageHeight = height;
        	requestLayout();
        }
    }
  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
    	if (imageWidth == 0 || imageHeight == 0) {
    		recalWithImage();
    	}
    	if (imageWidth == 0 || imageHeight == 0) {
    		recalcWithBackground();
    	}
    	if (imageWidth == 0 || imageHeight == 0) {
    		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	} else {
	        int width = MeasureSpec.getSize(widthMeasureSpec);  
	        int height = MeasureSpec.getSize(heightMeasureSpec);
	        if (getScaleType() == ScaleType.CENTER_INSIDE) {
	        	if (width * imageHeight / imageWidth < height) {
		        	height = width  * imageHeight / imageWidth; 
		        } else {
		        	width = height * imageWidth / imageHeight;
		        }
	        } else {
		        if (width * imageHeight / imageWidth > height) {
		        	height = width  * imageHeight / imageWidth; 
		        } else {
		        	width = height * imageWidth / imageHeight;
		        }
	        }
	        this.setMeasuredDimension(width, height);  
    	}
    }  
}

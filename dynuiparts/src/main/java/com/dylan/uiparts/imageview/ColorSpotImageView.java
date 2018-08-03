package com.dylan.uiparts.imageview;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ColorSpotImageView extends ImageView {
	 
    public ColorSpotImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }
    public ColorSpotImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }
    
    public void setSpotColor(int color) {
    	mSpotColor = color;
    	if (mPaint != null) {
    		mPaint.setColor(mSpotColor);
    	}
    	postInvalidate();
    }

	@SuppressLint("NewApi")
    protected void init(Context context, AttributeSet attrs, int defStyle) {
        if (Utility.isHoneycombOrLater()) {
        	setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcSeekbar, defStyle, 0);
		mSpotColor = a.getColor(R.styleable.ColorSpotImageView_csiv_spotColor, 0xffffffff);
		a.recycle();
		
        mFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setColor(mSpotColor);
    }

	private int mSpotColor = 0xffffffff;
	private Path mPath = null;
	private PaintFlagsDrawFilter mFilter = null;
    private Paint mPaint = null;    
	@Override
    protected void onDraw(Canvas canvas) {
		float h = getMeasuredHeight();
        float w = getMeasuredWidth();
    	if (mPath == null) {
    		mPath = new Path();
            mPath.addCircle(w / 2, h / 2, w / 2, Path.Direction.CW); 
            mPath.close();
    	}
    	canvas.drawCircle(w / 2.0f, h / 2.0f,  Math.min(w / 2.0f, h / 2.0f) - 1.0f, mPaint);
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.setDrawFilter(mFilter);
        canvas.clipPath(mPath, Region.Op.REPLACE);
        canvas.setDrawFilter(mFilter);
        canvas.drawColor(mSpotColor);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }
}

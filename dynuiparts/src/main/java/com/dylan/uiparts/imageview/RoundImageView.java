package com.dylan.uiparts.imageview;

import com.dylan.common.utils.Utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Region;

public class RoundImageView extends ImageView {
	 
	private Path mPath = null;
	private PaintFlagsDrawFilter mFilter = null;
	private Paint mPaint = null;
    public RoundImageView(Context context) {
        super(context);
        init();
    }
    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

	@SuppressLint("NewApi")
    protected void init() {
        if (Utility.isHoneycombOrLater()) {
        		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
 
	@Override
    protected void onDraw(Canvas canvas) {  
        int w = this.getWidth();
        int h = this.getHeight();
	    	if (mPath == null) {
	    		mPaint = new Paint();
	    		mPaint.setAntiAlias(true);
	    		mPaint.setFilterBitmap(true);
	    		mPaint.setColor(Color.WHITE);
	    		mPath = new Path();
            mPath.addCircle(w / 2, h / 2, w / 2, Path.Direction.CW); 
            mFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	    	}
	    	canvas.drawCircle(w / 2.0f, h / 2.0f, Math.min(w / 2.0f, h / 2.0f), mPaint);
	    	int saveCount = canvas.getSaveCount();
	    	canvas.save();
	    	canvas.setDrawFilter(mFilter);
	    	canvas.clipPath(mPath, Region.Op.REPLACE);
	    	canvas.drawColor(Color.WHITE);
	    	super.onDraw(canvas);
	    	canvas.restoreToCount(saveCount);
    }
}

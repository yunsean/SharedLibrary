package com.dylan.uiparts.imageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.dylan.uiparts.R;

public class RoundCornerImageView extends AppCompatImageView {
    
    private Paint paint;
    private Paint paintBorder;
    private Bitmap mSrcBitmap;
    private Drawable mPlaceHolder;
    private int mRadius;
    private boolean mIsCircle;
    private int mBackground;

    public RoundCornerImageView(final Context context) {
        this(context, null);
    }
    public RoundCornerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public RoundCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView, defStyle, 0);
        mRadius = (int) ta.getDimension(R.styleable.RoundCornerImageView_rciv_radius, 0);
        mIsCircle = ta.getBoolean(R.styleable.RoundCornerImageView_rciv_circle, false);
        mPlaceHolder = ta.getDrawable(R.styleable.RoundCornerImageView_rciv_placeHolder);
        mBackground = ta.getColor(R.styleable.RoundCornerImageView_rciv_background, 0x00000000);
        int srcResource = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", 0);
        if (srcResource != 0) mSrcBitmap = BitmapFactory.decodeResource(getResources(), srcResource);
        ta.recycle();
        paint = new Paint();
        paint.setAntiAlias(true);
        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);
    }

    public void setRadius(int radius) {
        this.mRadius = radius;
    }
    public void setCircle(boolean circle) {
        this.mIsCircle = circle;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int width = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        int height = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
        Bitmap image = drawableToBitmap(getDrawable());
        ScaleType scaleType = getScaleType();
        if (image == null) {
            image = drawableToBitmap(mPlaceHolder);
            scaleType = ScaleType.CENTER_INSIDE;
        }
        if (image == null) return;
        image = drawableToBitmap(image, width, height, scaleType);
        if (mIsCircle) canvas.drawBitmap(createCircleImage(image, width, height), getPaddingLeft(), getPaddingTop(), null);
        else canvas.drawBitmap(createRoundImage(image, width, height), getPaddingLeft(), getPaddingTop(), null);
    }

    private Bitmap createRoundImage(Bitmap source, int width, int height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rect, mRadius, mRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    private Bitmap createCircleImage(Bitmap source, int width, int height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.drawCircle(width / 2, height / 2, Math.min(width, height) / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, (width - source.getWidth()) / 2, (height - source.getHeight()) / 2, paint);
        return target;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private Paint mPaint = null;
    private Bitmap drawableToBitmap(Bitmap bitmap, int newWidth, int newHeight, ScaleType scaleType) {
        Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Rect src;
        Rect dst;
        if (scaleType == ScaleType.CENTER && (width > newWidth || height > newHeight)) {
            scaleType = ScaleType.CENTER_INSIDE;
        }
        if (scaleType == ScaleType.CENTER_CROP) {
            dst = new Rect(0, 0, newWidth, newHeight);
            if (width * newHeight / newWidth > height) {    //期望高比实际高大，则按照高度计算
                int validWidth = height * newWidth / newHeight;
                int offsetX = (width - validWidth) / 2;
                src = new Rect(offsetX, 0, offsetX + validWidth, height);
            } else {
                int validHeight = width * newHeight / newWidth;
                int offsetY = (height - validHeight) / 2;
                src = new Rect(0, offsetY, width, offsetY + validHeight);
            }
        } else if (scaleType == ScaleType.CENTER_INSIDE) {
            src = new Rect(0, 0, width, height);
            if (newHeight * width / height > height) {      //期望高比实际高小，则按照高度计算
                int validHeight = width * newHeight / newWidth;
                int offsetY = (newHeight - validHeight) / 2;
                dst = new Rect(0, offsetY, newWidth, offsetY + validHeight);
            } else {
                int validWidth = newHeight * width / height;
                int offsetX = (newWidth - validWidth) / 2;
                dst = new Rect(offsetX, 0, offsetX + validWidth, newHeight);
            }
        } else if (scaleType == ScaleType.CENTER) {
            src = new Rect(0, 0, width, height);
            int offsetX = (newWidth - width) / 2;
            int offsetY = (newHeight - height) / 2;
            dst = new Rect(offsetX, offsetY, offsetX + width, offsetY + height);
        } else {
            src = new Rect(0, 0, width, height);
            dst = new Rect(0, 0, newWidth, newHeight);
        }
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }
        if (mBackground != 0) {
            mPaint.setColor(mBackground);
            canvas.drawRect(0, 0, newWidth, newHeight, mPaint);
        }
        canvas.drawBitmap(bitmap, src, dst, mPaint);
        return newBitmap;
    }
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            if (mSrcBitmap != null) {
                return mSrcBitmap;
            } else {
                return null;
            }
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}

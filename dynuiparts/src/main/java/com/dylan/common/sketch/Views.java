package com.dylan.common.sketch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dylan.uiparts.R;

public class Views {

    @SuppressLint("InflateParams")
    public static void showTopbar(Activity context, String tips, int bgcolor, int txcolor, int duration) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_topbar, null);
        TextView tv = (TextView) layout.findViewById(R.id.infos);
        if (tips != null) {
            tv.setText(tips);
        }
        ViewGroup.LayoutParams layoutParams = tv.getLayoutParams();
        layoutParams.width = context.getWindow().getDecorView().getWidth();
        tv.requestLayout();
        tv.setBackgroundColor(bgcolor);
        tv.setTextColor(txcolor);

        Toast toast = new Toast(context);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.setDuration(duration);
        toast.show();
    }

    public static void errorTopbar(Activity context, String tips) {
        showTopbar(context, tips, 0xeec64343, 0xffffffff, Toast.LENGTH_SHORT);
    }

    public static void warningTopbar(Activity context, String tips) {
        showTopbar(context, tips, 0xeeeedd32, 0xffffffff, Toast.LENGTH_SHORT);
    }

    public static void infoTopbar(Activity context, String tips) {
        showTopbar(context, tips, 0xee18b4ed, 0xffffffff, Toast.LENGTH_SHORT);
    }

    public static Bitmap captureActivity(Activity activity) {
        try {
            Bitmap bitmap = captureView(activity.getWindow().getDecorView());
            Rect frame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            bitmap = Bitmap.createBitmap(bitmap, 0, statusBarHeight, frame.width(), frame.height());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Bitmap captureView(View v) {
        v.clearFocus();
        v.setPressed(false);
        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }

    public static int getWidth(Activity activity, View view) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        view.measure(size.x, size.y);
        return view.getMeasuredWidth();
    }
    public static int getHeight(Activity activity, View view) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        view.measure(size.x, size.y);
        return view.getMeasuredHeight();
    }
    public static Point getSize(Activity activity, View view) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        view.measure(size.x, size.y);
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        return new Point(width, height);
    }

    public static int getWidth(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return view.getMeasuredWidth();
    }
    public static int getHeight(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return view.getMeasuredHeight();
    }
    public static Point getSize(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        return new Point(width, height);
    }
}

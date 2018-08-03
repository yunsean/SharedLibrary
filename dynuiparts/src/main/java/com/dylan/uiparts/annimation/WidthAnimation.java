package com.dylan.uiparts.annimation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class WidthAnimation extends Animation {
    protected final int originalWidth;
    protected final View view;
    protected float perValue;

    public WidthAnimation(View view, int toWidth) {
        this(view, view.getWidth(), toWidth);
    }
    public WidthAnimation(View view, int fromWidth, int toWidth) {
        this.view = view;
        this.originalWidth = fromWidth;
        this.perValue = (toWidth - fromWidth);
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.getLayoutParams().width = (int) (originalWidth + perValue * interpolatedTime);
        view.requestLayout();
    }
    @Override
    public boolean willChangeBounds() {
        return true;
    }
}

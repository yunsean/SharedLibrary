package com.dylan.common.sketch;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

import com.dylan.uiparts.annimation.ExpandAnimation;
import com.dylan.uiparts.annimation.HeightAnimation;
import com.dylan.uiparts.annimation.MarginAnimation;
import com.dylan.uiparts.annimation.SizeAnimation;
import com.dylan.uiparts.annimation.WidthAnimation;

public class Animations {
    public static AnimationUtils HeightAnimation(View view, int toHeight) {
        HeightAnimation animation = new HeightAnimation(view, toHeight);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils HeightAnimation(View view, int fromHeight, int toHeight) {
        HeightAnimation animation = new HeightAnimation(view, fromHeight, toHeight);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }

    public static AnimationUtils RotateAnimation(View view, float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue) {
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotXType, pivotXValue, pivotYType, pivotYValue);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils RotateAnimation(View view, float fromDegrees, float toDegrees, float pivotX, float pivotY) {
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils RotateAnimation(View view, float fromDegrees, float toDegrees) {
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }

    public static AnimationUtils WidthAnimation(View view, int toWidth) {
        WidthAnimation animation = new WidthAnimation(view, toWidth);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils WidthAnimation(View view, int fromWidth, int toWidth) {
        WidthAnimation animation = new WidthAnimation(view, fromWidth, toWidth);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }

    public static AnimationUtils ExpandAnimation(View view) {
        ExpandAnimation animation = new ExpandAnimation(view);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils ExpandAnimation(View view, int toHeight) {
        ExpandAnimation animation = new ExpandAnimation(view, toHeight);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }

    public static AnimationUtils SizeAnimation(View view, SizeAnimation.Mode mode, int endSize) {
        SizeAnimation animation = new SizeAnimation(view, mode, endSize);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils SizeAnimation(View view, SizeAnimation.Mode mode, int beginSize, int endSize) {
        SizeAnimation animation = new SizeAnimation(view, mode, beginSize, endSize);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils SizeAnimation(View view, SizeAnimation.Size endSize) {
        SizeAnimation animation = new SizeAnimation(view, endSize);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils SizeAnimation(View view, SizeAnimation.Size beginSize, SizeAnimation.Size endSize) {
        SizeAnimation animation = new SizeAnimation(view, beginSize, endSize);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }

    public static AnimationUtils MarginAnimation(View view, int endBottomMargin) {
        MarginAnimation animation = new MarginAnimation(view, endBottomMargin);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils MarginAnimation(View view, MarginAnimation.Margin margin, int endMargin) {
        MarginAnimation animation = new MarginAnimation(view, margin, endMargin);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils MarginAnimation(View view, MarginAnimation.Margin margin, int beginMargin, int endMargin) {
        MarginAnimation animation = new MarginAnimation(view, margin, beginMargin, endMargin);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }

    public static AnimationUtils AlphaAnimation(View view, float toAlpha) {
        Animation animation = new AlphaAnimation(view.getAlpha(), toAlpha);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils AlphaAnimation(View view, float fromAlpha, float toAlpha) {
        Animation animation = new AlphaAnimation(fromAlpha, toAlpha);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }

    public static AnimationUtils ScaleAnimation(View view, float fromX, float toX, float fromY, float toY, float pivotXValue, float pivotYValue) {
        Animation animation = new ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, pivotXValue, Animation.RELATIVE_TO_SELF, pivotYValue);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils ScaleAnimationY(View view, float fromY, float toY, float pivotYValue) {
        Animation animation = new ScaleAnimation(1.f, 1.f, fromY, toY, Animation.RELATIVE_TO_SELF, 1.f, Animation.RELATIVE_TO_SELF, pivotYValue);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
    public static AnimationUtils ScaleAnimationX(View view, float fromX, float toX, float pivotXValue) {
        Animation animation = new ScaleAnimation(fromX, toX, 1.f, 1.f, Animation.RELATIVE_TO_SELF, pivotXValue, Animation.RELATIVE_TO_SELF, 1.f);
        view.clearAnimation();
        view.setAnimation(animation);
        return new AnimationUtils(animation);
    }
}

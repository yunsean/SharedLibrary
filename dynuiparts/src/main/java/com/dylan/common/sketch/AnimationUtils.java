package com.dylan.common.sketch;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.InterpolatorRes;
import android.view.animation.Animation;
import android.view.animation.Interpolator;

import com.dylan.uiparts.annimation.AnimationListener;

public class AnimationUtils {
    public Animation animation() {
        return this.animation;
    }
    public AnimationUtils reset() {
        animation.reset();
        return this;
    }
    public AnimationUtils start() {
        animation.start();
        return this;
    }
    public AnimationUtils startNow() {
        animation.startNow();
        return this;
    }
    public AnimationUtils cancel() {
        animation.cancel();
        return this;
    }
    public AnimationUtils interpolator(Context context, @InterpolatorRes int resID) {
        animation.setInterpolator(context, resID);
        return this;
    }
    public AnimationUtils interpolator(Interpolator i) {
        animation.setInterpolator(i);
        return this;
    }
    public AnimationUtils startOffset(long startOffset) {
        animation.setStartOffset(startOffset);
        return this;
    }
    public AnimationUtils duration(long durationMillis) {
        animation.setDuration(durationMillis);
        return this;
    }
    public AnimationUtils repeatMode(int repeatMode) {
        animation.setRepeatMode(repeatMode);
        return this;
    }
    public AnimationUtils repeatCount(int repeatCount) {
        animation.setRepeatCount(repeatCount);
        return this;
    }
    public AnimationUtils fillEnabled(boolean fillEnabled) {
        animation.setFillEnabled(fillEnabled);
        return this;
    }
    public AnimationUtils fillBefore(boolean fillBefore) {
        animation.setFillBefore(fillBefore);
        return this;
    }
    public AnimationUtils fillAfter(boolean fillAfter) {
        animation.setFillAfter(fillAfter);
        return this;
    }
    public AnimationUtils animationListener(Animation.AnimationListener listener) {
        animation.setAnimationListener(listener);
        return this;
    }
    public AnimationUtils animationListener(AnimationListener.AnimationEndListener end) {
        animation.setAnimationListener(new AnimationListener(end));
        return this;
    }
    public AnimationUtils animationListener(AnimationListener.AnimationStartListener start, AnimationListener.AnimationEndListener end, AnimationListener.AnimationRepeatListener repeat) {
        animation.setAnimationListener(new AnimationListener(start, end, repeat));
        return this;
    }
    public AnimationUtils backgroundColor(@ColorInt int bg) {
        animation.setBackgroundColor(bg);
        return this;
    }

    private android.view.animation.Animation animation = null;
    AnimationUtils(Animation animation) {
        this.animation = animation;
    }
}

package com.dylan.uiparts.annimation;

import android.view.animation.Animation;

public class AnimationListener implements android.view.animation.Animation.AnimationListener {

    public interface AnimationStartListener {
        public void onAnimationStart(Animation animation);
    }
    public interface AnimationEndListener {
        public void onAnimationEnd(Animation animation);
    }
    public interface AnimationRepeatListener {
        public void onAnimationRepeat(Animation animation);
    }

    public AnimationListener(AnimationEndListener onAnimationEnd) {
        this.onAnimationEnd = onAnimationEnd;
    }
    public AnimationListener(AnimationStartListener onAnimationStart, AnimationEndListener onAnimationEnd, AnimationRepeatListener onAnimationRepeat) {
        this.onAnimationStart = onAnimationStart;
        this.onAnimationEnd = onAnimationEnd;
        this.onAnimationRepeat = onAnimationRepeat;
    }

    private AnimationStartListener onAnimationStart = null;
    private AnimationEndListener onAnimationEnd = null;
    private AnimationRepeatListener onAnimationRepeat = null;
    @Override
    public void onAnimationStart(Animation animation) {
        if (onAnimationStart != null)onAnimationStart.onAnimationStart(animation);
    }
    @Override
    public void onAnimationEnd(Animation animation) {
        if (onAnimationEnd != null)onAnimationEnd.onAnimationEnd(animation);
    }
    @Override
    public void onAnimationRepeat(Animation animation) {
        if (onAnimationRepeat != null)onAnimationRepeat.onAnimationRepeat(animation);
    }
}

package com.dylan.dyn3rdparts.ratingbar;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.dylan.dyn3rdparts.R;

public class ScaleRatingBar extends BaseRatingBar {

    private static Handler sUiHandler = new Handler();

    public ScaleRatingBar(Context context) {
        super(context);
    }
    public ScaleRatingBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public ScaleRatingBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void emptyRatingBar() {
        sUiHandler.removeCallbacksAndMessages(null);
        int delay = 0;
        for (final PartialView view : mPartialViews) {
            sUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setEmpty();
                }
            }, delay += 5);
        }
    }

    @Override
    protected void fillRatingBar(final float rating, boolean animated) {
        sUiHandler.removeCallbacksAndMessages(null);
        int delay = 0;
        for (final PartialView partialView : mPartialViews) {
            final int    ratingViewId   = partialView.getId();
            final double maxIntOfRating = Math.ceil(rating);
            if (ratingViewId > maxIntOfRating) {
                partialView.setEmpty();
                continue;
            }
            if (animated) {
                sUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ratingViewId == maxIntOfRating) {
                            partialView.setPartialFilled(rating);
                        } else {
                            partialView.setFilled();
                        }
                        if (ratingViewId == rating) {
                            Animation scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.ratingbar_scale_up);
                            Animation scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.ratingbar_scale_down);
                            partialView.startAnimation(scaleUp);
                            partialView.startAnimation(scaleDown);
                        }

                    }
                }, delay += 15);
            } else {
                if (ratingViewId == maxIntOfRating) {
                    partialView.setPartialFilled(rating);
                } else {
                    partialView.setFilled();
                }
            }
        }
    }
}


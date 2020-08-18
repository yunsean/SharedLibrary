package com.dylan.dyn3rdparts.ratingbar;

import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;

interface SimpleRatingBar {

    void setNumStars(int numStars);
    int getNumStars();
    void setRating(float rating);
    float getRating();
    void setStarPadding(int ratingPadding);
    int getStarPadding();
    void setEmptyDrawable(Drawable drawable);
    void setEmptyDrawableRes(@DrawableRes int res);
    void setFilledDrawable(Drawable drawable);
    void setFilledDrawableRes(@DrawableRes int res);
}

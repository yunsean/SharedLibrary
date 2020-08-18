package com.dylan.uiparts.viewpager.transforms;

import androidx.viewpager.widget.ViewPager;
import android.view.View;

public class StackTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        page.setTranslationX(page.getWidth() * -position);
        page.setTranslationY(position < 0 ? position * page.getHeight() : 0f);
    }
}

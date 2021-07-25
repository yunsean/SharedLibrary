package com.yunsean.dynkotlins.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.dylan.uiparts.viewpager.StaticViewPager

class AutoSizeViewPager : StaticViewPager {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val view = getChildAt(currentItem)
        view?.measure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measureHeight(heightMeasureSpec, view))
    }

    private fun measureHeight(measureSpec: Int, view: View?): Int {
        var result = 0
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            if (view != null) result = view.measuredHeight
            if (specMode == MeasureSpec.AT_MOST) result = Math.min(result, specSize)
        }
        return result
    }
}
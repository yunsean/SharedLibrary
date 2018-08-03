package com.dylan.dyn3rdparts.imageslider.sliderTypes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.dylan.dyn3rdparts.R;

public class DefaultSliderView extends BaseSliderView{

    public DefaultSliderView(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.imageslider_render_type_default, null);
        ImageView target = (ImageView)v.findViewById(R.id.slider_image);
        bindEventAndShow(v, target);
        return v;
    }
}

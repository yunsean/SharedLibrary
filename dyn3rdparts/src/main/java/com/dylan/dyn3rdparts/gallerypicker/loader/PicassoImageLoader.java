package com.dylan.dyn3rdparts.gallerypicker.loader;

import android.app.Activity;
import android.widget.ImageView;

import com.dylan.dyn3rdparts.R;
import com.squareup.picasso.Picasso;

public class PicassoImageLoader implements ImageLoader {
    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        Picasso.with(activity)
                .load("file://" + path)
                .resize(width, height)
                .centerInside()
                .placeholder(R.drawable.gallerypicker_default_image)
                .error(R.drawable.gallerypicker_default_image)
                .into(imageView);
    }
    @Override
    public void clearMemoryCache() {

    }
}
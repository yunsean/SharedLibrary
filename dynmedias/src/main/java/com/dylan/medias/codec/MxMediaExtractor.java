package com.dylan.medias.codec;

import android.content.Context;
import android.media.MediaExtractor;

public class MxMediaExtractor {

    private MediaExtractor mMediaExtractor = null;
    private MxMediaExtractor() {
        mMediaExtractor = new MediaExtractor();
    }
    public static MxMediaExtractor with(Context context) {
        return new MxMediaExtractor();
    }
    public MxMediaExtractor open(String filepath) {
        return this;
    }
}

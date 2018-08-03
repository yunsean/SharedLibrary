package com.dylan.live.playback;

public interface Segment {

    long getId();
    long getBeginUtc();
    long getEndUtc();
    long getAudioOffset();
    long getVideoOffset();
    String getFilename();
}

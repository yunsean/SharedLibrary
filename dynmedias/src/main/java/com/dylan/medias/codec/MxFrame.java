package com.dylan.medias.codec;

import java.nio.ByteBuffer;

public interface MxFrame {
    int getFormat();
    int getWidth();
    int getHeight();
    long getTimeStamp();
    ByteBuffer getDirectBuffer();
    void close();
}

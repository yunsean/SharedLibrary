package com.dylan.live.playback;

import java.util.List;

public interface SegmentManager {
    List<Segment> querySegments(long begin, long end);
    Segment queryOldestSegment();
    Segment queryNewestSegment();
    List<Segment> queryAllSegment();
    void removeSegment(long id);
    List<Segment> querySegmentAfterTime(long begin);
    List<Segment> querySegmentAfterId(long id);
}

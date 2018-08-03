package com.dylan.common.data.tuple;

public class Triple<A, B, C> extends Tuple<A, B> {
    public final C third;
    public Triple(A first, B second, C third) {
        super(first, second);
        this.third = third;
    }
}

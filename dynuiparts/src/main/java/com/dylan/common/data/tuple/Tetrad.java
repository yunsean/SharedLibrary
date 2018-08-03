package com.dylan.common.data.tuple;

public class Tetrad<A, B, C, D> extends Triple<A, B, C> {
    public final D fourth;
    public Tetrad(A first, B second, C third, D fourth) {
        super(first, second, third);
        this.fourth = fourth;
    }
}

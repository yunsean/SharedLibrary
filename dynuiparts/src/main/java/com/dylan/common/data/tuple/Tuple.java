package com.dylan.common.data.tuple;

public class Tuple<A, B> {
    final public A first;
    final public B second;
    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }
}

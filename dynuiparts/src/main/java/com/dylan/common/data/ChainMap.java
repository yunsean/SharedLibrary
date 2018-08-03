package com.dylan.common.data;

import java.util.HashMap;

public class ChainMap<T, V> extends HashMap<T, V> {
    public ChainMap() {
        super();
    }
    public ChainMap(T key, V value) {
        super();
        if (key != null && value != null) {
            super.put(key, value);
        }
    }
    public ChainMap<T, V> set(T key, V value) {
        super.put(key, value);
        return this;
    }
    public ChainMap<T, V> sets(Object...keyValues) {
        if (keyValues == null)return this;
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            if (keyValues[i] == null) break;
            if (keyValues[i + 1] == null) break;
            super.put((T)keyValues[i], (V)keyValues[i + 1]);
        }
        return this;
    }
}

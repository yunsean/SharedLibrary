package com.dylan.common.data;

import java.io.Serializable;

public class SerializableIt<T> implements Serializable {

    private T object = null;

    public SerializableIt(T object) {
        this.object = object;
    }
    public T getObject() {
        return object;
    }
    public void setObject(T object) {
        this.object = object;
    }
}

package com.dylan.common.data;

import java.io.Serializable;
import java.util.Map;

public class SerializableMap<K, V> implements Serializable {
    private Map<K, V> map = null;
    public SerializableMap(Map<K, V> map) {
        this.map = map;
    }

    public Map<K, V> getMap() {
        return map;
    }
    public void setMap(Map<K, V> map) {
        this.map = map;
    }
}

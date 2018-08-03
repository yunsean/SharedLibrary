package com.dylan.common.data;

import java.util.List;

public class ArrayUtil {
    public static boolean contain(long[] array, long instance) {
        if (array == null) return false;
        for (long obj : array) {
            if (obj == instance) return true;
        }
        return false;
    }
    public static boolean contain(int[] array, int instance) {
        if (array == null) return false;
        for (int obj : array) {
            if (obj == instance) return true;
        }
        return false;
    }
    public static <T> boolean contain(T[] array, T instance) {
        if (array == null) {
            return false;
        }
        for (T obj : array) {
            if (obj == instance) {
                return true;
            }
        }
        return false;
    }
    public static <T> boolean containAny(T[] array, T[] instance) {
        if (array == null) {
            return false;
        }
        for (T obj : array) {
            for (T ins : instance) {
                if (obj == ins) {
                    return true;
                }
            }
        }
        return false;
    }
    public static <T> boolean containAll(T[] array, T[] instance) {
        if (array == null) {
            return false;
        }
        for (T ins : instance) {
            boolean found = false;
            for (T obj : array) {
                if (obj == ins) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public static long[] toArray(List<Long> list) {
        long[] values = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i);
        }
        return values;
    }
}

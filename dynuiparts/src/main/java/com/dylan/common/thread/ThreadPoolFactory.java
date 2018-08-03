package com.dylan.common.thread;

public class ThreadPoolFactory {

    static ThreadPoolProxy normalPool;
    static ThreadPoolProxy downloadPool;

    public static ThreadPoolProxy getNormalPool() {
        if (normalPool == null) {
            synchronized (ThreadPoolFactory.class) {
                if (normalPool == null) {
                    normalPool = new ThreadPoolProxy(5, 5, 3000);
                }
            }
        }
        return normalPool;
    }
    public static ThreadPoolProxy getDownLoadPool() {
        if (downloadPool == null) {
            synchronized (ThreadPoolFactory.class) {
                if (downloadPool == null) {
                    downloadPool = new ThreadPoolProxy(3, 3, 3000);
                }
            }
        }
        return downloadPool;
    }
}

package com.dylan.common.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolProxy {
    ThreadPoolExecutor threadPoolExecutor;
    int corePoolSize;
    int maximumPoolSize;
    long keepAliveTime;

    public ThreadPoolProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        super();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
    }
    private ThreadPoolExecutor initThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            synchronized (ThreadPoolProxy.class) {
                if (threadPoolExecutor == null) {
                    TimeUnit unit = TimeUnit.MILLISECONDS;
                    BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>();
                    ThreadFactory threadFactory = Executors.defaultThreadFactory();
                    RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
                    threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
                }
            }
        }
        return threadPoolExecutor;
    }

    public void execute(Runnable task) {
        initThreadPoolExecutor();
        threadPoolExecutor.execute(task);
    }
    public Future<?> submit(Runnable task) {
        initThreadPoolExecutor();
        return threadPoolExecutor.submit(task);
    }
    public void remove(Runnable task) {
        initThreadPoolExecutor();
        threadPoolExecutor.remove(task);
    }
}

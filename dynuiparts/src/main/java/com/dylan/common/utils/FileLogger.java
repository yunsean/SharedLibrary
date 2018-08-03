package com.dylan.common.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileLogger {

    private static volatile FileLogger instance = null;
    private SimpleDateFormat mFormat = null;
    private WriteThread mThread = null;

    private FileLogger() {
        mThread = new WriteThread();
        mFormat = new SimpleDateFormat("MM-dd HH:mm:ss:SS");
        mThread.start();
    }

    public static FileLogger getInstance() {
        if (instance == null) {
            synchronized (FileLogger.class) {
                if (instance == null) {
                    instance = new FileLogger();
                }
            }
        }
        return instance;
    }

    public static void e(String tag, String str, Exception e) {
        Log.e(tag, str, e);
        String log = "";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log = str + "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            log = str + " fail to print Exception";
        }
        FileLogger.getInstance().Log(tag, log);
    }
    public static void e(String tag, String str) {
        Log.e(tag, str);
        FileLogger.getInstance().Log(tag, str);
    }
    private synchronized void Log(String tag, String str) {
        String time = mFormat.format(new Date());
        mThread.enqueue(time + " " + tag + " " + str);
    }

    public class WriteThread extends Thread {
        private boolean isRunning = false;
        private String filePath = null;
        private final Object lock = new Object();
        private ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue<String>();

        WriteThread() {
            String sdcard = getPath();
            if (sdcard != null) filePath = sdcard + "/logger.txt";
            isRunning = true;
        }
        public String getPath() {
            return exist() ? Environment.getExternalStorageDirectory().toString() : null;
        }
        boolean exist() {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        }
        void enqueue(String str) {
            mQueue.add(str);
            if (isRunning() == false) {
                awake();
            }
        }
        boolean isRunning() {
            return isRunning;
        }
        void awake() {
            synchronized (lock) {
                lock.notify();
            }
        }
        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    isRunning = true;
                    while (!mQueue.isEmpty()) {
                        try {
                            recordStringLog(mQueue.poll());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isRunning = false;
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        void recordStringLog(String text) {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileWriter filerWriter = new FileWriter(file, true);
                BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                bufWriter.write(text);
                bufWriter.newLine();
                bufWriter.close();
                filerWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public boolean isExitLogFile() {
            File file = new File(filePath);
            if (file.exists() && file.length() > 3) {
                return true;
            } else {
                return false;
            }
        }
        public void deleteLogFile() {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

}
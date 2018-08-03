package com.dylan.common.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.dylan.common.data.DateUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class LogcatHelper {
    private static LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper logDumper = null;
    private int pid;
    public void init(Context context) {
        if (Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED)) {
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "log";
        } else {
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + "log";
        }
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) file.mkdirs();
    }
    public static LogcatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LogcatHelper.class) {
                if (INSTANCE == null) INSTANCE = new LogcatHelper(context);
            }
        }
        return INSTANCE;
    }
    private LogcatHelper(Context context) {
        init(context);
        pid = android.os.Process.myPid();
    }
    private String filePrefix = "log-";
    public void start(String filePrefix) {
        this.filePrefix = filePrefix;
        start();
    }
    public void start() {
        if (logDumper == null) logDumper = new LogDumper(String.valueOf(pid), PATH_LOGCAT);
        Log.i("path", PATH_LOGCAT);
        logDumper.start();
    }
    public void stop() {
        if (logDumper != null) {
            logDumper.stopLogs();
            logDumper = null;
        }
    }

    private class LogDumper extends Thread {
        private Process logcatProc;
        private BufferedReader reader = null;
        private boolean running = true;
        String cmds = null;
        private String pid;
        private FileOutputStream out = null;
        public LogDumper(String pid, String dir) {
            this.pid = pid;
            try {
                out = new FileOutputStream(new File(dir, filePrefix + DateUtil.formatDate(new Date(), "yyyyMMddHHmmss") + ".log"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            cmds = "logcat *:e *:i | grep \"(" + this.pid + ")\"";
        }
        public void stopLogs() {
            running = false;
        }
        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                reader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line = null;
                while (running && (line = reader.readLine()) != null) {
                    if (!running) break;
                    if (line.length() == 0) continue;
                    if (out != null && line.contains(pid)) {
                        out.write((System.currentTimeMillis() + "  " + line + "\n").getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (reader != null) {
                    try {
                        reader.close();
                        reader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }
}
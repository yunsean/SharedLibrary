package com.dylan.common.utils;

import com.dylan.common.application.Application;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPHelper {
	
	public static final String TAG = "SharedPreferences";

    public static SharedPreferences sp() {
        return Application.context().getSharedPreferences("Default", Context.MODE_MULTI_PROCESS);
    }

    public static boolean putStringValue(String key, String value) {
        SharedPreferences storage = sp();
        Editor editor = storage.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static boolean putLongValue(String key, long value) {
        SharedPreferences storage = sp();
        Editor editor = storage.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    public static String getStringValue(String key) {
        SharedPreferences storage = sp();
        return storage.getString(key, null);
    }

    public static String getStringValue2(String key, String defaultString) {
        SharedPreferences storage = sp();
        return storage.getString(key, defaultString);
    }

    public static long getLongValue(String key) {
        SharedPreferences storage = sp();
        return storage.getLong(key, 0);
    }

    public static long getLongValue(String key, long defValue) {
        SharedPreferences storage = sp();
        return storage.getLong(key, defValue);
    }

    public static boolean removeValue(String key) {
        SharedPreferences storage = sp();
        Editor editor = storage.edit();
        editor.remove(key);
        return editor.commit();
    }

    public static boolean getBooleanValue(String key) {
        SharedPreferences storage = sp();
        return storage.getBoolean(key, false);
    }

    public static boolean getBooleanValue(String key, boolean defaultValue) {
        SharedPreferences storage = sp();
        return storage.getBoolean(key, defaultValue);
    }

    public static boolean putBooleanValue(String key, boolean value) {
        SharedPreferences storage = sp();
        Editor editor = storage.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean putIntValue(String key, int value) {
        SharedPreferences storage = sp();
        Editor editor = storage.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int getIntValue(String key) {
        SharedPreferences storage = sp();
        return storage.getInt(key, -1);
    }
}

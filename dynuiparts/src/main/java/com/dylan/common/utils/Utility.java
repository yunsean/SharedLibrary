package com.dylan.common.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.dylan.common.data.StrUtil;
import com.dylan.common.digest.MD5;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Utility {
    public static int getAndroidSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static boolean isFroyoOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean isGingerbreadOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean isHoneycombOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isICSOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean isJellyBeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isJellyBeanMR1OrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean isKitkatOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isLollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isMOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isEqualOrAfterVersion(int version) {
        return Build.VERSION.SDK_INT >= version;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getPixelDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.densityDpi;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void showSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), InputMethodManager.SHOW_FORCED);
        }
    }

    public static void toggleSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0, 0);
        }
    }

    public static void showSoftKeyPicker(Activity activity) {
        ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
    }

    public static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int screenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int screenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getNumber(TextView v, int def) {
        try {
            return Integer.parseInt(v.getText().toString());
        } catch (Exception e) {
            v.setText(String.valueOf(def));
            return def;
        }
    }

    public static int setNumber(TextView v, int value, int min, int max) {
        if (value < min)
            value = min;
        if (value > max)
            value = max;
        v.setText(String.valueOf(value));
        return value;
    }

    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static void jumpNetworkSetting(Context context) {
        Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
        context.startActivity(intent);
    }

    public static String getImei(Context context) {
        String imei = null;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
            imei = tm.getDeviceId();
        } catch (Exception ex) {
        }
        if (imei == null || imei.length() == 0) {
            imei = "";
        }
        return imei.trim();
    }

    public static String getSeriNum(Context context) {
        String seriNum = null;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
            seriNum = tm.getSimSerialNumber();
        } catch (Exception ex) {
        }
        if (seriNum == null || seriNum.length() == 0) {
            seriNum = "";
        }
        return seriNum.trim();
    }

    public static String getImsi(Context context) {
        String imsi = null;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
            imsi = tm.getSubscriberId();
        } catch (Exception ex) {
        }
        if (imsi == null || imsi.length() == 0) {
            imsi = generateImei();
        }
        return imsi;
    }

    public static String getFakeId(Context context) {
        String[] values = readPreference(context, new String[]{"fakeid"});
        if (StrUtil.isBlank(values[0])) {
            values[0] = getImsi(context);
            savePreference(context, new String[]{"fakeid"}, values);
        }
        return values[0];
    }

    public static String generateImei() {
        StringBuffer imei = new StringBuffer();
        long time = System.currentTimeMillis();
        String currentTime = Long.toString(time);
        imei.append(currentTime.substring(currentTime.length() - 5));
        StringBuffer model = new StringBuffer();
        model.append(Build.MODEL.replaceAll(" ", ""));
        while (model.length() < 6) {
            model.append('0');
        }
        imei.append(model.substring(0, 6));
        Random random = new Random(time);
        long tmp = 0;
        while (tmp < 0x1000) {
            tmp = random.nextLong();
        }
        imei.append(Long.toHexString(tmp).substring(0, 4));
        return imei.toString();
    }

    public static String getModelName() {
        return android.os.Build.MODEL;
    }

    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            verCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return verCode;
    }

    public static String getVerName(Context context) {
        String verName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            verName = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return verName;
    }

    public static String getSign(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
            Iterator<PackageInfo> iter = apps.iterator();
            while (iter.hasNext()) {
                PackageInfo packageinfo = iter.next();
                String packageName = packageinfo.packageName;
                if (packageName.equals(context.getPackageName())) {
                    Signature[] signs = packageinfo.signatures;
                    Signature sign = signs[0];
                    String md5 = MD5.asHex(MD5.digest(sign.toByteArray()));
                    return md5;
                }
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            @SuppressWarnings("deprecation")
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static final String PREFS_NAME = "DEFAULT";
    public static String[] readPreference(Context context, String[] keys) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String[] results = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            results[i] = settings.getString(keys[i], "");
        }
        return results;
    }
    public static <T> T readPreferenceT(Context context, String key, T defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String value = settings.getString(key, null);
        if (value == null) return defaultValue;
        try {
            if (defaultValue.getClass().isAssignableFrom(String.class)) return (T)value;
            Method valueOf = defaultValue.getClass().getMethod("valueOf", new Class[]{String.class});
            if (valueOf != null) {
                return (T) valueOf.invoke(null, new Object[]{value});
            }
        } catch (Exception ex) {
        }
        return defaultValue;
    }

    public static String[] readPreference(Context context, String pref, String[] keys) {
        SharedPreferences settings = context.getSharedPreferences(pref, 0);
        String[] results = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            results[i] = settings.getString(keys[i], "");
        }
        return results;
    }

    public static String readPreference(Context context, String pref, String key) {
        return readPreference(context, pref, key, "");
    }

    public static String readPreference(Context context, String pref, String key, String def) {
        SharedPreferences settings = context.getSharedPreferences(pref, 0);
        String value = settings.getString(key, null);
        if (value == null) return def;
        else return value;
    }

    public static void savePreference(Context context, String[] keys, String[] values) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i < keys.length; i++) {
            editor.putString(keys[i], values[i]);
        }
        editor.commit();
    }
    public static void savePreference(Context context, String[] keys, Object[] values) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i < keys.length; i++) {
            editor.putString(keys[i], values[i].toString());
        }
        editor.commit();
    }

    public static void savePreference(Context context, String pref, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(pref, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static void savePreference(Context context, String pref, String[] keys, String[] values) {
        SharedPreferences settings = context.getSharedPreferences(pref, 0);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i < keys.length; i++) {
            editor.putString(keys[i], values[i]);
        }
        editor.commit();
    }

    public static byte[] readResource(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] array = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(array)) != -1) {
            outputStream.write(array, 0, len);
        }
        inputStream.close();
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /*
    <uses-permission android：name="android.permission.WAKE_LOCK" />
    <uses-permission android：name="android.permission.DISABLE_KEYGUARD" />
    */
    public void wakeAndUnlock(Context context, boolean unlock) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
        wakeLock.acquire();
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        if (unlock) {
            keyguardLock.disableKeyguard();
        } else {
            keyguardLock.reenableKeyguard();
        }
        wakeLock.release();
    }

    public static final boolean isGpsAvaliable(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }
    public static final void openGps(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    public static final boolean setObjectValue(Object object, String fieldName, Object value) {
        try {
            Class<?> clazz = object.getClass();
            Field field = null;
            do {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    if (field != null) break;
                } catch (Exception ex) {
                }
            } while ((clazz = clazz.getSuperclass()) != null);
            if (field == null) return false;
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            String type = field.getType().getSimpleName();
            if (("Long".equals(type) || "long".equals(type)) && (value instanceof Long)) field.setLong(object, (Long)value);
            else if (("Integer".equals(type) || "int".equals(type)) && (value instanceof Integer)) field.setInt(object, (Integer)value);
            else if (("Boolean".equals(type) || "boolean".equals(type)) && (value instanceof Integer)) field.setBoolean(object, (Boolean) value);
            else if (("Float".equals(type) || "float".equals(type)) && (value instanceof Integer)) field.setFloat(object, (Float) value);
            else if (("Double".equals(type) || "double".equals(type)) && (value instanceof Integer)) field.setDouble(object, (Double) value);
            else if (("Short".equals(type) || "short".equals(type)) && (value instanceof Integer)) field.setShort(object, (Short) value);
            else if (("char".equals(type)) && (value instanceof Integer)) field.setChar(object, (char) value);
            else if ("String".equals(type)) field.set(object, value);
            field.setAccessible(isAccessible);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

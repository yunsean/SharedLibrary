package com.dylan.uiparts.activity;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.dylan.common.utils.Utility;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RequestPermissionResultDispatch {

    public static void requestPermissions(Activity activity, int requestCode, String[] permissions) {
        if (!Utility.isMOrLater()) {
            doExecuteResult(activity, true, requestCode, null);
            return;
        }
        List<String> deniedPermissions = findDeniedPermissions(activity, permissions);
        if (deniedPermissions.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        } else {
            doExecuteResult(activity, true, requestCode, null);
        }
    }
    public static void requestPermissions(Fragment fragment, int requestCode, String[] permissions) {
        if (!Utility.isMOrLater()) {
            doExecuteResult(fragment, true, requestCode, null);
            return;
        }
        List<String> deniedPermissions = findDeniedPermissions(fragment.getActivity(), permissions);
        if (deniedPermissions.size() > 0) {
            fragment.requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
        } else {
            doExecuteResult(fragment, true, requestCode, null);
        }
    }
    public static List<String> findDeniedPermissions(Activity activity, String... permission){
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permission){
            if (activity == null || activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        onRequestPermissionsResult((Object)activity, requestCode, permissions, grantResults);
    }
    public static void onRequestPermissionsResult(Fragment fragment, int requestCode, String[] permissions, int[] grantResults) {
        onRequestPermissionsResult((Object)fragment, requestCode, permissions, grantResults);
    }
    public static void onRequestPermissionsResult(Object object, int requestCode, String[] permissions, int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++){
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                deniedPermissions.add(permissions[i]);
            }
        }
        doExecuteResult(object, deniedPermissions.size() == 0, requestCode, deniedPermissions.toArray(new String[deniedPermissions.size()]));
    }

    public static void doExecuteResult(Object object, boolean result, int requestCode, String[] deniedPermissions) {
        Method executeMethod = findMethodWithRequestCode(object.getClass(), requestCode);
        if(executeMethod != null){
            try {
                if(!executeMethod.isAccessible()) {
                    executeMethod.setAccessible(true);
                }
                Class<?>[] parameterTypes = executeMethod.getParameterTypes();
                if (parameterTypes.length == 3) {
                    executeMethod.invoke(object, result, requestCode, deniedPermissions);
                } else if (parameterTypes.length == 2) {
                    executeMethod.invoke(object, result, requestCode);
                } else if (parameterTypes.length == 1) {
                    executeMethod.invoke(object, result);
                } else if (parameterTypes.length == 0 && result) {
                    executeMethod.invoke(object);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static <A extends Annotation> Method findMethodWithRequestCode(Class clazz, int requestCode) {
        try {
            for (; clazz != null; clazz = clazz.getSuperclass()) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(RequestPermissionResult.class) && requestCode == method.getAnnotation(RequestPermissionResult.class).requestCode()) {
                        return method;
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}

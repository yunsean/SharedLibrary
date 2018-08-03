package com.dylan.uiparts.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ActivityResultDispatch {

    public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Method executeMethod = findMethodWithRequestCode(activity.getClass(), requestCode);
        if(executeMethod != null){
            try {
                if(!executeMethod.isAccessible()) {
                    executeMethod.setAccessible(true);
                }
                Class<?>[] parameterTypes = executeMethod.getParameterTypes();
                if (parameterTypes.length == 3) {
                    executeMethod.invoke(activity, requestCode, resultCode, data);
                } else if (parameterTypes.length == 2) {
                    executeMethod.invoke(activity, resultCode, data);
                } else if (parameterTypes.length == 1 && resultCode == Activity.RESULT_OK) {
                    executeMethod.invoke(activity, data);
                } else if (parameterTypes.length == 0 && resultCode == Activity.RESULT_OK) {
                    executeMethod.invoke(activity, new Object[]{});
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data) {
        Method executeMethod = findMethodWithRequestCode(fragment.getClass(), requestCode);
        if(executeMethod != null){
            try {
                if(!executeMethod.isAccessible()) {
                    executeMethod.setAccessible(true);
                }
                Class<?>[] parameterTypes = executeMethod.getParameterTypes();
                if (parameterTypes.length == 3) {
                    executeMethod.invoke(fragment, requestCode, resultCode, data);
                } else if (parameterTypes.length == 2) {
                    executeMethod.invoke(fragment, resultCode, data);
                } else if (parameterTypes.length == 1 && resultCode == Activity.RESULT_OK) {
                    executeMethod.invoke(fragment, data);
                } else if (parameterTypes.length == 0 && resultCode == Activity.RESULT_OK) {
                    executeMethod.invoke(fragment, new Object[]{});
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static <A extends Annotation> Method findMethodWithRequestCode(Class clazz, int requestCode) {
        try {
            for (; clazz != null; clazz = clazz.getSuperclass()) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(ActivityResult.class) && requestCode == method.getAnnotation(ActivityResult.class).requestCode()) {
                        return method;
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

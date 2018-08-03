package com.dylan.common.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class PojoUtil {
    public static <T extends Object> T mapToPojo(Map<String, Object> params, Class<?> clazz) {
        if (params == null) {
            return null;
        }
        try {
            T t = (T) clazz.newInstance();
            for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
                try {
                    Field[] fields = clazz.getDeclaredFields();
                    for(int i = 0 ; i< fields.length;i++){
                        String name = fields[i].getName();
                        Object value = params.get(name);
                        if(value != null && !"".equals(value)){
                            boolean isAccessible = fields[i].isAccessible();
                            fields[i].setAccessible(true);
                            fields[i].set(t, value);
                            if (!isAccessible)fields[i].setAccessible(false);
                        }
                    }
                } catch(Exception e){
                }
            }
            return t;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static <T extends Object> Map<String, Object> pojoToMap(T t) {
        if (t == null) {
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        Class<?> clazz = t.getClass();
        for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (int j = 0; j < fields.length; j++) {
                    String name = fields[j].getName();
                    Object value = null;
                    Method method = t.getClass().getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
                    value = method.invoke(t);
                    if(value != null) {
                        params.put(name, value);
                    }
                }
            } catch (Exception e) {
            }
        }
        return params;
    }

    public static <T extends Object> String pojoToKeyValue(T t, boolean urlEncoding) {
        if(t == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(1024);
        Class<?> clazz = t.getClass();
        for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (int j = 0; j < fields.length; j++) {
                try {
                    String name = fields[j].getName();
                    Object value = null;
                    Method method = t.getClass().getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
                    value = method.invoke(t);
                    sb.append(name);
                    sb.append("=");
                    if(value != null) {
                        String svalue = value.toString();
                        if (urlEncoding) svalue = URLEncoder.encode(svalue, "UTF-8").replaceAll("\\+", "%20");
                        sb.append(svalue);
                    }
                    sb.append("&");
                } catch (Exception ex) {
                }
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}

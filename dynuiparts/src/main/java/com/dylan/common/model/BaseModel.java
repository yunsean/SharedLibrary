package com.dylan.common.model;

import android.os.Parcel;
import android.util.Log;

import com.dylan.common.data.EnumUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BaseModel {
	
	@Target(ElementType.FIELD)
	public @interface ParcelItem {
	}
	
	public static <T> T createFromParcel(Parcel source, Class<?> classOfT) {
		try {
			@SuppressWarnings("unchecked")
			T item = (T) classOfT.newInstance();
			Field[] fields = classOfT.getDeclaredFields();
			for (Field f : fields) {
				if (f.getAnnotation(ParcelItem.class) == null) {
					continue;
				}
				String name = f.getName();
				Log.i("dylan", name);
				boolean accessible = f.isAccessible();
		        f.setAccessible(true);
				try {
			        Class<?> type = f.getType(); 
					if (type.isEnum()) {
						int value = source.readInt();
						Object e = EnumUtil.fromCode(value, type);
				        f.set(item, e);
					} else {
						f.set(item, source.readValue(null));
					}
				} catch (Exception e) {
				}
		        f.setAccessible(accessible);
			}
			return item;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void writeToParcel(Parcel dest, int flags) {
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			if (f.getAnnotation(ParcelItem.class) == null) {
				continue;
			}
			boolean accessible = f.isAccessible();
	        f.setAccessible(true);
			try {
				Class<?> type = f.getType(); 
				if (type.isEnum()) {
					Method[] methods = type.getMethods();
					for (Method method : methods) {
						if (method.getName().equals("ordinal")) {
							int code = (Integer) method.invoke(f.get(this));
							dest.writeInt(code);
						}
					}
				} else {
					dest.writeValue(f.get(this));
				}
			} catch (Exception e) {
			}
	        f.setAccessible(accessible);
		}
	}
};

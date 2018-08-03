package com.dylan.common.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class JsonUtil {
	public static final JSONObject toJsonObject(String content) {
		try {
			return new JSONObject(content);
		} catch (Exception ex) {
			return null;
		}
	}
	public static final JSONArray toJsonArray(String content) {
		try {
			return new JSONArray(content);
		} catch (Exception ex) {
			return null;
		}
	}

	public static final boolean boolValue(JSONObject obj, String key, boolean def) {
		try {
			return obj.getBoolean(key);
		} catch (Exception ex) {
			return def;
		}
	}
	public static final boolean boolValue(JSONObject obj, String key) {
		return boolValue(obj, key, false);
	}
	
	public static final int intValue(JSONObject obj, String key, int def) {
		try {
			return obj.getInt(key);
		} catch (Exception ex) {
			return def;
		}
	}
	public static final int intValue(JSONObject obj, String key) {
		return intValue(obj, key, 0);
	}
	public static final long longValue(JSONObject obj, String key, long def) {
		try {
			return obj.getLong(key);
		} catch (Exception ex) {
			return def;
		}
	}
	public static final long longValue(JSONObject obj, String key) {
		return longValue(obj, key, 0);
	}
	public static final float floatValue(JSONObject obj, String key, float def) {
		try {
			return (float) obj.getDouble(key);
		} catch (Exception ex) {
			return def;
		}
	}
	public static final float floatValue(JSONObject obj, String key) {
		return floatValue(obj, key, 0);
	}
	public static final double doubleValue(JSONObject obj, String key, double def) {
		try {
			return obj.getDouble(key);
		} catch (Exception ex) {
			return def;
		}
	}
	public static final double doubleValue(JSONObject obj, String key) {
		return doubleValue(obj, key, 0);
	}

	public static final String textValue(JSONObject obj, String key, String def) {
		try {
			if (!obj.isNull(key)) {
				return obj.getString(key);
			} else {
				return def;
			}
		} catch (Exception ex) {
			return def;
		}
	}
	public static final String textValue(JSONArray array, int index, String def) {
		try {
			return array.getString(index);
		} catch (Exception ex) {
			return def;
		}
	}
	public static final String textValue(JSONObject obj, String key) {
		return textValue(obj, key, null);
	}
	public static final String textValue(JSONArray array, int index) {
		return textValue(array, index, null);
	}

	public static final JSONObject jsonValue(JSONObject obj, String key) {
		try {
			return obj.getJSONObject(key);
		} catch (Exception ex) {
			return null;
		}
	}
	public static final JSONObject jsonValue(JSONArray obj, int index) {
		try {
			return obj.getJSONObject(index);
		} catch (Exception ex) {
			return null;
		}
	}
	public static final JSONArray arrayValue(JSONObject obj, String key) {
		try {
			return obj.getJSONArray(key);
		} catch (Exception ex) {
			return null;
		}
	}
	public static final String[] textArrayValue(JSONObject obj, String key) {
		try {
			JSONArray array = obj.getJSONArray(key);
			String[] value = new String[array.length()];
			for (int i = 0; i < array.length(); i++) {
				value[i] = array.getString(i);
			}
			return value;
		} catch (Exception ex) {
			return null;
		}
	}
	public static final int[] intArrayValue(JSONObject obj, String key) {
		try {
			JSONArray array = obj.getJSONArray(key);
			int[] value = new int[array.length()];
			for (int i = 0; i < array.length(); i++) {
				value[i] = array.getInt(i);
			}
			return value;
		} catch (Exception ex) {
			return null;
		}		
	}
	
	public static final <T> T valueFor(JSONObject obj, Class<?> classOfT) {
		try {
			@SuppressWarnings("unchecked")
			T value = (T) classOfT.newInstance();
			Field[] fields = classOfT.getDeclaredFields();
			for (Field f : fields) {
				String name = f.getName();
				if (obj.has(name) && !obj.isNull(name)) {
					boolean accessible = f.isAccessible();
			        f.setAccessible(true);
			        Class<?> type = f.getType(); 
					try {
						if (type.isEnum()) {
							Object e = EnumUtil.fromCode(obj.getInt(name), type);
					        f.set(value, e);
						} else if (type.isPrimitive()) {
					        f.set(value, obj.get(name));
						} else if (type == String.class) { 
							f.set(value, obj.getString(name));
						} else {
							Object o = valueFor(obj.getJSONObject(name), type);
					        f.set(value, o);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
			        f.setAccessible(accessible);
				}
			}
			return value;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}

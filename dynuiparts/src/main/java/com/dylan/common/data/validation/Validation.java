package com.dylan.common.data.validation;

import androidx.databinding.ObservableField;

import com.dylan.common.data.validation.constraint.Constraint;
import com.dylan.common.data.validation.constraint.ConstraintValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Validation {

    public static ValidResult verify(Object target) {
        Class<?> targetClass = target.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        ValidResult result = new ValidResult();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            if (annotations == null || annotations.length < 1)continue;
            for (Annotation annotation : annotations) {
                Class<?> annotationType = annotation.annotationType();
                if (annotationType.getAnnotation(Constraint.class) != null) {
                    Boolean accessible = null;
                    Object value = null;
                    try {
                        Constraint constraint = annotationType.getAnnotation(Constraint.class);
                        accessible = field.isAccessible();
                        field.setAccessible(true);
                        value = field.get(target);
                        if (value instanceof ObservableField) value = ((ObservableField)value).get();
                        Class<? extends ConstraintValidator> validatedBy = constraint.validatedBy();
                        if (validatedBy != null) {
                            ConstraintValidator<?> validator = validatedBy.newInstance();
                            Method method = null;
                            boolean isValid = true;
                            if (value == null) {
                                if (!validator.allowNull()) {
                                    isValid = false;
                                }
                            } else if (constraint.specializedType() != null) {
                                Class<?>[] specializedTypes = constraint.specializedType();
                                if ((method = getMethod(validatedBy, "initialize", annotationType)) != null) {
                                    method.invoke(validator, annotation);
                                }
                                boolean specialized = false;
                                for (Class<?> specializedType : specializedTypes) {
                                    if (specializedType.isInstance(value)) {
                                        if ((method = getMethod(validatedBy, "isValid", specializedType)) != null) {
                                            isValid = (Boolean) method.invoke(validator, value);
                                            specialized = true;
                                            break;
                                        }
                                    }
                                }
                                if (!specialized) {
                                    if ((method = getMethod(validatedBy, "isValid", Object.class)) != null) {
                                        isValid = (Boolean) method.invoke(validator, value);
                                        specialized = true;
                                    }
                                }
                            }
                            if (!isValid) {
                                String message = null;
                                if ((method = getMethod(annotationType, "message")) != null) {
                                    message = (String) method.invoke(annotation);
                                }
                                result.reject(field.getName(), value, message);
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        result.reject(field.getName(), value, ex.getMessage());
                    } finally {
                        if (accessible != null) {
                            field.setAccessible(accessible);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(name, parameterTypes);
        } catch (Exception ex) {
            return null;
        }
    }
}

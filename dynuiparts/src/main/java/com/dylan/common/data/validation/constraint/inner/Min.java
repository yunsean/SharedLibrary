package com.dylan.common.data.validation.constraint.inner;

import android.widget.TextView;

import com.dylan.common.data.NumberUtil;
import com.dylan.common.data.validation.constraint.Constraint;
import com.dylan.common.data.validation.constraint.ConstraintValidator;
import com.dylan.common.sketch.Sketch;
import com.dylan.uiparts.layout.TextLineInfo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Min.MinValidator.class,
    specializedType = {Long.class, CharSequence.class, Integer.class, Float.class, Double.class, Short.class, Byte.class, TextView.class, TextLineInfo.class})
public @interface Min {
    String message() default "{validation.constraints.Min.message}";
    long value();

    public class MinValidator implements ConstraintValidator<Min> {
        private long expectMin = 0;
        @Override
        public void initialize(Min constraintAnnotation) {
            expectMin = constraintAnnotation.value();
        }
        @Override
        public boolean isValid(Object value) {
            return false;
        }
        @Override
        public boolean allowNull() {
            return false;
        }
        public boolean isValid(Long value) {
            return value >= expectMin;
        }
        public boolean isValid(Integer value) {
            return value >= expectMin;
        }
        public boolean isValid(Float value) {
            return value >= expectMin;
        }
        public boolean isValid(Double value) {
            return value >= expectMin;
        }
        public boolean isValid(Short value) {
            return value >= expectMin;
        }
        public boolean isValid(Byte value) {
            return value >= expectMin;
        }
        public boolean isValid(CharSequence value) {
            int n = NumberUtil.intValue(value.toString());
            return n >= expectMin;
        }
        public boolean isValid(TextView value) {
            return isValid(Sketch.get_tv(value));
        }
        public boolean isValid(TextLineInfo value) {
            return isValid(Sketch.get_tli(value));
        }
    }
}

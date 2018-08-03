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
@Constraint(validatedBy = Max.MinValidator.class,
    specializedType = {Long.class, CharSequence.class, Integer.class, Float.class, Double.class, Short.class, Byte.class, TextView.class, TextLineInfo.class})
public @interface Max {
    String message() default "{validation.constraints.Min.message}";
    long value();

    public class MinValidator implements ConstraintValidator<Max> {
        private long expectMax = 0;
        @Override
        public void initialize(Max constraintAnnotation) {
            expectMax = constraintAnnotation.value();
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
            return value <= expectMax;
        }
        public boolean isValid(Integer value) {
            return value <= expectMax;
        }
        public boolean isValid(Float value) {
            return value <= expectMax;
        }
        public boolean isValid(Double value) {
            return value <= expectMax;
        }
        public boolean isValid(Short value) {
            return value <= expectMax;
        }
        public boolean isValid(Byte value) {
            return value <= expectMax;
        }
        public boolean isValid(CharSequence value) {
            int n = NumberUtil.intValue(value.toString());
            return n <= expectMax;
        }
        public boolean isValid(TextView value) {
            return isValid(Sketch.get_tv(value));
        }
        public boolean isValid(TextLineInfo value) {
            return isValid(Sketch.get_tli(value));
        }
    }
}

package com.dylan.common.data.validation.constraint.inner;

import android.widget.TextView;

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
@Constraint(validatedBy = Size.MinValidator.class,
    specializedType = {TextView.class, TextLineInfo.class, CharSequence.class})
public @interface Size {
    String message() default "{validation.constraints.Min.message}";
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;

    public class MinValidator implements ConstraintValidator<Size> {
        private int expectMin = Integer.MIN_VALUE;
        private int expectMax = Integer.MAX_VALUE;
        @Override
        public void initialize(Size constraintAnnotation) {
            expectMin = constraintAnnotation.min();
            expectMax = constraintAnnotation.max();
        }
        @Override
        public boolean isValid(Object value) {
            if (value == null) return true;
            return false;
        }
        @Override
        public boolean allowNull() {
            return true;
        }
        public boolean isValid(CharSequence value) {
            int length = value.toString().trim().length();
            if (length < expectMin) return false;
            if (length > expectMax) return false;
            return true;
        }
        public boolean isValid(TextView value) {
            return isValid(Sketch.get_tv(value));
        }
        public boolean isValid(TextLineInfo value) {
            return isValid(Sketch.get_tli(value));
        }
    }
}

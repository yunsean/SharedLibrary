package com.dylan.common.data.validation.constraint.inner;

import android.widget.TextView;

import com.dylan.common.data.validation.constraint.Constraint;
import com.dylan.common.data.validation.constraint.ConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotNull.NotNullValidator.class, specializedType = {TextView.class, String.class, CharSequence.class})
public @interface NotNull {
    String message() default "{validation.constraints.NotNull.message}";

    public class NotNullValidator implements ConstraintValidator<NotNull> {
        @Override
        public void initialize(NotNull constraintAnnotation) {
        }
        @Override
        public boolean isValid(Object value) {
            return value != null;
        }
        @Override
        public boolean allowNull() {
            return false;
        }
        public boolean isValid(TextView value) {
            if (value == null || !value.isShown()) return true;
            return ((TextView) value).getText().toString().trim().length() > 0;
        }
        public boolean isValid(String value) {
            return value.trim().length() > 0;
        }
        public boolean isValid(CharSequence value) {
            return value.toString().trim().length() > 0;
        }
    }
}

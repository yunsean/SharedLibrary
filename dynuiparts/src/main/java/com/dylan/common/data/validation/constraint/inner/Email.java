package com.dylan.common.data.validation.constraint.inner;

import android.widget.TextView;

import com.dylan.common.data.validation.constraint.Constraint;
import com.dylan.common.data.validation.constraint.ConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

@Documented
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Email.NotNullValidator.class, specializedType = {TextView.class, String.class, CharSequence.class})
public @interface Email {
    String message() default "{validation.constraints.NotNull.message}";

    public class NotNullValidator implements ConstraintValidator<Email> {
        @Override
        public void initialize(Email constraintAnnotation) {
        }
        @Override
        public boolean isValid(Object value) {
            return false;
        }
        @Override
        public boolean allowNull() {
            return true;
        }
        public boolean isValid(TextView value) {
            return isValid(((TextView) value).getText().toString());
        }
        public boolean isValid(String value) {
            if (value == null || value.length() < 1)return true;
            String regex = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
            return Pattern.matches(regex, value);
        }
        public boolean isValid(CharSequence value) {
            return isValid(value.toString());
        }
    }
}

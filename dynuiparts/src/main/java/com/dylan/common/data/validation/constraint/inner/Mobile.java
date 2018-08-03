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
@Constraint(validatedBy = Mobile.NotNullValidator.class, specializedType = {TextView.class, String.class, CharSequence.class})
public @interface Mobile {
    String message() default "{validation.constraints.NotNull.message}";

    public class NotNullValidator implements ConstraintValidator<Mobile> {
        @Override
        public void initialize(Mobile constraintAnnotation) {
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
            if (value == null || value.isShown()) return true;
            return isValid(((TextView) value).getText().toString());
        }
        public boolean isValid(String value) {
            if (value == null || value.length() < 1)return true;
            return isMobileNumber(value);
        }
        public boolean isValid(CharSequence value) {
            return isValid(value.toString());
        }

        private boolean isMobileNumber(String mobile) {
            final String MOBILE = "^1(3[0-9]|4[57]|5[0-35-9]|7[01678]|8[0-9])\\d{8}$";
            final String CM = "^1(3[4-9]|4[7]|5[0-27-9]|7[08]|8[2-478])\\d{8}$";
            final String CU = "^1(3[0-2]|4[5]|5[256]|7[016]|8[56])\\d{8}$";
            final String CT = "^1(3[34]|53|7[07]|8[019])\\d{8}$";
            if (Pattern.matches(MOBILE, mobile) ||
                    Pattern.matches(CM, mobile) ||
                    Pattern.matches(CU, mobile) ||
                    Pattern.matches(CT, mobile)) {
                return true;
            } else {
                return false;
            }
        }
    }
}

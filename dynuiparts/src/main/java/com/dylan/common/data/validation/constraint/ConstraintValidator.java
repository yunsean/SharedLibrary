package com.dylan.common.data.validation.constraint;

import java.lang.annotation.Annotation;

public interface ConstraintValidator<A extends Annotation> {
    public void initialize(A constraintAnnotation);
    public boolean isValid(Object value);
    public boolean allowNull();
}

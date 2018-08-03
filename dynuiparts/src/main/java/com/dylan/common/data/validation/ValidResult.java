package com.dylan.common.data.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidResult {
    private List<FieldError> fieldErrors = new ArrayList<>();
    public void reject(String field, Object rejectValue, String message) {
        fieldErrors.add(new FieldError(field, rejectValue, message));
    }
    public boolean hasErrors() {
        return fieldErrors.size() > 0;
    }
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
    public String getFirstError() {
        if (fieldErrors.size() < 1)return null;
        return fieldErrors.get(0).getMessage();
    }

    public FieldError hasError(String field) {
        for (FieldError error : fieldErrors) {
            if (error.getField().equals(field)) {
                return error;
            }
        }
        return null;
    }
}

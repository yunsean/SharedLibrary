package com.dylan.common.data.validation;

public class FieldError {
    private String field = null;
    private Object rejectValue = null;
    private String message;

    public FieldError(String field, Object rejectValue, String message) {
        this.field = field;
        this.rejectValue = rejectValue;
        this.message = message;
    }

    public String getField() {
        return field;
    }
    public Object getRejectValue() {
        return rejectValue;
    }
    public String getMessage() {
        return message;
    }
}

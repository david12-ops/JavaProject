package com.example.utils;

import com.example.utils.interfaces.ErrorHandler;

public class ValidationBundle<T> {
    private final T validator;
    private final ErrorHandler errorHandler;

    public ValidationBundle(T validator, ErrorHandler errorHandler) {
        this.validator = validator;
        this.errorHandler = errorHandler;
    }

    public T getValidator() {
        return validator;
    }

    public ErrorHandler getErrorManager() {
        return errorHandler;
    }
}

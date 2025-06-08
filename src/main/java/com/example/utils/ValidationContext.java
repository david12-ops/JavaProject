package com.example.utils;

import java.util.HashMap;
import java.util.Map;

import com.example.utils.enums.ValidationMode;
import com.example.utils.services.ValidationService;

public class ValidationContext {
    protected class ValidationBundle<T> {
        private final T validator;
        private final ErrorManager errorManager;

        public ValidationBundle(T validator, ErrorManager errorManager) {
            this.validator = validator;
            this.errorManager = errorManager;
        }

        public T getValidator() {
            return validator;
        }

        public ErrorManager getErrorManager() {
            return errorManager;
        }
    }

    private Map<String, String> errorMap = new HashMap<>();
    private final ErrorManager errorToolManager = new ErrorManager(errorMap);
    private final ValidationService validationService = new ValidationService();

    private ValidationBundle<ValidationService.MessageValidations> messageValidatorBundle;
    private ValidationBundle<ValidationService.UserValidations> userValidatorBundle;

    public ValidationContext(ValidationMode mode) {
        if (mode == ValidationMode.USER) {
            this.userValidatorBundle = new ValidationBundle<>(validationService.new UserValidations(errorToolManager),
                    errorToolManager);
        }

        if (mode == ValidationMode.MESSAGE) {
            this.messageValidatorBundle = new ValidationBundle<>(
                    validationService.new MessageValidations(errorToolManager), errorToolManager);
        }
    }

    public ValidationBundle<ValidationService.MessageValidations> getMessageValidationBundle() {
        if (messageValidatorBundle == null)
            throw new IllegalStateException("Message validations not initialized. Use ValidationMode.MESSAGE.");
        return messageValidatorBundle;
    }

    public ValidationBundle<ValidationService.UserValidations> getUserValidationBundle() {
        if (userValidatorBundle == null)
            throw new IllegalStateException("User validations not initialized. Use ValidationMode.USER.");
        return userValidatorBundle;
    }
}

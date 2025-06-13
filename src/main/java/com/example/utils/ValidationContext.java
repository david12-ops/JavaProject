package com.example.utils;

import java.util.HashMap;
import java.util.Map;

import com.example.utils.enums.ValidationMode;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.services.ValidationService;

public class ValidationContext {
    private Map<String, String> errorMap = new HashMap<>();
    private final ErrorHandler errorHandler = new ErrorManager(errorMap);
    private final ValidationService validationService = new ValidationService();

    private ValidationBundle<ValidationService.MessageValidations> messageValidatorBundle;
    private ValidationBundle<ValidationService.UserValidations> userValidatorBundle;

    public ValidationContext(ValidationMode mode) {
        if (mode == ValidationMode.USER) {
            this.userValidatorBundle = new ValidationBundle<>(validationService.new UserValidations(errorHandler),
                    errorHandler);
        }

        if (mode == ValidationMode.MESSAGE) {
            this.messageValidatorBundle = new ValidationBundle<>(validationService.new MessageValidations(errorHandler),
                    errorHandler);
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

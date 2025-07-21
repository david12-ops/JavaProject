package com.example.utils.services;

import com.example.model.UserToken;
import com.example.utils.ValidationContext;
import com.example.utils.enums.ValidationMode;
import com.example.utils.interfaces.AuthService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.MailService;
import com.example.utils.interfaces.MessageValidator;

public class MailboxService implements MailService {
    private static MailboxService instance;

    private final ValidationContext validationContext = new ValidationContext(ValidationMode.MESSAGE);
    private ErrorHandler errorHandler;
    private MessageValidator messageValidator;

    private MailboxService() {
        errorHandler = validationContext.getMessageValidationBundle().getErrorManager();
        messageValidator = validationContext.getMessageValidationBundle().getValidator();
    }

    public static MailboxService getInstance() {
        if (instance == null) {
            synchronized (MailboxService.class) {
                if (instance == null) {
                    instance = new MailboxService();
                }
            }
        }

        return instance;
    }

    @Override
    public void sendMessage(UserToken userToken, ) {
        if (userToken != null) {

        }
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
    }

    @Override
    public void updateStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateStatus'");
    }

    @Override
    public void removeMessage() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeMessage'");
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}

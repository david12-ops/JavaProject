package com.example.utils.services;

import java.io.File;
import java.util.List;

import com.example.dto.MessageDTO;
import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.MessageRepository;
import com.example.utils.RepositoryFactory;
import com.example.utils.ValidationContext;
import com.example.utils.enums.ValidationMode;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.MailService;
import com.example.utils.interfaces.MessageValidator;

public class MailboxService implements MailService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.MESSAGE);
    private final MessageRepository messageRepository = RepositoryFactory.getMessageRepository();
    private final List<UserDTO> userDTOs = RepositoryFactory.getUserRepository().getAllUserDtos();

    private ErrorHandler errorHandler;
    private MessageValidator messageValidator;

    private record LabeledValue(String label, Object value) {
    }

    // Support Methods
    private boolean containsDataNull(String errorKey, LabeledValue... labeledValues) {
        for (LabeledValue labeledValue : labeledValues) {
            if (labeledValue.value == null) {
                errorHandler.logError(errorHandler.createErrorBody(errorKey, "Invalid " + labeledValue.label()));
                return true;
            }
        }
        return false;
    }

    private String resolveUserId() {
        return null;
    }

    private String resolveMessageId() {
        return null;
    }

    public MailboxService() {
        errorHandler = validationContext.getMessageValidationBundle().getErrorManager();
        messageValidator = validationContext.getMessageValidationBundle().getValidator();
    }

    @Override
    public void sendMessage(UserToken senderToken, String recevierId, String subject, String message,
            List<File> files) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
    }

    @Override
    public void updateStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateStatus'");
    }

    @Override
    public void removeMessage(MessageDTO messageDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeMessage'");
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}

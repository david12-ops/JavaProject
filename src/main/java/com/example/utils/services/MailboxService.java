package com.example.utils.services;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.dto.MessageDTO;
import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.MessageRepository;
import com.example.utils.RepositoryFactory;
import com.example.utils.ValidationContext;
import com.example.utils.enums.ValidationMode;
import com.example.utils.enums.ViewLevel;
import com.example.utils.enums.MessageStatus;
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

    private MessageDTO createMessageDTO(String messageId, String senderId, String senderMailAccount, String recevierId,
            String recevierMailAccount, String subject, String message, LocalDateTime timestamp,
            List<String> attachedBase64Files, Map<String, Set<MessageStatus>> statuses, List<File> attachedFiles) {
        return new MessageDTO(messageId, senderId, senderMailAccount, recevierId, recevierMailAccount, subject, message,
                timestamp, attachedBase64Files, statuses, attachedFiles);
    }

    private boolean containsDataNull(String errorKey, LabeledValue... labeledValues) {
        for (LabeledValue labeledValue : labeledValues) {
            if (labeledValue.value == null) {
                errorHandler.logError(errorHandler.createErrorBody(errorKey, "Invalid " + labeledValue.label()));
                return true;
            }
        }
        return false;
    }

    private String resolveUserIdByEmail(String email) {
        if (userDTOs == null || userDTOs.size() == 0)
            return null;

        for (UserDTO userDTO : userDTOs) {
            String userEmailFromDTO = userDTO.getMailAccount();
            if (userEmailFromDTO != null && userEmailFromDTO.equals(email))
                return userDTO.getUserId();
        }
        return null;
    }

    private String resolveEmailByUserId(String userId) {
        if (userDTOs == null || userDTOs.size() == 0)
            return null;

        for (UserDTO userDTO : userDTOs) {
            String userIdFromDTO = userDTO.getUserId();
            if (userIdFromDTO != null && userIdFromDTO.equals(userId))
                return userDTO.getMailAccount();
        }
        return null;
    }

    public MailboxService() {
        errorHandler = validationContext.getMessageValidationBundle().getErrorManager();
        messageValidator = validationContext.getMessageValidationBundle().getValidator();
    }

    @Override
    public void sendMessage(UserToken senderToken, String recevierEmail, String subject, String message,
            List<File> files) {
        if (containsDataNull("sendMessage", new LabeledValue("token", senderToken)))
            return;

        boolean isValid = messageValidator.validMessageData(recevierEmail, subject, message)
                && messageValidator.validFiles(files);
        String resolvedRecevierId = resolveUserIdByEmail(recevierEmail);

        if (isValid && !containsDataNull("sendMessage", new LabeledValue("recevierId", resolvedRecevierId))) {
            MessageDTO newMessageDTO = createMessageDTO(null, senderToken.getUserId(), null, resolvedRecevierId, null,
                    subject, message, LocalDateTime.now(), null, null, files);
            messageRepository.addMessage(senderToken, newMessageDTO);
        }
    }

    @Override
    public void updateStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateStatus'");
    }

    @Override
    public void removeMessage(MessageDTO messageDTO) {
        messageRepository.removeMessage(messageDTO);
    }

    @Override
    public List<MessageDTO> getMessageDTOs(UserToken userToken, MessageStatus messageStatus) {
        List<MessageDTO> messageDTOs = messageRepository.getAllMessageDtos();
        List<MessageDTO> updatedMessageDTOs = new ArrayList<>();

        boolean containsNull = containsDataNull("getMessageDTOs", new LabeledValue("userToken", userToken),
                new LabeledValue("DTOs", messageDTOs));

        if (containsNull)
            return List.of();

        messageDTOs.forEach(messageDTO -> {
            String senderMailAccount = resolveEmailByUserId(messageDTO.getSenderId());
            String recevierMailAccount = resolveEmailByUserId(messageDTO.getRecevierId());

            MessageDTO updatedMessageDTO = createMessageDTO(messageDTO.getMessageId(), messageDTO.getSenderId(),
                    senderMailAccount, messageDTO.getRecevierId(), recevierMailAccount, messageDTO.getSubject(),
                    messageDTO.getMessage(), messageDTO.getTimestamp(), messageDTO.getAttachedBase64Files(),
                    messageDTO.getStatuses(), messageDTO.getAttachedFiles());
            updatedMessageDTO.sanitize(ViewLevel.PUBLIC);
            updatedMessageDTOs.add(updatedMessageDTO);
        });

        return updatedMessageDTOs;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}

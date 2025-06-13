package com.example.model;

import com.example.utils.FileConvertor;
import com.example.utils.JsonStorageTool;
import com.example.utils.ValidationContext;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.ValidationMode;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.MessageValidator;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MessageRepository {

    static Dotenv dotenv = Dotenv.load();
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.MESSAGE);

    private ErrorHandler errorHandler;
    private MessageValidator messageValidator;
    private List<Message> listOfMessages;
    private JsonStorageTool<Message> storageTool;
    private EnvironmentType environmentType;
    private Map<MessageStatus, Set<MessageStatus>> allowedByStatus;

    private void defineMessageStatusTransition() {
        Map<MessageStatus, Set<MessageStatus>> map = new HashMap<>();

        map.put(MessageStatus.INBOX, EnumSet.of(MessageStatus.TRASH, MessageStatus.STARRED, MessageStatus.SNOOZED));
        map.put(MessageStatus.SENT, EnumSet.of(MessageStatus.TRASH, MessageStatus.STARRED, MessageStatus.SNOOZED));
        map.put(MessageStatus.STARRED, EnumSet.of(MessageStatus.TRASH, MessageStatus.SNOOZED));

        map.put(MessageStatus.SNOOZED, Set.of());
        map.put(MessageStatus.DRAFTS, Set.of());
        map.put(MessageStatus.TRASH, Set.of());

        allowedByStatus = Collections.unmodifiableMap(map);
    }

    public MessageRepository(EnvironmentType environmentType) {
        this.environmentType = environmentType;
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool = new JsonStorageTool<Message>(dotenv.get("FILE_PATH_MESSAGES"),
                    new TypeReference<List<Message>>() {
                    });
            this.listOfMessages = storageTool.getItems();
            defineMessageStatusTransition();
            this.messageValidator = validationContext.getMessageValidationBundle().getValidator();
            this.errorHandler = validationContext.getMessageValidationBundle().getErrorManager();
        } else if (environmentType == EnvironmentType.TEST) {
            this.listOfMessages = new ArrayList<>();
            defineMessageStatusTransition();
            this.messageValidator = validationContext.getMessageValidationBundle().getValidator();
            this.errorHandler = validationContext.getMessageValidationBundle().getErrorManager();
        }
    }

    // Support Methods
    private void applyMessageAdding(Message message) {
        clearError("addMessage");
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.addItem(message);
            listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfMessages.add(message);
        }
    }

    private boolean isStatusUpdateAllowed(Message message, MessageStatus newStatus, String userKey) {
        Set<MessageStatus> messageStatuses = message.getStatuses().get(userKey);
        for (MessageStatus status : messageStatuses) {
            if (!allowedByStatus.get(status).contains(newStatus)) {
                return false;
            }
        }

        return true;
    }

    private boolean containsOnlySupportedStatuses(Map<String, Set<MessageStatus>> messageStatuses,
            List<MessageStatus> expectedMessageStatus) {
        for (Set<MessageStatus> statuses : messageStatuses.values()) {
            for (MessageStatus status : statuses) {
                if (!expectedMessageStatus.contains(status)) {
                    return false;
                }
            }
        }

        return true;
    }

    // Methods that implement the main logic
    public void setTestData(List<Message> listOfMessages) {
        this.listOfMessages = listOfMessages;
    }

    public List<Message> getTestData() {
        return listOfMessages;
    }

    public String getError(String errorName) {
        return errorHandler.getError(errorName);
    }

    public void clearError(String errorName) {
        errorHandler.removeError(errorName);
    }

    public void addMessage(UserToken senderToken, String recevierEmail, String subject, String message,
            List<File> files) {

        boolean isValid = messageValidator.validMessageData(recevierEmail, subject, message)
                && messageValidator.validFiles(files);

        if (senderToken == null) {
            errorHandler.logError(errorHandler.createErrorBody("addMessage", "Token of sender is required"));
            return;
        }

        if (!isValid) {
            errorHandler
                    .logError(errorHandler.createErrorBody("addMessage", "Invalid message data or unsupported files"));
            return;
        }

        List<String> base64Files = (files != null && !files.isEmpty()) ? files.stream().map(file -> {
            try {
                return FileConvertor.imageToBase64(file);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).toList() : null;

        Map<String, Set<MessageStatus>> messageStatuses = new HashMap<>();
        messageStatuses.put(senderToken.getUserId(), Set.of(MessageStatus.SENT));
        messageStatuses.put(recevierEmail, Set.of(MessageStatus.INBOX));

        if (!containsOnlySupportedStatuses(messageStatuses, List.of(MessageStatus.SENT, MessageStatus.INBOX))) {
            errorHandler
                    .logError(errorHandler.createErrorBody("addMessage", "Message contains unsupported status values"));
            return;
        }

        Message newMessage = new Message(null, senderToken.getUserId(), recevierEmail, subject, message,
                LocalDateTime.now(), base64Files, messageStatuses);

        applyMessageAdding(newMessage);
    }

    public void removeMessage(Message message) {
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.removeItem(message);
            listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfMessages.remove(message);
        }
    }

    public void updateMessageStatus(Message message, MessageStatus statusFrom, MessageStatus statusTo,
            UserToken userToken) {

        if (userToken == null) {
            errorHandler.logError(errorHandler.createErrorBody("updateMessageStatus", "Token of user required"));
            return;
        }

        String userKey = statusFrom == MessageStatus.INBOX ? userToken.getMailAccount() : userToken.getUserId();

        if (!isStatusUpdateAllowed(message, statusTo, userKey)) {
            errorHandler.logError(
                    errorHandler.createErrorBody("updateMessageStatus", "Message status update is not allowed"));
            return;
        }

        Set<MessageStatus> currenMessageStatuses = new HashSet<>(message.getStatuses().getOrDefault(userKey, Set.of()));
        currenMessageStatuses.add(statusTo);
        message.setStatuses(userKey, currenMessageStatuses);

        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.updateItem(message, message);
            listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfMessages.set(listOfMessages.indexOf(message), message);
        }
    }

    public List<Message> getAllReceviedMessagesByUser(String recevierEmail) {
        return listOfMessages.stream().filter(message -> message.getReceiver().equals(recevierEmail)).toList();
    }

    public List<Message> getAllSentMessagesByUser(String userId) {
        return listOfMessages.stream().filter(message -> message.getSenderId().equals(userId)).toList();
    }
}

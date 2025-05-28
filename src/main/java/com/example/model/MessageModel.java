package com.example.model;

import com.example.utils.ErrorToolManager;
import com.example.utils.FileConvertor;
import com.example.utils.JsonStorageTool;
import com.example.utils.enums.Environment;
import com.example.utils.enums.MessageStatus;
import com.example.utils.services.ValidationService;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MessageModel {

    static Dotenv dotenv = Dotenv.load();
    private HashMap<String, String> errorMap = new HashMap<>();
    private final ErrorToolManager errorToolManager = new ErrorToolManager(errorMap);
    private final ValidationService validationService = new ValidationService();
    private final ValidationService.MessageModelValidations validator = validationService.new MessageModelValidations(
            errorToolManager);
    private List<Message> listOfMessages;
    private JsonStorageTool<Message> storageTool;
    private Environment environment;

    public MessageModel(Environment environment) {
        this.environment = environment;
        if (environment == Environment.PRODUCTION) {
            storageTool = new JsonStorageTool<Message>(dotenv.get("FILE_PATH_MESSAGES"),
                    new TypeReference<List<Message>>() {
                    });
            this.listOfMessages = storageTool.getItems();
        } else if (environment == Environment.TEST) {
            this.listOfMessages = new ArrayList<>();
        }
    }

    // Support Methods
    private void applyMessageAdding(Message message) {
        clearError("addMesssage");
        if (environment == Environment.PRODUCTION) {
            storageTool.addItem(message);
            listOfMessages = storageTool.getItems();
        } else if (environment == Environment.TEST) {
            listOfMessages.add(message);
        }
    }

    private boolean containsOnlySupportedStatuses(Map<String, Set<MessageStatus>> messageStatus,
            List<MessageStatus> expectedMessageStatus) {
        boolean expectedStatus = false;
        for (Set<MessageStatus> statuses : messageStatus.values()) {
            for (MessageStatus status : statuses) {
                if (expectedMessageStatus.contains(status)) {
                    expectedStatus = true;
                } else {
                    expectedStatus = false;
                }
            }
        }

        return expectedStatus;
    }

    // Methods that implement the main logic
    public void setTestData(List<Message> listOfMessages) {
        this.listOfMessages = listOfMessages;
    }

    public List<Message> getTestData() {
        return listOfMessages;
    }

    public String getError(String errorName) {
        return errorToolManager.getError(errorName);
    }

    public void clearError(String errorName) {
        errorToolManager.removeError(errorName);
    }

    public void addMessage(UserToken senderToken, String recevierEmail, String subject, String message,
            List<File> files) {

        boolean isValid = validator.validMessageData(recevierEmail, subject, message) && validator.validFiles(files);

        if (senderToken == null) {
            errorToolManager.logError(errorToolManager.createErrorBody("addMessage", "Token of sender is required"));
            return;
        }

        if (!isValid) {
            errorToolManager.logError(
                    errorToolManager.createErrorBody("addMessage", "Invalid message data or unsupported files"));
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

        Map<String, Set<MessageStatus>> messageStatus = new HashMap<>();
        messageStatus.put(senderToken.getUserId(), Set.of(MessageStatus.SENT));
        messageStatus.put(recevierEmail, Set.of(MessageStatus.INBOX));

        if (!containsOnlySupportedStatuses(messageStatus, List.of(MessageStatus.SENT, MessageStatus.INBOX))) {
            errorToolManager.logError(
                    errorToolManager.createErrorBody("addMessage", "Message contains unsupported status values"));
            return;
        }

        Message newMessage = new Message(null, senderToken.getUserId(), recevierEmail, subject, message,
                LocalDateTime.now(), base64Files, messageStatus);

        applyMessageAdding(newMessage);
    }

    public void removeMessage(Message message) {
        storageTool.removeItem(message);
    }

    public void updateMessageStatus(Message message, String aacount, MessageStatus status) {
        message.setStatus(aacount, Set.of(status));
    }

    public List<Message> getAllReceviedMessagesByUser(String recevierEmail) {
        return listOfMessages.stream().filter(message -> message.getReceiver().equals(recevierEmail)).toList();
    }

    public List<Message> getAllSentMessagesByUser(String userId) {
        return listOfMessages.stream().filter(message -> message.getSenderId().equals(userId)).toList();
    }

}

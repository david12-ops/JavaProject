package com.example.model;

import com.example.utils.ErrorToolManager;
import com.example.utils.FileConvertor;
import com.example.utils.JsonStorageTool;
import com.example.utils.enums.MessageStatus;
import com.example.utils.services.ValidationService;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageModel {

    static Dotenv dotenv = Dotenv.load();
    private HashMap<String, String> errorMap = new HashMap<>();
    private final ErrorToolManager errorToolManager = new ErrorToolManager(errorMap);
    private final ValidationService validationService = new ValidationService();
    private final ValidationService.MessageModelValidations validator = validationService.new MessageModelValidations(
            errorToolManager);
    private List<Message> listOfMessages;
    private JsonStorageTool<Message> storageTool;

    public MessageModel() {
        storageTool = new JsonStorageTool<Message>(dotenv.get("FILE_PATH_MESSAGES"),
                new TypeReference<List<Message>>() {
                });
        this.listOfMessages = storageTool.getItems();
    }

    public String getError(String errorName) {
        return errorToolManager.getError(errorName);
    }

    public void clearError(String errorName) {
        errorToolManager.removeError(errorName);
    }

    public void addMesssage(UserToken senderToken, String recevierEmail, String subject, String message,
            List<File> files) {

        boolean valid = validator.validMessageData(recevierEmail, subject, message) && validator.validFiles(files);

        if (senderToken == null) {
            errorToolManager.logError(errorToolManager.createErrorBody("addMessage", "Token of sender is required"));
            return;
        }

        if (valid) {
            List<String> convertedFiles = files != null ? files.stream().map(file -> {
                try {
                    return FileConvertor.imageToBase64(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).filter(Objects::nonNull).toList() : null;

            Map<String, List<MessageStatus>> messageStatus = new HashMap<>();
            messageStatus.put(senderToken.getUserId(), List.of(MessageStatus.SENT));
            messageStatus.put(recevierEmail, List.of(MessageStatus.INBOX));

            Message newMessage = new Message(null, senderToken.getUserId(), recevierEmail, subject, message,
                    LocalDateTime.now(), convertedFiles, messageStatus);
            storageTool.addItem(newMessage);
        }
    }

    public void removeMessage(Message message) {
        storageTool.removeItem(message);
    }

    public List<Message> getAllReceviedMessagesByUser(String userId) {
        return listOfMessages.stream().filter(message -> message.getReceiverId().equals(userId)).toList();
    }

    public List<Message> getAllSentMessagesByUser(String userId) {
        return listOfMessages.stream().filter(message -> message.getSenderId().equals(userId)).toList();
    }
}

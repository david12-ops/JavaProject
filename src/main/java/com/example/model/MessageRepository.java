package com.example.model;

import com.example.dto.MessageDTO;
import com.example.utils.FileConvertor;
import com.example.utils.JsonStorageTool;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.MessageStatus;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MessageRepository {
    static Dotenv dotenv = Dotenv.load();
    private List<Message> listOfMessages;
    private JsonStorageTool<Message> storageTool;
    private EnvironmentType environmentType;

    public MessageRepository(EnvironmentType environmentType) {
        this.environmentType = environmentType;
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool = new JsonStorageTool<Message>(dotenv.get("FILE_PATH_MESSAGES"),
                    new TypeReference<List<Message>>() {
                    });
            this.listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            this.listOfMessages = new ArrayList<>();
        }
    }

    private void applyMessageAdding(Message message) {
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.addItem(message);
            listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfMessages.add(message);
        }
    }

    public void setTestData(List<Message> listOfMessages) {
        this.listOfMessages = listOfMessages;
    }

    public List<Message> getTestData() {
        return listOfMessages;
    }

    public void addMessage(UserToken senderToken, String recevierEmail, String subject, String message,
            List<File> files) {
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
        String userKey = statusFrom == MessageStatus.INBOX ? userToken.getMailAccount() : userToken.getUserId();

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

    public List<MessageDTO> getAllMessageDtos() {
        List<MessageDTO> messageDTOs = new ArrayList<>();
        listOfMessages.forEach(message -> {
            messageDTOs.add(new MessageDTO(message.getMessageId(), message.getSenderId(), message.getReceiver(),
                    message.getSubject(), message.getMessage(), message.getTimestamp(), message.getAttachedFiles(),
                    message.getStatuses()));
        });
        return messageDTOs;
    }
}

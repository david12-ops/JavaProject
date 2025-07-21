package com.example.model.repository;

import com.example.dto.MessageDTO;
import com.example.model.Message;
import com.example.model.UserToken;
import com.example.utils.FileConvertor;
import com.example.utils.JsonStorageTool;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.MessageStatus;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
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

    public void addMessage(UserToken senderToken, MessageDTO messageDTO) {
        // String recevier, String subject, String message, List<File> files
        List<File> files = messageDTO.getAttachedFiles();
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
        messageStatuses.put(messageDTO.getReceiver(), Set.of(MessageStatus.INBOX));

        Message newMessage = new Message(null, senderToken.getUserId(), messageDTO.getReceiver(),
                messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(), base64Files,
                messageStatuses);

        applyMessageAdding(newMessage);
    }

    public void removeMessage(MessageDTO messageDTO) {
        // Message message
        Message messageToRemove = new Message(messageDTO.getMessageId(), messageDTO.getSenderId(),
                messageDTO.getReceiver(), messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(),
                messageDTO.getAttachedBase64Files(), messageDTO.getStatuses());
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.removeItem(messageToRemove);
            listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfMessages.remove(messageToRemove);
        }
    }

    public void updateMessageStatus(MessageDTO messageDTO, MessageStatus statusFrom, MessageStatus statusTo,
            UserToken userToken) {
        // Message message, MessageStatus statusFrom, MessageStatus statusTo, UserToken
        // userToken
        String userKey = statusFrom == MessageStatus.INBOX ? userToken.getMailAccount() : userToken.getUserId();

        Set<MessageStatus> currenMessageStatuses = new HashSet<>(
                messageDTO.getStatuses().getOrDefault(userKey, Set.of()));
        currenMessageStatuses.add(statusTo);

        Message messageToUpdate = new Message(messageDTO.getMessageId(), messageDTO.getSenderId(),
                messageDTO.getReceiver(), messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(),
                messageDTO.getAttachedBase64Files(), null);

        messageToUpdate.setStatuses(userKey, currenMessageStatuses);

        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.updateItem(messageToUpdate, messageToUpdate);
            listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfMessages.set(listOfMessages.indexOf(messageToUpdate), messageToUpdate);
        }
    }

    public List<MessageDTO> getAllMessageDtos() {
        List<MessageDTO> messageDTOs = new ArrayList<>();
        listOfMessages.forEach(message -> {
            messageDTOs.add(new MessageDTO(message.getMessageId(), message.getSenderId(), message.getReceiver(),
                    message.getSubject(), message.getMessage(), message.getTimestamp(),
                    message.getAttachedBase64Files(), message.getStatuses(), null));
        });
        return messageDTOs;
    }
}

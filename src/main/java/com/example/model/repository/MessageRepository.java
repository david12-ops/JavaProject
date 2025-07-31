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
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

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

    public void addMessage(UserToken userToken, MessageDTO messageDTO) {
        List<File> files = messageDTO.getAttachedFiles();
        List<String> base64Files = (files != null && !files.isEmpty()) ? files.stream().map(file -> {
            try {
                return FileConvertor.imageToBase64(file);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).toList() : null;

        Message newMessage = new Message(null, userToken.getUserId(), messageDTO.getRecevierId(),
                messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(), base64Files,
                messageDTO.getStatuses());

        applyMessageAdding(newMessage);
    }

    public void removeMessage(MessageDTO messageDTO) {
        Message messageToRemove = new Message(messageDTO.getMessageId(), messageDTO.getSenderId(),
                messageDTO.getRecevierId(), messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(),
                messageDTO.getAttachedBase64Files(), messageDTO.getStatuses());
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.removeItem(messageToRemove);
            listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfMessages.remove(messageToRemove);
        }
    }

    public void updateMessageStatus(MessageDTO messageDTO) {
        // Sem se vrátit pak
        String userKey = statusFrom == MessageStatus.INBOX ? userToken.getMailAccount() : userToken.getUserId();

        EnumSet<MessageStatus> currentMessageStatuses = EnumSet
                .copyOf(messageDTO.getStatuses().getOrDefault(userKey, EnumSet.noneOf(MessageStatus.class)));

        currentMessageStatuses.add(statusTo);

        Message messageToUpdate = new Message(messageDTO.getMessageId(), messageDTO.getSenderId(),
                messageDTO.getRecevierId(), messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(),
                messageDTO.getAttachedBase64Files(), null);

        messageToUpdate.setStatuses(userKey, currentMessageStatuses);

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
            messageDTOs.add(new MessageDTO(message.getMessageId(), message.getSenderId(), null, message.getRecevierId(),
                    null, message.getSubject(), message.getMessage(), message.getTimestamp(),
                    message.getAttachedBase64Files(), message.getStatuses(), null));
        });
        return messageDTOs;
    }
}

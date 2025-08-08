package com.example.model.repository;

import com.example.dto.MessageDTO;
import com.example.model.Message;
import com.example.utils.FileConvertor;
import com.example.utils.JsonStorageTool;
import com.example.utils.enums.EnvironmentType;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    private void applyMessageUpdate(Message currentMessage, Message updatedMessage) {
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.updateItem(currentMessage, updatedMessage);
            listOfMessages = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            int index = listOfMessages.indexOf(currentMessage);
            if (index >= 0) {
                listOfMessages.set(index, updatedMessage);
            }
        }
    }

    public void setTestData(List<Message> listOfMessages) {
        this.listOfMessages = listOfMessages;
    }

    public List<Message> getTestData() {
        return listOfMessages;
    }

    public void addMessage(MessageDTO messageDTO) {
        List<File> files = messageDTO.getAttachedFiles();
        List<String> base64Files = (files != null && !files.isEmpty()) ? files.stream().map(file -> {
            try {
                return FileConvertor.fileToBase64(file);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).toList() : null;

        Message newMessage = new Message(null, messageDTO.getSenderId(), messageDTO.getRecevierId(),
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

    public void updateMessageStatus(MessageDTO currentMessageDTO, MessageDTO updadMessageDTO) {
        Message currentMessage = new Message(currentMessageDTO.getMessageId(), currentMessageDTO.getSenderId(),
                currentMessageDTO.getRecevierId(), currentMessageDTO.getSubject(), currentMessageDTO.getMessage(),
                currentMessageDTO.getTimestamp(), currentMessageDTO.getAttachedBase64Files(),
                currentMessageDTO.getStatuses());
        Message updatedMessage = new Message(updadMessageDTO.getMessageId(), updadMessageDTO.getSenderId(),
                updadMessageDTO.getRecevierId(), updadMessageDTO.getSubject(), updadMessageDTO.getMessage(),
                updadMessageDTO.getTimestamp(), updadMessageDTO.getAttachedBase64Files(),
                updadMessageDTO.getStatuses());

        applyMessageUpdate(currentMessage, updatedMessage);
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

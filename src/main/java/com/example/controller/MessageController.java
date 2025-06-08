package com.example.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Message;
import com.example.model.MessageRepository;
import com.example.model.UserToken;
import com.example.utils.enums.MessageStatus;

public class MessageController {
    private MessageRepository MessageRegister;

    public MessageController(MessageRepository MessageRegister) {
        this.MessageRegister = MessageRegister;
    }

    public String getError(String errorName) {
        return MessageRegister.getError(errorName);
    }

    public void clearError(String errorName) {
        MessageRegister.clearError(errorName);
    }

    public void addMessage(UserToken senderToken, String recevierEmail, String subject, String message,
            List<File> files) {
        MessageRegister.addMessage(senderToken, recevierEmail, subject, message, files);
    }

    public void responseToMessage() {
        throw new UnsupportedOperationException("Unimplemented method 'responseToMessage'");
    }

    public void removeMessage(Message message) {
        MessageRegister.removeMessage(message);
    }

    public List<Message> getMessages(MessageStatus type, UserToken userToken) {
        List<Message> messages = new ArrayList<>();

        if (type == MessageStatus.SENT && userToken != null) {
            messages = MessageRegister.getAllSentMessagesByUser(userToken.getUserId());
        }

        if (type == MessageStatus.INBOX && userToken != null) {
            messages = MessageRegister.getAllReceviedMessagesByUser(userToken.getMailAccount());
        }

        return messages;
    }

}

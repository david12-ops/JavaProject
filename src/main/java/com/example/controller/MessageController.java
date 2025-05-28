package com.example.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Message;
import com.example.model.MessageModel;
import com.example.model.UserToken;
import com.example.utils.enums.MessageStatus;

public class MessageController {
    private MessageModel messageModel;

    public MessageController(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public String getError(String errorName) {
        return messageModel.getError(errorName);
    }

    public void clearError(String errorName) {
        messageModel.clearError(errorName);
    }

    public void addMessage(UserToken senderToken, String recevierEmail, String subject, String message,
            List<File> files) {
        messageModel.addMessage(senderToken, recevierEmail, subject, message, files);
    }

    public void responseToMessage() {
        throw new UnsupportedOperationException("Unimplemented method 'responseToMessage'");
    }

    public void removeMessage(Message message) {
        messageModel.removeMessage(message);
    }

    public List<Message> getMessages(MessageStatus type, UserToken userToken) {
        List<Message> messages = new ArrayList<>();

        if (type == MessageStatus.SENT && userToken != null) {
            messages = messageModel.getAllSentMessagesByUser(userToken.getUserId());
        }

        if (type == MessageStatus.INBOX && userToken != null) {
            messages = messageModel.getAllReceviedMessagesByUser(userToken.getUserId());
        }

        return messages;
    }

}

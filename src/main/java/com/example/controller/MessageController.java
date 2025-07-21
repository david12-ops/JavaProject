package com.example.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Message;
import com.example.model.UserToken;
import com.example.model.repository.MessageRepository;
import com.example.utils.enums.MessageStatus;
import com.example.utils.services.MailboxService;

public class MessageController {
    private MessageRepository messageRegister;
    private final MailboxService mailboxService = MailboxService.getInstance();

    public MessageController(MessageRepository MessageRegister) {
        this.messageRegister = MessageRegister;
    }

    public String getError(String errorName) {
        return mailboxService.getErrorHandler().getError(errorName);
    }

    public void clearError(String errorName) {
        mailboxService.getErrorHandler().removeError(errorName);
    }

    public void addMessage(UserToken senderToken, String recevierId, String subject, String message, List<File> files) {
        messageRegister.addMessage(senderToken, recevierId, subject, message, files);
    }

    public void responseToMessage() {
        throw new UnsupportedOperationException("Unimplemented method 'responseToMessage'");
    }

    public void removeMessage(Message message) {
        messageRegister.removeMessage(message);
    }

    public List<Message> getMessages(MessageStatus type, UserToken userToken) {
        List<Message> messages = new ArrayList<>();

        // if (type == MessageStatus.SENT && userToken != null) {
        // messages = messageRegister.getAllSentMessagesByUser(userToken.getUserId());
        // }

        // if (type == MessageStatus.INBOX && userToken != null) {
        // messages =
        // messageRegister.getAllReceviedMessagesByUser(userToken.getMailAccount());
        // }

        return messages;
    }
}

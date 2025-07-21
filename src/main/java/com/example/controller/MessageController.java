package com.example.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import com.example.dto.MessageDTO;
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
        messageRegister.addMessage(senderToken,
                new MessageDTO(null, null, recevierId, subject, message, LocalDateTime.now(), null, null, files));
    }

    public void responseToMessage() {
        throw new UnsupportedOperationException("Unimplemented method 'responseToMessage'");
    }

    public void removeMessage(MessageDTO messageDTO) {
        messageRegister.removeMessage(messageDTO);
    }

    public List<MessageDTO> getMessages(MessageStatus type, UserToken userToken) {
        return messageRegister.getAllMessageDtos();
    }
}

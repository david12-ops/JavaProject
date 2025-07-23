package com.example.controller;

import java.io.File;
import java.util.List;

import com.example.dto.MessageDTO;
import com.example.model.UserToken;
import com.example.utils.enums.MessageStatus;
import com.example.utils.services.MailboxService;

public class MessageController {
    /*
     * controlers will not have direct access to register because of limitations
     * that come with this do factory to repository to have shred one instance
     * across services (optional private constructor)
     */
    private final MailboxService mailboxService = new MailboxService();

    public MessageController() {
    }

    public String getError(String errorName) {
        return mailboxService.getErrorHandler().getError(errorName);
    }

    public void clearError(String errorName) {
        mailboxService.getErrorHandler().removeError(errorName);
    }

    public void sendMessage(UserToken senderToken, String recevierEmail, String subject, String message,
            List<File> files) {
        mailboxService.sendMessage(senderToken, recevierEmail, subject, message, files);
    }

    public void responseToMessage() {
        throw new UnsupportedOperationException("Unimplemented method 'responseToMessage'");
    }

    public void updateStatus() {
        mailboxService.updateStatus();
    }

    public void removeMessage(MessageDTO messageDTO) {
        mailboxService.removeMessage(messageDTO);
    }

    public List<MessageDTO> getMessages(MessageStatus type, UserToken userToken) {
        return mailboxService.getMessageDTOs(userToken, type);
    }
}

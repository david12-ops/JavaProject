package com.example.controller;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import com.example.dto.MessageDTO;
import com.example.model.UserToken;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.OperationType;
import com.example.utils.services.MailboxService;

public class MessageController {
    private final MailboxService mailboxService = new MailboxService(EnvironmentType.PRODUCTION);

    public MessageController() {
    }

    public String getError(String errorName) {
        return mailboxService.getErrorHandler().getError(errorName);
    }

    public void clearError(String errorName) {
        mailboxService.getErrorHandler().removeError(errorName);
    }

    public void sendMessage(UserToken userToken, String recevierEmail, String subject, String message,
            List<File> files) {
        mailboxService.sendMessage(userToken, recevierEmail, subject, message, files);
    }

    public void responseToMessage() {
        throw new UnsupportedOperationException("Unimplemented method 'responseToMessage'");
    }

    public void updateMessageStatus(UserToken userToken, MessageDTO messageDTO, MessageStatus newStatus) {
        mailboxService.updateStatus(userToken, messageDTO, newStatus, OperationType.UPDATE);
    }

    public void removeMessageStatus(UserToken userToken, MessageDTO messageDTO, MessageStatus messageStatusToRemove) {
        mailboxService.updateStatus(userToken, messageDTO, messageStatusToRemove, OperationType.REMOVE);
    }

    public void removeMessage(UserToken userToken, MessageDTO messageDTO) {
        mailboxService.removeMessage(userToken, messageDTO);
    }

    public List<MessageDTO> getMessages(EnumSet<MessageStatus> messageStatusesFromUI, UserToken userToken) {
        return mailboxService.getMessageDTOs(userToken, messageStatusesFromUI);
    }
}

package com.example.utils.interfaces;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import com.example.dto.MessageDTO;
import com.example.model.UserToken;
import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.OperationType;

public interface MailService {
    void sendMessage(UserToken userToken, String recevierId, String subject, String message, List<File> files);

    void updateStatus(UserToken userToken, MessageDTO messageDTO, MessageStatus status, OperationType operationType);

    void removeMessage(UserToken userToken, MessageDTO messageDTO);

    List<MessageDTO> getMessageDTOs(UserToken userToken, EnumSet<MessageStatus> messageStatuses);

    ErrorHandler getErrorHandler();
}

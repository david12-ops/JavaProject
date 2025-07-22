package com.example.utils.interfaces;

import java.io.File;
import java.util.List;

import com.example.dto.MessageDTO;
import com.example.model.UserToken;

public interface MailService {
    void sendMessage(UserToken senderToken, String recevierId, String subject, String message, List<File> files);

    void updateStatus();

    void removeMessage(MessageDTO messageDTO);

    ErrorHandler getErrorHandler();
}

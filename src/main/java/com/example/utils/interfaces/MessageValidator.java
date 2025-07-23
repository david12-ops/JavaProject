package com.example.utils.interfaces;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.model.Message;
import com.example.utils.enums.MessageStatus;

public interface MessageValidator {
    boolean validFiles(List<File> files);

    boolean validMessageData(String whom, String subject, String message);

    boolean containsOnlySupportedStatuses(Map<String, Set<MessageStatus>> messageStatuses,
            List<MessageStatus> expectedMessageStatus);

    boolean isStatusUpdateAllowed(Message message, MessageStatus newStatus, String userKey);
}

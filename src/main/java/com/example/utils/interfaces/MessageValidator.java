package com.example.utils.interfaces;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.EnumSet;

import com.example.utils.enums.MessageStatus;

public interface MessageValidator {
    boolean validFiles(List<File> files);

    boolean validMessageData(String whom, String subject, String message);

    boolean containsOnlyAllowedStatuses(Map<String, EnumSet<MessageStatus>> messageStatuses,
            EnumSet<MessageStatus> expectedMessageStatus);

    boolean isStatusUpdateAllowed(EnumSet<MessageStatus> messageStatuses, MessageStatus newStatus);
}

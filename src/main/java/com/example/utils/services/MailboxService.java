package com.example.utils.services;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.model.Message;
import com.example.utils.ValidationContext;
import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.ValidationMode;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.MailService;
import com.example.utils.interfaces.MessageValidator;

public class MailboxService implements MailService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.MESSAGE);
    private ErrorHandler errorHandler;
    private MessageValidator messageValidator;
    private Map<MessageStatus, Set<MessageStatus>> allowedByStatus;

    private void defineMessageStatusTransition() {
        Map<MessageStatus, Set<MessageStatus>> map = new HashMap<>();

        map.put(MessageStatus.INBOX, EnumSet.of(MessageStatus.TRASH, MessageStatus.STARRED, MessageStatus.SNOOZED));
        map.put(MessageStatus.SENT, EnumSet.of(MessageStatus.TRASH, MessageStatus.STARRED, MessageStatus.SNOOZED));
        map.put(MessageStatus.STARRED, EnumSet.of(MessageStatus.TRASH, MessageStatus.SNOOZED));

        map.put(MessageStatus.SNOOZED, Set.of());
        map.put(MessageStatus.DRAFTS, Set.of());
        map.put(MessageStatus.TRASH, Set.of());

        allowedByStatus = Collections.unmodifiableMap(map);
    }

    private boolean isStatusUpdateAllowed(Message message, MessageStatus newStatus, String userKey) {
        Set<MessageStatus> messageStatuses = message.getStatuses().get(userKey);
        for (MessageStatus status : messageStatuses) {
            if (!allowedByStatus.get(status).contains(newStatus)) {
                return false;
            }
        }

        return true;
    }

    private boolean containsOnlySupportedStatuses(Map<String, Set<MessageStatus>> messageStatuses,
            List<MessageStatus> expectedMessageStatus) {
        for (Set<MessageStatus> statuses : messageStatuses.values()) {
            for (MessageStatus status : statuses) {
                if (!expectedMessageStatus.contains(status)) {
                    return false;
                }
            }
        }

        return true;
    }

    public MailboxService() {
        this.errorHandler = validationContext.getUserValidationBundle().getErrorManager();
        this.messageValidator = validationContext.getMessageValidationBundle().getValidator();
        defineMessageStatusTransition();
    }

    // public List<Message> getAllReceviedMessagesByUser(String recevierEmail) {
    // return listOfMessages.stream().filter(message ->
    // message.getReceiver().equals(recevierEmail)).toList();
    // }

    // public List<Message> getAllSentMessagesByUser(String userId) {
    // return listOfMessages.stream().filter(message ->
    // message.getSenderId().equals(userId)).toList();
    // }
}

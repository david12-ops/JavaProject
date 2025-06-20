package com.example.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.ViewLevel;

public class MessageDTO {
    private String messageId;

    private String senderId;

    private Map<String, Set<MessageStatus>> statuses;

    private String receiver;

    private String subject;

    private String message;

    private List<String> attachedFiles;

    private LocalDateTime timestamp;

    public MessageDTO() {
    }

    public MessageDTO(String messageId, String senderId, String receiver, String subject, String message,
            LocalDateTime timestamp, List<String> attachedFiles, Map<String, Set<MessageStatus>> statuses) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiver = receiver;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.statuses = statuses;
        this.attachedFiles = attachedFiles;
    }

    public void sanitize(ViewLevel level) {
        if (level == ViewLevel.PUBLIC) {
            this.receiver = null;
            this.messageId = null;
            this.senderId = null;
            this.timestamp = null;
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Map<String, Set<MessageStatus>> getStatuses() {
        return statuses;
    }

    public void setStatuses(Map<String, Set<MessageStatus>> statuses) {
        this.statuses = statuses;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<String> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

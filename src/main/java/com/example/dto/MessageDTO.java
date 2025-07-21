package com.example.dto;

import java.io.File;
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

    private List<String> attachedBase64Files;

    private List<File> attachedFiles;

    private LocalDateTime timestamp;

    public MessageDTO() {
    }

    public MessageDTO(String messageId, String senderId, String receiver, String subject, String message,
            LocalDateTime timestamp, List<String> attachedBase64Files, Map<String, Set<MessageStatus>> statuses,
            List<File> attachedFiles) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiver = receiver;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.statuses = statuses;
        this.attachedBase64Files = attachedBase64Files;
        this.attachedFiles = attachedFiles;
    }

    public void sanitize(ViewLevel level) {
        if (level == ViewLevel.PUBLIC) {
            // ?
            this.receiver = null;
            this.messageId = null;
            this.senderId = null;
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

    public List<String> getAttachedBase64Files() {
        return attachedBase64Files;
    }

    public void setAttachedBase64Files(List<String> attachedBase64Files) {
        this.attachedBase64Files = attachedBase64Files;
    }

    public List<File> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<File> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

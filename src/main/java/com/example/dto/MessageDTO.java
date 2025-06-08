package com.example.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.utils.enums.MessageStatus;

public class MessageDTO {

    private Map<String, Set<MessageStatus>> statuses;

    private String receiver;

    private String subject;

    private String message;

    private List<String> attachedFiles;

    public MessageDTO() {
    }

    public MessageDTO(Map<String, Set<MessageStatus>> statuses, String receiver, String subject, String message,
            List<String> attachedFiles) {
        this.statuses = statuses;
        this.receiver = receiver;
        this.subject = subject;
        this.message = message;
        this.attachedFiles = attachedFiles;
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
}

package com.example.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.example.utils.enums.MessageStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {

    @JsonProperty("message_id")
    private final String messageId;

    @JsonProperty("sender_id")
    private final String senderId;

    @JsonProperty("statuses")
    private Map<String, Set<MessageStatus>> statuses;

    @JsonProperty("recevier")
    private String receiver;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("message")
    private String message;

    @JsonProperty("attached_files")
    private List<String> attachedFiles;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonCreator
    public Message(@JsonProperty("message_id") String messageId, @JsonProperty("sender_id") String senderId) {
        this.messageId = messageId;
        this.senderId = senderId;
    }

    public Message(String messageId, String senderId, String receiver, String subject, String message,
            LocalDateTime timestamp, List<String> attachedFiles, Map<String, Set<MessageStatus>> statuses) {
        this.messageId = (messageId == null || messageId.isBlank()) ? UUID.randomUUID().toString() : messageId;
        this.senderId = senderId;
        this.receiver = receiver;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.statuses = statuses;
        this.attachedFiles = attachedFiles;
    }

    /*
     * This method determines how the objects will be compared.
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Message message = (Message) obj;
        return messageId.equals(message.messageId);
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<String> getAttachedFiles() {
        return attachedFiles;
    }

    public Map<String, Set<MessageStatus>> getStatuses() {
        return statuses;
    }

    public void setStatuses(String key, Set<MessageStatus> statuses) {
        this.statuses.put(key, statuses);
    }

    @Override
    public String toString() {
        return "Message{" + "messageId=" + messageId + ", senderId=" + senderId + ", receiver=" + receiver
                + ", message='" + message + '\'' + ", timestamp=" + timestamp + '}';
    }
}

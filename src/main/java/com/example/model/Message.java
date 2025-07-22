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

    @JsonProperty("recevierId")
    private String receiverId;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("message")
    private String message;

    @JsonProperty("attached_files")
    private List<String> attachedBase64Files;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonCreator
    public Message(@JsonProperty("message_id") String messageId, @JsonProperty("sender_id") String senderId) {
        this.messageId = (messageId == null || messageId.isBlank()) ? UUID.randomUUID().toString() : messageId;
        this.senderId = senderId;
    }

    public Message(String messageId, String senderId, String receiverId, String subject, String message,
            LocalDateTime timestamp, List<String> attachedBase64Files, Map<String, Set<MessageStatus>> statuses) {
        this.messageId = (messageId == null || messageId.isBlank()) ? UUID.randomUUID().toString() : messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.statuses = statuses;
        this.attachedBase64Files = attachedBase64Files;
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

    public String getRecevierId() {
        return receiverId;
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

    public List<String> getAttachedBase64Files() {
        return attachedBase64Files;
    }

    public Map<String, Set<MessageStatus>> getStatuses() {
        return statuses;
    }

    public void setStatuses(String key, Set<MessageStatus> statuses) {
        this.statuses.put(key, statuses);
    }

    @Override
    public String toString() {
        return "Message{" + "messageId=" + messageId + ", senderId=" + senderId + ", receiverId=" + receiverId
                + ", message='" + message + '\'' + ", timestamp=" + timestamp + '}';
    }
}

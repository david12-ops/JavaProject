package com.example.dto;

import java.io.File;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.ViewLevel;

public class MessageDTO {
    private String messageId;

    private String senderId;

    private String senderMailAccount;

    private Map<String, EnumSet<MessageStatus>> statuses;

    private String recevierId;

    private String recevierMailAccount;

    private String subject;

    private String message;

    private List<String> attachedBase64Files;

    private List<File> attachedFiles;

    private LocalDateTime timestamp;

    public MessageDTO(String messageId, String senderId, String senderMailAccount, String recevierId,
            String recevierMailAccount, String subject, String message, LocalDateTime timestamp,
            List<String> attachedBase64Files, Map<String, EnumSet<MessageStatus>> statuses, List<File> attachedFiles) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderMailAccount = senderMailAccount;
        this.recevierId = recevierId;
        this.recevierMailAccount = recevierMailAccount;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.statuses = statuses;
        this.attachedBase64Files = attachedBase64Files;
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
        MessageDTO messageDTO = (MessageDTO) obj;
        return messageId.equals(messageDTO.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    public void sanitize(ViewLevel level) {
        if (level == ViewLevel.PUBLIC) {
            this.recevierId = null;
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

    public String getSenderMailAccount() {
        return senderMailAccount;
    }

    public Map<String, EnumSet<MessageStatus>> getStatuses() {
        return statuses;
    }

    public void setStatuses(String key, EnumSet<MessageStatus> statuses) {
        this.statuses.put(key, statuses);
    }

    public String getRecevierId() {
        return recevierId;
    }

    public String getRecevierMailAccount() {
        return recevierMailAccount;
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

    @Override
    public String toString() {
        return "MessageDTO{" + "messageId='" + messageId + '\'' + ", senderId='" + senderId + '\''
                + ", senderMailAccount='" + senderMailAccount + '\'' + ", recevierId='" + recevierId + '\''
                + ", recevierMailAccount='" + recevierMailAccount + '\'' + ", subject='" + subject + '\''
                + ", message='"
                + (message != null ? message.substring(0, Math.min(30, message.length())) + "..." : null) + '\''
                + ", timestamp=" + timestamp + ", statuses=" + statuses + ", attachedBase64FilesCount="
                + (attachedBase64Files != null ? attachedBase64Files.size() : 0) + ", attachedFilesCount="
                + (attachedFiles != null ? attachedFiles.size() : 0) + '}';
    }

}

package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class User {
    @JsonProperty("user_id")
    private final String userId;

    @JsonProperty("group_id")
    private final String groupId;

    @JsonProperty("mailAccount")
    private String mailAccount;

    @JsonProperty("password")
    private String password;

    @JsonProperty("profileImage")
    private String profileImage;

    @JsonCreator
    public User(@JsonProperty("user_id") String userId, @JsonProperty("group_id") String groupId) {
        this.userId = (userId == null || userId.isBlank()) ? UUID.randomUUID().toString() : userId;
        this.groupId = (groupId == null || groupId.isBlank()) ? UUID.randomUUID().toString() : groupId;
    }

    public User(String userId, String groupId, String mailAccount, String password, String profileImage) {
        this.mailAccount = mailAccount;
        this.password = password;
        this.userId = (userId == null || userId.isBlank()) ? UUID.randomUUID().toString() : userId;
        this.groupId = (groupId == null || groupId.isBlank()) ? UUID.randomUUID().toString() : groupId;
        this.profileImage = null;
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
        User user = (User) obj;
        return userId.equals(user.userId);
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getMailAccount() {
        return mailAccount;
    }

    public String getPassword() {
        return password;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setMailAccount(String mailAccount) {
        this.mailAccount = mailAccount;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    @Override
    public String toString() {
        return "User{" + "userId='" + userId + '\'' + ", groupId='" + groupId + '\'' + ", mailAccount='" + mailAccount
                + '}';
    }
}

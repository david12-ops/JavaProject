package com.example.dto;

import com.example.utils.enums.ViewLevel;

public class UserDTO {
    private String userId;

    private String groupId;

    private String mailAccount;

    private String currentPassword;

    private String password;

    private String confirmPassword;

    private String profileImage;

    public UserDTO() {
    }

    public UserDTO(String userId, String groupId, String mailAccount, String currentPassword, String password,
            String confirmPassword, String profileImage) {
        this.userId = userId;
        this.groupId = groupId;
        this.mailAccount = mailAccount;
        this.currentPassword = currentPassword;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.profileImage = profileImage;
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
        UserDTO userDTO = (UserDTO) obj;
        return userId.equals(userDTO.userId);
    }

    public void sanitize(ViewLevel level) {
        if (level == ViewLevel.PUBLIC) {
            this.currentPassword = null;
            this.groupId = null;
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMailAccount() {
        return mailAccount;
    }

    public void setMailAccount(String mailAccount) {
        this.mailAccount = mailAccount;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

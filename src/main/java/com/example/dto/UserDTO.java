package com.example.dto;

public class UserDTO {

    private String mailAccount;

    private String currentPassword;

    private String confirmPassword;

    private String profileImage;

    public UserDTO() {
    }

    public UserDTO(String mailAccount, String currentPassword, String confirmPassword, String profileImage) {
        this.mailAccount = mailAccount;
        this.currentPassword = currentPassword;
        this.confirmPassword = confirmPassword;
        this.profileImage = profileImage;
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
}

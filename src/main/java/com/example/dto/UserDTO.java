package com.example.dto;

public class UserDTO {

    private String mailAccount;

    private String currentPassword;

    private String password;

    private String confirmPassword;

    private String profileImage;

    public UserDTO() {
    }

    public UserDTO(String mailAccount, String currentPassword, String password, String confirmPassword,
            String profileImage) {
        this.mailAccount = mailAccount;
        this.currentPassword = currentPassword;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

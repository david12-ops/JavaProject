package com.example.model;

public class UserToken {
    private final String mailAccount;
    private final String groupId;
    private final String userId;

    public UserToken(String userId, String groupId, String mailAccount) {
        this.userId = userId;
        this.groupId = groupId;
        this.mailAccount = mailAccount;
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
        UserToken userToken = (UserToken) obj;
        return userId.equals(userToken.userId);
    }

    public String getMailAccount() {
        return mailAccount;
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupId() {
        return groupId;
    }
}

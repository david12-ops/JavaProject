package com.example.controller;

import java.io.File;
import java.util.List;

import com.example.model.User;
import com.example.model.UserModel;
import com.example.model.UserToken;
import com.example.utils.FileConvertor;
import com.example.utils.enums.AddTypeOperation;
import com.example.utils.enums.Form;
import com.example.utils.enums.GetUserTypeOperation;
import com.example.utils.interfaces.AuthManagement;
import com.example.utils.interfaces.UserManagement;
import com.example.utils.services.SessionService;

import javafx.scene.image.Image;

public class UserController implements AuthManagement, UserManagement {
    private UserModel userModel;
    private SessionService sessionService;
    private String currentSessionId;

    public UserController(UserModel userModel) {
        this.userModel = userModel;
        this.sessionService = SessionService.getInstance();
    }

    private User getUser(String emailAccount, String password, GetUserTypeOperation getUserTypeOperation) {
        if (getUserTypeOperation == GetUserTypeOperation.BYTOKEN) {
            UserToken userToken = getLoggedUser();
            return userModel.getUserByCredentials(null, null, userToken);
        }

        if (getUserTypeOperation == GetUserTypeOperation.BYCREDENTIALS) {
            return userModel.getUserByCredentials(emailAccount, password, null);
        }
        return null;
    }

    public String getError(String errorName) {
        return userModel.getError(errorName);
    }

    public void clearError(String errorName) {
        userModel.clearError(errorName);
    }

    // Auth
    @Override
    public boolean register(String emailAccount, String password, String confirmationPassword) {
        return userModel.addUser(emailAccount, password, confirmationPassword, null, AddTypeOperation.NEWACCOUNT,
                Form.REGISTER);
    }

    @Override
    public void login(String emailAccount, String password) {
        User user = getUser(emailAccount, password, GetUserTypeOperation.BYCREDENTIALS);

        if (user != null && !sessionService.isUserLoggedIn(user.getUserId())) {
            currentSessionId = sessionService.createSessionId(user);
        }
    }

    @Override
    public boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationNewPassword) {

        User foundUser = getUser(emailAccount, password, GetUserTypeOperation.BYCREDENTIALS);
        return userModel.updateUser(foundUser, newPassword, confirmationNewPassword, Form.FORGOTCREDENTIALS);
    }

    @Override
    public void logOut() {
        sessionService.removeSession(currentSessionId);
        currentSessionId = null;
    }

    @Override
    public UserToken getLoggedUser() {
        return sessionService.getUserTokenBySessionId(currentSessionId);
    }

    @Override
    public Image getImageProfile() {
        User user = getUser(null, null, GetUserTypeOperation.BYTOKEN);

        if (user != null && user.getProfileImage() != null) {
            return FileConvertor.Base64ToImage(user.getProfileImage());
        }
        return null;
    }

    // User actions
    @Override
    public boolean removeAccount(User user) {
        UserToken userToken = getLoggedUser();
        return userModel.removeUser(userToken, user);
    }

    @Override
    public boolean updateLoggedInAccount(String newPassword, String confirmationNewPassword) {
        UserToken userToken = getLoggedUser();
        return userModel.updateUser(userToken, newPassword, confirmationNewPassword, Form.FORGOTCREDENTIALS);
    }

    @Override
    public void updateImageProfile(File file) {
        UserToken userToken = getLoggedUser();
        if (file == null) {
            userModel.updateUser(userToken, null);

        } else {
            userModel.updateUser(userToken, file);
        }
    }

    @Override
    public boolean addAnotherAccount(String emailAccount, String password, String confirmationPassword) {
        UserToken userToken = getLoggedUser();
        return userModel.addUser(emailAccount, password, confirmationPassword, userToken,
                AddTypeOperation.ANOTHERACCOUNT, Form.ADDACCOUNT);
    }

    @Override
    public boolean switchAccount(User switchtoUser) {
        UserToken userToken = getLoggedUser();

        if (userToken != null && switchtoUser != null
                && !userToken.getMailAccount().equals(switchtoUser.getMailAccount())) {
            logOut();
            currentSessionId = sessionService.createSessionId(switchtoUser);
            return true;
        }

        return false;
    }

    @Override
    public List<User> getAllUserAccounts() {
        UserToken userToken = getLoggedUser();
        List<User> users = userModel.getAllUserAccounts(userToken);

        if (users == null || users.size() == 0) {
            return List.of();
        }

        return users.stream().filter(user -> !user.getUserId().equals(userToken.getUserId())
                && !user.getMailAccount().equals(userToken.getMailAccount())).toList();
    }
}

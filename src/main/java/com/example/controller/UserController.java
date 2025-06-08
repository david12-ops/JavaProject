package com.example.controller;

import java.io.File;
import java.util.List;

import com.example.model.User;
import com.example.model.UserRepository;
import com.example.model.UserToken;
import com.example.utils.FileConvertor;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.FormType;
import com.example.utils.enums.GetUserOperationType;
import com.example.utils.interfaces.AuthService;
import com.example.utils.interfaces.AccountService;
import com.example.utils.services.SessionService;

import javafx.scene.image.Image;

public class UserController implements AuthService, AccountService {
    private UserRepository userRepository;
    private SessionService sessionService;
    private String currentSessionId;

    public UserController(UserRepository UserRepository) {
        this.userRepository = UserRepository;
        this.sessionService = SessionService.getInstance();
    }

    private User getUser(String emailAccount, String password, GetUserOperationType getUserTypeOperation) {
        if (getUserTypeOperation == GetUserOperationType.BYTOKEN) {
            UserToken userToken = getLoggedUser();
            return userRepository.getUserByCredentials(null, null, userToken);
        }

        if (getUserTypeOperation == GetUserOperationType.BYCREDENTIALS) {
            return userRepository.getUserByCredentials(emailAccount, password, null);
        }
        return null;
    }

    public String getError(String errorName) {
        return userRepository.getError(errorName);
    }

    public void clearError(String errorName) {
        userRepository.clearError(errorName);
    }

    // Auth
    @Override
    public boolean register(String emailAccount, String password, String confirmationPassword) {
        return userRepository.addUser(emailAccount, password, confirmationPassword, null, AddOperationType.NEWACCOUNT,
                FormType.REGISTER);
    }

    @Override
    public void login(String emailAccount, String password) {
        User user = getUser(emailAccount, password, GetUserOperationType.BYCREDENTIALS);

        if (user != null && !sessionService.isUserLoggedIn(user.getUserId())) {
            currentSessionId = sessionService.createSessionId(user);
        }
    }

    @Override
    public boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationNewPassword) {

        User foundUser = getUser(emailAccount, password, GetUserOperationType.BYCREDENTIALS);
        return userRepository.updateUser(foundUser, newPassword, confirmationNewPassword, FormType.FORGOTCREDENTIALS);
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
        User user = getUser(null, null, GetUserOperationType.BYTOKEN);

        if (user != null && user.getProfileImage() != null) {
            return FileConvertor.Base64ToImage(user.getProfileImage());
        }
        return null;
    }

    // User actions
    @Override
    public boolean removeAccount(User user) {
        UserToken userToken = getLoggedUser();
        return userRepository.removeUser(userToken, user);
    }

    @Override
    public boolean updateLoggedInAccount(String newPassword, String confirmationNewPassword) {
        UserToken userToken = getLoggedUser();
        return userRepository.updateUser(userToken, newPassword, confirmationNewPassword, FormType.FORGOTCREDENTIALS);
    }

    @Override
    public void updateImageProfile(File file) {
        UserToken userToken = getLoggedUser();
        if (file == null) {
            userRepository.updateUser(userToken, null);

        } else {
            userRepository.updateUser(userToken, file);
        }
    }

    @Override
    public boolean addAnotherAccount(String emailAccount, String password, String confirmationPassword) {
        UserToken userToken = getLoggedUser();
        return userRepository.addUser(emailAccount, password, confirmationPassword, userToken,
                AddOperationType.ANOTHERACCOUNT, FormType.ADDACCOUNT);
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
        List<User> users = userRepository.getAllUserAccounts(userToken);

        if (users == null || users.size() == 0) {
            return List.of();
        }

        return users.stream().filter(user -> !user.getUserId().equals(userToken.getUserId())
                && !user.getMailAccount().equals(userToken.getMailAccount())).toList();
    }
}

package com.example.controller;

import java.io.File;
import java.util.List;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.FormType;
import com.example.utils.interfaces.AuthService;
import com.example.utils.interfaces.AccountService;
import com.example.utils.services.SessionService;
import com.example.utils.services.UserAccountService;
import com.example.utils.services.UserAuthService;

import javafx.scene.image.Image;

public class UserController {
    private final AuthService authService = new UserAuthService(SessionService.getInstance());
    private final AccountService accountService = new UserAccountService();

    public UserController() {
    }

    public String getAuthError(String errorName) {
        return authService.getErrorHandler().getError(errorName);
    }

    public void clearAuthError(String errorName) {
        authService.getErrorHandler().removeError(errorName);
    }

    public String getAccountError(String errorName) {
        return accountService.getErrorHandler().getError(errorName);
    }

    public void clearAccountError(String errorName) {
        accountService.getErrorHandler().removeError(errorName);
    }

    // Auth
    public boolean register(String emailAccount, String password, String confirmationPassword) {
        return authService.register(emailAccount, password, confirmationPassword, FormType.REGISTER,
                AddOperationType.NEWACCOUNT);
    }

    public void login(String emailAccount, String password) {
        authService.login(emailAccount, password);
    }

    public boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationNewPassword) {

        return authService.updateNotLoggedAccount(emailAccount, password, newPassword, confirmationNewPassword,
                FormType.FORGOTCREDENTIALS);
    }

    public void logOut() {
        authService.logOut();
    }

    public UserToken getLoggedUser() {
        return authService.getLoggedUser();
    }

    public Image getImageProfile() {
        return accountService.getImageProfile(getLoggedUser());
    }

    // User actions
    public boolean removeAccount(UserDTO userDTO) {
        return accountService.removeAccount(getLoggedUser(), userDTO);
    }

    public boolean updateLoggedInAccount(String newPassword, String confirmationNewPassword) {
        return authService.updateLoggedInAccount(newPassword, confirmationNewPassword, FormType.FORGOTCREDENTIALS);
    }

    public void updateImageProfile(File file) {
        accountService.updateImageProfile(getLoggedUser(), file);
    }

    public boolean addAnotherAccount(String emailAccount, String password, String confirmationPassword) {
        return authService.register(emailAccount, password, confirmationPassword, FormType.ADDACCOUNT,
                AddOperationType.ANOTHERACCOUNT);
    }

    public boolean switchAccount(UserDTO switchToUserDTO) {
        return authService.switchAccount(switchToUserDTO);
    }

    public List<UserDTO> getAllUserAccounts() {
        return accountService.getAllUserAccounts(getLoggedUser());
    }
}

package com.example.utils.interfaces;

import java.util.List;

import com.example.model.User;
import com.example.model.UserToken;

public interface AuthService {

    boolean register(String emailAccount, String password, String confirmationPassword);

    void login(String emailAccount, String password);

    void logOut();

    UserToken getLoggedUser();

    List<User> getAllUserAccounts();

    boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationPassword);

    boolean updateLoggedInAccount(String newPassword, String confirmationPassword);
}

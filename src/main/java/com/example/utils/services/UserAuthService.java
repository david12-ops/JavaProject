package com.example.utils.services;

import com.example.model.UserToken;
import com.example.utils.interfaces.AuthService;

public class UserAuthService implements AuthService {

    @Override
    public boolean register(String emailAccount, String password, String confirmationPassword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    @Override
    public void login(String emailAccount, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public void logOut() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logOut'");
    }

    @Override
    public UserToken getLoggedUser() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLoggedUser'");
    }

    @Override
    public boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationPassword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateNotLoggedAccount'");
    }

    @Override
    public boolean updateLoggedInAccount(String newPassword, String confirmationPassword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateLoggedInAccount'");
    }

}

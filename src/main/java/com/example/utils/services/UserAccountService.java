package com.example.utils.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.utils.interfaces.AccountService;

import javafx.scene.image.Image;

public class UserAccountService implements AccountService {

    @Override
    public boolean addAnotherAccount(String emailAccount, String password, String confirmationPassword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAnotherAccount'");
    }

    @Override
    public boolean switchAccount(User switchtoUser) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'switchAccount'");
    }

    @Override
    public boolean removeAccount(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAccount'");
    }

    @Override
    public List<UserDTO> getAllUserAccounts() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUserAccounts'");
    }

    @Override
    public void updateImageProfile(File file) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateImageProfile'");
    }

    @Override
    public Image getImageProfile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getImageProfile'");
    }

}

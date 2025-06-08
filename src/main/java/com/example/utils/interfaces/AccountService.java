package com.example.utils.interfaces;

import com.example.model.User;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;

public interface AccountService {

    boolean addAnotherAccount(String emailAccount, String password, String confirmationPassword);

    boolean switchAccount(User switchtoUser);

    boolean removeAccount(User user);

    void updateImageProfile(File file) throws IOException;

    Image getImageProfile();
}

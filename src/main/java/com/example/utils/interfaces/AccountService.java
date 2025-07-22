package com.example.utils.interfaces;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;

import javafx.scene.image.Image;

import java.io.File;
import java.util.List;

public interface AccountService {
    boolean removeAccount(UserToken userToken, UserDTO userDTO);

    List<UserDTO> getAllUserAccounts(UserToken userToken);

    void updateImageProfile(UserToken userToken, File file);

    Image getImageProfile(UserToken userToken);

    ErrorHandler getErrorHandler();
}

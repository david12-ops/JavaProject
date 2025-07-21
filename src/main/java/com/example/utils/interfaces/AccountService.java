package com.example.utils.interfaces;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;

import javafx.scene.image.Image;

import java.io.File;
import java.util.List;

public interface AccountService {
    boolean removeAccount(UserToken userToken, UserDTO userDTO, UserRepository userRepository);

    List<UserDTO> getAllUserAccounts(UserToken userToken, UserRepository userRepository);

    void updateImageProfile(UserToken userToken, File file, UserRepository userRepository);

    Image getImageProfile(UserToken userToken, UserRepository userRepository);

    ErrorHandler getErrorHandler();
}

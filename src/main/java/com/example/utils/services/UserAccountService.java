package com.example.utils.services;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.example.dto.UserDTO;
import com.example.model.UserRepository;
import com.example.model.UserToken;
import com.example.utils.FileConvertor;
import com.example.utils.interfaces.AccountService;

import javafx.scene.image.Image;

public class UserAccountService implements AccountService {

    private UserDTO getUserByToken(UserToken userToken, List<UserDTO> userDTOs) {
        for (UserDTO userDTO : userDTOs) {
            if (userDTO.getUserId().equals(userToken.getUserId())
                    && userDTO.getMailAccount().equals(userToken.getMailAccount())) {
                return userDTO;
            }
        }
        return null;
    }

    public UserAccountService() {
    }

    @Override
    public boolean removeAccount(UserToken userToken, UserDTO userDTO, UserRepository userRepository) {
        if (userToken.getUserId().equals(userDTO.getUserId())
                && userToken.getMailAccount().equals(userDTO.getMailAccount())) {
            return false;
        }
        userRepository.removeUser(userDTO);
        return !userRepository.getAllUserDtos().contains(userDTO);
    }

    @Override
    public void updateImageProfile(UserToken userToken, File file, UserRepository userRepository) {
        UserDTO userDTO = getUserByToken(userToken, userRepository.getAllUserDtos());

        if (userDTO != null && file == null) {
            userRepository.updateUser(userDTO, (File) null);
        }

        if (userDTO != null && file != null) {
            userRepository.updateUser(userDTO, file);
        }
    }

    @Override
    public List<UserDTO> getAllUserAccounts(UserToken userToken, UserRepository userRepository) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userToken == null | userDTOs == null || userDTOs.size() == 0) {
            return List.of();
        }

        return userDTOs.stream().filter(userDTO -> !userDTO.getUserId().equals(userToken.getUserId())
                && userDTO.getGroupId().equals(userToken.getGroupId())).toList();
    }

    @Override
    public Image getImageProfile(UserToken userToken, UserRepository userRepository) {
        if (userToken == null)
            return null;

        Optional<UserDTO> foundUserDTO = userRepository.getAllUserDtos().stream()
                .filter(userDTO -> userDTO.getGroupId().equals(userToken.getGroupId())
                        && userDTO.getMailAccount().equals(userToken.getMailAccount()))
                .findAny();

        if (foundUserDTO.isPresent() && foundUserDTO.get().getProfileImage() != null) {
            return FileConvertor.Base64ToImage(foundUserDTO.get().getProfileImage());
        }
        return null;
    }

}

package com.example.model.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.utils.FileConvertor;
import com.example.utils.JsonStorageTool;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.EnvironmentType;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.cdimascio.dotenv.Dotenv;

public class UserRepository {
    static Dotenv dotenv = Dotenv.load();
    private List<User> listOfUsers;
    private JsonStorageTool<User> storageTool;
    private EnvironmentType environmentType;

    public UserRepository(EnvironmentType environmentType) {
        this.environmentType = environmentType;
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool = new JsonStorageTool<User>(dotenv.get("FILE_PATH_USERS"), new TypeReference<List<User>>() {
            });
            this.listOfUsers = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            this.listOfUsers = new ArrayList<>();
        }
    }

    private void applyUserUpdate(User currentUser, User updatedUser) {
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.updateItem(currentUser, updatedUser);
            listOfUsers = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            int index = listOfUsers.indexOf(currentUser);
            if (index >= 0) {
                listOfUsers.set(index, updatedUser);
            }
        }
    }

    private void applyUserAdding(User user) {
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.addItem(user);
            listOfUsers = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfUsers.add(user);
        }
    }

    public void setTestData(List<User> listOfUsers) {
        this.listOfUsers = listOfUsers;
    }

    public void addUser(UserDTO userDTO, AddOperationType addOperationType) {
        if (addOperationType == AddOperationType.ANOTHERACCOUNT)
            applyUserAdding(new User(null, userDTO.getGroupId(), userDTO.getMailAccount(),
                    BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt()), null));

        if (addOperationType == AddOperationType.NEWACCOUNT)
            applyUserAdding(new User(null, null, userDTO.getMailAccount(),
                    BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt()), null));
    }

    public void removeUser(UserDTO userDTO) {
        User user = new User(userDTO.getUserId(), userDTO.getGroupId(), userDTO.getMailAccount(), userDTO.getPassword(),
                userDTO.getProfileImage());
        if (environmentType == EnvironmentType.PRODUCTION) {
            storageTool.removeItem(user);
            listOfUsers = storageTool.getItems();
        } else if (environmentType == EnvironmentType.TEST) {
            listOfUsers.remove(user);
        }
    }

    public void updateUser(UserDTO currentUserDTO, UserDTO updatedUserDTO) {
        User currentUser = new User(currentUserDTO.getUserId(), currentUserDTO.getGroupId(),
                currentUserDTO.getMailAccount(), currentUserDTO.getCurrentPassword(), currentUserDTO.getProfileImage());
        User updatedUser = new User(updatedUserDTO.getUserId(), updatedUserDTO.getGroupId(),
                updatedUserDTO.getMailAccount(), BCrypt.hashpw(updatedUserDTO.getPassword(), BCrypt.gensalt()),
                updatedUserDTO.getProfileImage());

        applyUserUpdate(currentUser, updatedUser);
    }

    public void updateUser(UserDTO userDTO, File profileImage) {
        try {
            User currentUser = new User(userDTO.getUserId(), userDTO.getGroupId(), userDTO.getMailAccount(),
                    userDTO.getCurrentPassword(), userDTO.getProfileImage());
            String base64 = profileImage != null ? FileConvertor.fileToBase64(profileImage) : null;

            User updatedUser = new User(userDTO.getUserId(), userDTO.getGroupId(), userDTO.getMailAccount(),
                    userDTO.getCurrentPassword(), base64);

            applyUserUpdate(currentUser, updatedUser);
        } catch (IOException e) {
            System.err.println("Error converting image: " + e.getMessage());
        }
    }

    public List<UserDTO> getAllUserDtos() {
        List<UserDTO> userDTOs = new ArrayList<>();
        listOfUsers.forEach(user -> {
            userDTOs.add(new UserDTO(user.getUserId(), user.getGroupId(), user.getMailAccount(), user.getPassword(),
                    null, null, user.getProfileImage()));
        });
        return userDTOs;
    }
}

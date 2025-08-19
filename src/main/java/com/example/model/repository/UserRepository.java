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
    private JsonStorageTool<User> storageTool;
    private EnvironmentType environmentType;

    private List<User> listOfUsersProd;
    private List<User> listOfUsersTest;

    public UserRepository(EnvironmentType environmentType) {
        if (EnvironmentType.PRODUCTION == environmentType) {
            this.environmentType = environmentType;
            storageTool = new JsonStorageTool<User>(dotenv.get("FILE_PATH_USERS"), new TypeReference<List<User>>() {
            });
            this.listOfUsersProd = storageTool.getItems();
        } else if (EnvironmentType.TEST == environmentType) {
            this.environmentType = environmentType;
            this.listOfUsersTest = new ArrayList<>();
        } else {
            System.err.println("❌ Critical Error: Invalid environment type provided.");
            Thread.dumpStack();
            System.exit(1);
        }
    }

    private void applyUserUpdate(User currentUser, User updatedUser) {
        if (EnvironmentType.PRODUCTION == environmentType) {
            storageTool.updateItem(currentUser, updatedUser);
            listOfUsersProd = storageTool.getItems();
        } else if (EnvironmentType.TEST == environmentType) {
            int index = listOfUsersTest.indexOf(currentUser);
            if (index >= 0) {
                listOfUsersTest.set(index, updatedUser);
            }
        }
    }

    private void applyUserAdding(User user) {
        if (EnvironmentType.PRODUCTION == environmentType) {
            storageTool.addItem(user);
            listOfUsersProd = storageTool.getItems();
        } else if (EnvironmentType.TEST == environmentType) {
            listOfUsersTest.add(user);
        }
    }

    public void setTestData(List<User> listOfUsers) {
        this.listOfUsersTest = listOfUsers;
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
        if (EnvironmentType.PRODUCTION == environmentType) {
            storageTool.removeItem(user);
            listOfUsersProd = storageTool.getItems();
        } else if (EnvironmentType.TEST == environmentType) {
            listOfUsersTest.remove(user);
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
        List<User> data = EnvironmentType.PRODUCTION == environmentType ? listOfUsersProd
                : EnvironmentType.TEST == environmentType ? listOfUsersTest : new ArrayList<>();

        data.forEach(user -> {
            userDTOs.add(new UserDTO(user.getUserId(), user.getGroupId(), user.getMailAccount(), user.getPassword(),
                    null, null, user.getProfileImage()));
        });

        return userDTOs;
    }
}

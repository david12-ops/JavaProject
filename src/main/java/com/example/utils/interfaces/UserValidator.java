package com.example.utils.interfaces;

import java.io.File;
import java.util.List;

import com.example.dto.UserDTO;
import com.example.utils.enums.FormType;
import com.example.utils.enums.OperationType;

public interface UserValidator {
    boolean validProfileImage(File profileImage);

    boolean validEmail(String email);

    boolean validPassword(String currentPassword, String password, String email, FormType formType);

    boolean nonDuplicateUserWithEmail(OperationType operationType, String currentUserEmail, String newEmail,
            List<UserDTO> userDTOs);

    boolean confirmedPassword(String password, String confirmationPassword, FormType formType);
}

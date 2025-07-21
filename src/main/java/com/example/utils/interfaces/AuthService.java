package com.example.utils.interfaces;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.FormType;

public interface AuthService {
        boolean register(String emailAccount, String password, String confirmationPassword, FormType formType,
                        AddOperationType addTypeOperation, UserRepository userRepository);

        void login(String emailAccount, String password, UserRepository userRepository);

        void logOut();

        UserToken getLoggedUser();

        boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
                        String confirmationNewPassword, FormType formType, UserRepository userRepository);

        boolean updateLoggedInAccount(String newPassword, String confirmationNewPassword, FormType formType,
                        UserRepository userRepository);

        boolean switchAccount(UserDTO switchToUserDTO, UserRepository userRepository);

        ErrorHandler getErrorHandler();
}

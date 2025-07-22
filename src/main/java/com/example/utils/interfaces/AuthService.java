package com.example.utils.interfaces;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.FormType;

public interface AuthService {
        boolean register(String emailAccount, String password, String confirmationPassword, FormType formType,
                        AddOperationType addTypeOperation);

        void login(String emailAccount, String password);

        void logOut();

        UserToken getLoggedUser();

        boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
                        String confirmationNewPassword, FormType formType);

        boolean updateLoggedInAccount(String newPassword, String confirmationNewPassword, FormType formType);

        boolean switchAccount(UserDTO switchToUserDTO);

        ErrorHandler getErrorHandler();
}

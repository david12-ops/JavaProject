package com.example;

import org.mockito.junit.jupiter.MockitoExtension;

import com.example.utils.ErrorManager;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.interfaces.AuthService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.services.SessionService;
import com.example.utils.services.UserAuthService;
import com.example.utils.services.ValidationService;
import com.example.utils.services.ValidationService.MessageValidations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class UserAccountServiceTest {
    private void compareErrors(String expectedMessage, String key, ErrorHandler errorHandler) {
        String errorMessage = errorHandler.getError(key);
        assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
    }

    @Test
    @DisplayName("Should remove account")
    public void testRemoveAccount() {

    }

    @Test
    @DisplayName("Should update image profile of account")
    public void testUpdateImageProfile() {

    }

    @Test
    @DisplayName("Should get all user accounts")
    public void testGetAllUserAccounts() {

    }

    @Test
    @DisplayName("Should get image profile of account")
    public void testGetImageProfile() {

    }
}

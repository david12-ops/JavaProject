package com.example;

import org.mockito.junit.jupiter.MockitoExtension;

import com.example.utils.interfaces.ErrorHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class UserAccountServiceTest {
    private void compareErrors(String expectedMessage, String key, ErrorHandler errorHandler) {
        String errorMessage = errorHandler.getError(key);
        if (errorMessage != null) {
            assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
            errorHandler.removeError(key);
        }
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

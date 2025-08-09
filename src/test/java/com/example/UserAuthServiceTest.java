package com.example;

import org.mockito.junit.jupiter.MockitoExtension;

import com.example.utils.interfaces.ErrorHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class UserAuthServiceTest {
    private void compareErrors(String expectedMessage, String key, ErrorHandler errorHandler) {
        String errorMessage = errorHandler.getError(key);
        if (errorMessage != null) {
            assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
            errorHandler.removeError(key);
        }
    }

    @Test
    @DisplayName("Should register new account")
    public void testRegister() {

    }

    @Test
    @DisplayName("Should login account")
    public void testLogin() {

    }

    @Test
    @DisplayName("Should update not logged account")
    public void testUpdateNotLoggedAccount() {

    }

    @Test
    @DisplayName("Should switch account")
    public void testSwitchAccount() {

    }

    @Test
    @DisplayName("Should update logged account")
    public void testUpdateLoggedInAccount() {

    }
}

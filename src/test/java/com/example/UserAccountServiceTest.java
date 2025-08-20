package com.example;

import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.interfaces.AccountService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.services.UserAccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class UserAccountServiceTest {
    private AccountService accountService;

    private void compareErrors(String expectedMessage, String key, ErrorHandler errorHandler) {
        String errorMessage = errorHandler.getError(key);
        assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
    }

    @BeforeEach
    void setup() {
        this.accountService = new UserAccountService(EnvironmentType.TEST);
    }

    @Test
    @DisplayName("Should remove account")
    public void testRemoveAccount() {
        assertFalse(accountService.removeAccount(null, null));
        compareErrors("Invalid token argument in removeAccount function.", "token", accountService.getErrorHandler());

        UserToken userToken1 = new UserToken(null, "groupC", "eve@example.com");
        assertFalse(accountService.removeAccount(userToken1, null));
        compareErrors("Invalid token argument in removeAccount function.", "token", accountService.getErrorHandler());

        UserToken userToken2 = new UserToken("5", null, "eve@example.com");
        assertFalse(accountService.removeAccount(userToken2, null));
        compareErrors("Invalid token argument in removeAccount function.", "token", accountService.getErrorHandler());

        UserToken userToken3 = new UserToken("5", "groupC", null);
        assertFalse(accountService.removeAccount(userToken3, null));
        compareErrors("Invalid token argument in removeAccount function.", "token", accountService.getErrorHandler());

        UserToken userToken4 = new UserToken("5", "groupC", "eve@example.com");
        assertFalse(accountService.removeAccount(userToken4, null));
        compareErrors("Invalid dto argument in removeAccount function.", "dto", accountService.getErrorHandler());

        UserToken userToken5 = new UserToken("5", "groupC", "eve@example.com");
        assertFalse(accountService.removeAccount(userToken5,
                new UserDTO(null, null, "alice@example.com", null, null, null, null)));

        UserToken userToken6 = new UserToken("1", "groupA", "alice@example.com");
        assertTrue(accountService.removeAccount(userToken6,
                new UserDTO(null, null, "bob@example.com", null, null, null, null)));

        UserToken userToken7 = new UserToken("1", "groupA", "alice@example.com");
        assertFalse(accountService.removeAccount(userToken7,
                new UserDTO(null, null, "bopaaaa@example.com", null, null, null, null)));
    }

    @Test
    @DisplayName("Should update image profile of account")
    public void testUpdateImageProfile() {
        accountService.updateImageProfile(null, null);
        compareErrors("Invalid token argument in updateImageProfile function.", "token",
                accountService.getErrorHandler());

        UserToken userToken1 = new UserToken(null, "groupC", "eve@example.com");
        accountService.updateImageProfile(userToken1, null);
        compareErrors("Invalid token argument in updateImageProfile function.", "token",
                accountService.getErrorHandler());

        UserToken userToken2 = new UserToken("5", null, "eve@example.com");
        accountService.updateImageProfile(userToken2, null);
        compareErrors("Invalid token argument in updateImageProfile function.", "token",
                accountService.getErrorHandler());

        UserToken userToken3 = new UserToken("5", "groupC", null);
        accountService.updateImageProfile(userToken3, null);
        compareErrors("Invalid token argument in updateImageProfile function.", "token",
                accountService.getErrorHandler());
    }

    @Test
    @DisplayName("Should get all user accounts")
    public void testGetAllUserAccounts() {
        List<UserDTO> userDTOs;

        UserToken userToken1 = new UserToken("1", "groupA", "alice@example.com");
        assertEquals(1, accountService.getAllUserAccounts(userToken1).size());

        UserToken userToken2 = new UserToken("3", "groupB", "charlie@example.com");
        assertEquals(1, accountService.getAllUserAccounts(userToken2).size());

        UserToken userToken3 = new UserToken("5", "groupC", "eve@example.com");
        assertEquals(0, accountService.getAllUserAccounts(userToken3).size());

        userDTOs = accountService.getAllUserAccounts(null);
        assertEquals(0, userDTOs == null ? 0 : userDTOs.size());
        compareErrors("Invalid token argument in getAllUserAccounts function.", "token",
                accountService.getErrorHandler());

        UserToken userToken4 = new UserToken(null, "groupC", "eve@example.com");
        userDTOs = accountService.getAllUserAccounts(userToken4);
        assertEquals(0, userDTOs == null ? 0 : userDTOs.size());
        compareErrors("Invalid token argument in getAllUserAccounts function.", "token",
                accountService.getErrorHandler());

        UserToken userToken5 = new UserToken("5", null, "eve@example.com");
        userDTOs = accountService.getAllUserAccounts(userToken5);
        assertEquals(0, userDTOs == null ? 0 : userDTOs.size());
        compareErrors("Invalid token argument in getAllUserAccounts function.", "token",
                accountService.getErrorHandler());

        UserToken userToken6 = new UserToken("5", "groupC", null);
        userDTOs = accountService.getAllUserAccounts(userToken6);
        assertEquals(0, userDTOs == null ? 0 : userDTOs.size());
        compareErrors("Invalid token argument in getAllUserAccounts function.", "token",
                accountService.getErrorHandler());
    }

    @Test
    @DisplayName("Should get image profile of account")
    public void testGetImageProfile() {
        assertNull(accountService.getImageProfile(null));
        compareErrors("Invalid token argument in getImageProfile function.", "token", accountService.getErrorHandler());

        UserToken userToken1 = new UserToken(null, "groupC", "eve@example.com");
        assertNull(accountService.getImageProfile(userToken1));
        compareErrors("Invalid token argument in getImageProfile function.", "token", accountService.getErrorHandler());

        UserToken userToken2 = new UserToken("5", null, "eve@example.com");
        assertNull(accountService.getImageProfile(userToken2));
        compareErrors("Invalid token argument in getImageProfile function.", "token", accountService.getErrorHandler());

        UserToken userToken3 = new UserToken("5", "groupC", null);
        assertNull(accountService.getImageProfile(userToken3));
        compareErrors("Invalid token argument in getImageProfile function.", "token", accountService.getErrorHandler());

        UserToken userToken4 = new UserToken("5", "groupC", "eve@example.com");
        assertNull(accountService.getImageProfile(userToken4));

        UserToken userToken5 = new UserToken("5", "groupC", "eve@example.com");
        assertNull(accountService.getImageProfile(userToken5));
    }
}

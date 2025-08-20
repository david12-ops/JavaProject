package com.example;

import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.UserDTO;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.FormType;
import com.example.utils.interfaces.AuthService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.services.SessionService;
import com.example.utils.services.UserAuthService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class UserAuthServiceTest {
        private AuthService authService;

        private void compareErrors(String expectedMessage, String key, ErrorHandler errorHandler) {
                String errorMessage = errorHandler.getError(key);
                assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
        }

        @BeforeEach
        void setup() {
                this.authService = new UserAuthService(SessionService.getInstance(), EnvironmentType.TEST);
        }

        @Test
        @DisplayName("Should register new account")
        public void testRegister() {
                boolean registered1 = authService.register("example@gmail.com", "Password1234", "Password1234",
                                FormType.FORGOTCREDENTIALS, AddOperationType.NEWACCOUNT);
                compareErrors("Provided unsupported type of form.", "formType", authService.getErrorHandler());
                assertFalse(registered1);

                boolean registered2 = authService.register("example@gmail.com", "Password1234", "Password1234", null,
                                AddOperationType.ANOTHERACCOUNT);
                compareErrors("Provided unsupported type of form.", "formType", authService.getErrorHandler());
                assertFalse(registered2);

                boolean registered3 = authService.register("example@gmail.com", "Password1234", "Password1234",
                                FormType.REGISTER, null);
                compareErrors("Operation with type null is not supported.", "operationType",
                                authService.getErrorHandler());
                assertFalse(registered3);

                boolean registered4 = authService.register("examplegmail.com", "Password1234", "Password1234",
                                FormType.ADDACCOUNT, AddOperationType.NEWACCOUNT);
                assertFalse(registered4);

                boolean registered5 = authService.register("examplegmail.com", "Password1234", "Password1234",
                                FormType.ADDACCOUNT, AddOperationType.ANOTHERACCOUNT);
                assertFalse(registered5);

                boolean registered6 = authService.register("example@gmail.com", "Password@1234", "Password@1234",
                                FormType.REGISTER, AddOperationType.NEWACCOUNT);
                assertTrue(registered6);

                boolean registered7 = authService.register("example2@gmail.com", "Password@123456", "Password@123456",
                                FormType.ADDACCOUNT, AddOperationType.ANOTHERACCOUNT);
                compareErrors("Invalid token argument in register function.", "token", authService.getErrorHandler());
                assertFalse(registered7);

                authService.login("example@gmail.com", "Password@1234");
                boolean registered8 = authService.register("example2@gmail.com", "Password@123456", "Password@123456",
                                FormType.ADDACCOUNT, AddOperationType.ANOTHERACCOUNT);
                assertTrue(registered8);
        }

        @Test
        @DisplayName("Should update not logged account")
        public void testUpdateNotLoggedAccount() {
                boolean updated1 = authService.updateNotLoggedAccount("alice@example.com", "Password@1234",
                                "Password@123456", "Password@123456", FormType.FORGOTCREDENTIALS);
                compareErrors("Invalid dto argument in updateNotLoggedAccount function.", "dto",
                                authService.getErrorHandler());
                assertFalse(updated1);

                boolean updated2 = authService.updateNotLoggedAccount("alice@example.com", "hashedPassword1!",
                                "Password@123456", "Password@123456", FormType.FORGOTCREDENTIALS);
                assertTrue(updated2);

                boolean updated3 = authService.updateNotLoggedAccount("alice@example.com", "hashedPassword1!",
                                "Password@123456", "Password@123456", null);
                compareErrors("Provided unsupported type of form.", "formType", authService.getErrorHandler());
                assertFalse(updated3);

                boolean updated4 = authService.updateNotLoggedAccount("alice@example.com", "hashedPassword1!",
                                "Password@123456", "Password@1236", FormType.FORGOTCREDENTIALS);
                assertFalse(updated4);
        }

        @Test
        @DisplayName("Should switch account")
        public void testSwitchAccount() {
                boolean switch1 = authService.switchAccount(null);
                compareErrors("Invalid token argument in switchAccount function.", "token",
                                authService.getErrorHandler());
                assertFalse(switch1);

                authService.login("alice@example.com", "hashedPassword1!");
                boolean switch2 = authService.switchAccount(null);
                compareErrors("Invalid dto argument in switchAccount function.", "dto", authService.getErrorHandler());
                assertFalse(switch2);
                authService.logOut();

                UserDTO userDTOToSwitch1 = new UserDTO(null, null, "bob@example.com", null, null, null, null);
                authService.login("alice@example.com", "hashedPassword1!");
                boolean switch3 = authService.switchAccount(userDTOToSwitch1);
                assertTrue(switch3);
                authService.logOut();

                UserDTO userDTOToSwitch2 = new UserDTO(null, null, "eve@example.com", null, null, null, null);
                authService.login("alice@example.com", "hashedPassword1!");
                boolean switch4 = authService.switchAccount(userDTOToSwitch2);
                assertFalse(switch4);
                authService.logOut();
        }

        @Test
        @DisplayName("Should log in")
        public void testLogin() {
                authService.login("alice@example.com", "hashedPassword1!");
                assertNotNull(authService.getLoggedUser());
                authService.logOut();
                assertNull(authService.getLoggedUser());

                authService.login("alice@example.com", "hahesword1!");
                compareErrors("Invalid dto argument in login function.", "dto", authService.getErrorHandler());
                assertNull(authService.getLoggedUser());

                authService.login("alice@exale.com", "hashedPassword1!");
                compareErrors("Invalid dto argument in login function.", "dto", authService.getErrorHandler());
                assertNull(authService.getLoggedUser());
        }

        @Test
        @DisplayName("Should update logged account")
        public void testUpdateLoggedInAccount() {
                authService.login("alice@example.com", "hashedPassword1!");
                boolean updated1 = authService.updateLoggedInAccount("@example.com", "Password@123456",
                                FormType.FORGOTCREDENTIALS);
                assertFalse(updated1);
                authService.logOut();

                authService.login("alice@example.com", "hashedPassword1!");
                boolean updated2 = authService.updateLoggedInAccount("Password@123456", "Password@123456",
                                FormType.FORGOTCREDENTIALS);
                assertTrue(updated2);
                authService.logOut();

                boolean updated3 = authService.updateLoggedInAccount("Password@123456", "Password@123456",
                                FormType.FORGOTCREDENTIALS);
                compareErrors("Invalid token argument in updateLoggedInAccount function.", "token",
                                authService.getErrorHandler());
                assertFalse(updated3);

                authService.login("alice@example.com", "hashedPassword1!");
                boolean updated4 = authService.updateLoggedInAccount("Password@123456", "Password@123456", null);
                compareErrors("Provided unsupported type of form.", "formType", authService.getErrorHandler());
                assertFalse(updated4);
                authService.logOut();
        }
}

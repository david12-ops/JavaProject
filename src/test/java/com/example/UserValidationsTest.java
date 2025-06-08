package com.example;

import com.example.model.User;
import com.example.utils.ErrorManager;
import com.example.utils.enums.FormType;
import com.example.utils.enums.OperationType;
import com.example.utils.services.ValidationService;
import com.example.utils.services.ValidationService.UserValidations;

import javafx.util.Pair;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserValidationsTest {

        private ErrorManager errorToolManager;
        private ValidationService validationService;
        private UserValidations validator;
        private Map<FormType, String> errors;

        @BeforeEach
        void setup() {
                this.errorToolManager = new ErrorManager(new HashMap<>());
                this.validationService = new ValidationService();
                this.validator = validationService.new UserValidations(errorToolManager);
                this.errors = new HashMap<>();
        }

        private void compareErrors(String expectedMessage, List<String> keys, ErrorManager errorToolManager) {
                for (String key : keys) {
                        String errorMessage = errorToolManager.getError(key);
                        if (errorMessage != null) {
                                assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
                                errorToolManager.removeError(key);
                        }
                }
        }

        @Test
        @DisplayName("Should validate password confirmation fails for mismatches and succeeds for exact matches")
        void testPasswordMatchSuccess() {

                errors.put(FormType.ADDACCOUNT, "Password does not match the confirmation");
                errors.put(FormType.REGISTER, "Password does not match the confirmation");
                errors.put(FormType.FORGOTCREDENTIALS, "New password does not match the confirmation");

                List<Pair<String, String>> invalidInputs = Arrays.asList(new Pair<>("Secret", "secret"), // case-sensitive
                                new Pair<>("a@čbc", "ca@čb"), // different order
                                new Pair<>("abcde", "abfde"), // one char difference
                                new Pair<>("abcde", null));

                List<Pair<String, String>> validInputs = Arrays
                                .asList(new Pair<>("long@stringwithtext", "long@stringwithtext"));

                for (FormType form : Arrays.asList(FormType.ADDACCOUNT, FormType.FORGOTCREDENTIALS,
                                FormType.REGISTER)) {
                        for (Pair<String, String> pair : invalidInputs) {
                                assertFalse(validator.confirmedPassword(pair.getKey(), pair.getValue(), form));

                                compareErrors(form == FormType.ADDACCOUNT || form == FormType.REGISTER
                                                ? "Password does not match the confirmation"
                                                : "New password does not match the confirmation",
                                                Arrays.asList("confirmPassword", "confirmNewPassword"),
                                                errorToolManager);

                        }

                        for (Pair<String, String> pair : validInputs) {
                                assertTrue(validator.confirmedPassword(pair.getKey(), pair.getValue(), form));
                        }

                }
        }

        @Test
        @DisplayName("Should enforce password rules: structure, similarity to email, and uniqueness from current")
        void testPasswordContent() {
                List<String> validPasswords = Arrays.asList("Password1!45", "Welcome2@456", "Secure3$7895");

                List<String> sameAsCurrentPassword = Arrays.asList("Password1!", "Welcome2@", "Secure3$");

                List<String> invalidPasswords = Arrays.asList("password", // no uppercase, digit, or special character
                                "PASSWORD1!", // no lowercase
                                "12345!@", // no letters
                                null);

                List<String> tooSimilarPasswordsWithEmail = Arrays.asList("John.Doe123!", "Jane.smith@123",
                                "Michael@123");

                List<User> users = Arrays.asList(
                                new User(null, null, "john.doe@example.com",
                                                BCrypt.hashpw("Password1!", BCrypt.gensalt())),
                                new User(null, null, "jane.smith@example.com",
                                                BCrypt.hashpw("Welcome2@", BCrypt.gensalt())),
                                new User(null, null, "michael.lee@example.com",
                                                BCrypt.hashpw("Secure3$", BCrypt.gensalt())));

                for (FormType form : Arrays.asList(FormType.ADDACCOUNT, FormType.FORGOTCREDENTIALS,
                                FormType.REGISTER)) {
                        for (int i = 0; i < users.size(); i++) {
                                User user = users.get(i);
                                String validPassword = validPasswords.get(i);
                                String sameAsCurrentPass = sameAsCurrentPassword.get(i);

                                assertFalse(validator.validPassword(user.getPassword(),
                                                tooSimilarPasswordsWithEmail.get(i), user.getMailAccount(), form));
                                compareErrors(form == FormType.ADDACCOUNT || form == FormType.REGISTER
                                                ? "Password is too similar to your email"
                                                : "New password is too similar to your email",
                                                Arrays.asList("password", "newPassword"), errorToolManager);

                                assertTrue(validator.validPassword(user.getPassword(), validPassword,
                                                user.getMailAccount(), form));

                                assertFalse(validator.validPassword(user.getPassword(), sameAsCurrentPass,
                                                user.getMailAccount(), form));

                                compareErrors(form == FormType.ADDACCOUNT || form == FormType.REGISTER
                                                ? "Password must be different from the current password"
                                                : "New password must be different from the current password",
                                                Arrays.asList("password", "newPassword"), errorToolManager);

                                for (String password : invalidPasswords) {
                                        assertFalse(validator.validPassword(user.getPassword(), password,
                                                        user.getMailAccount(), form));
                                        compareErrors(form == FormType.ADDACCOUNT || form == FormType.REGISTER
                                                        ? "Password must include uppercase, lowercase, number, and special character, and be at least 8 characters"
                                                        : "New password must include uppercase, lowercase, number, and special character, and be at least 8 characters",
                                                        Arrays.asList("password", "newPassword"), errorToolManager);

                                }

                        }
                }
        }

        @Test
        @DisplayName("Should detect invalid email formats and accept valid ones")
        void testEmailContent() {
                List<String> invalidEmails = Arrays.asList(null, // Null email
                                "", // Empty string
                                "plainaddress", // Missing '@' and domain
                                "@missinglocalpart.com", // Missing local part before '@'
                                "missingdomain@.com", // Missing domain name before the dot
                                "missingat.com", // Missing '@' symbol
                                "no@domain@domain.com", // Multiple '@' symbols
                                "user@domain", // Missing domain suffix (.com, .org, etc.)
                                "user@domain..com", // Double dots in the domain part
                                "user@domain_com", // Underscore in the domain part is invalid
                                "user@domain#com", // Invalid character '#' in domain part
                                null);

                List<String> validEmails = Arrays.asList("user@example.com", // Basic valid email
                                "user.name@domain.com", // Email with a dot in the local part
                                "user_name@domain.com", // Email with an underscore in the local part
                                "user123@domain.co", // Alphanumeric local part and a valid domain suffix
                                "user@subdomain.domain.com", // Email with a subdomain
                                "user+name@domain.com", // Email with a '+' in the local part
                                "user.name123@domain.co.uk", // Email with a domain suffix longer than .com
                                "user1234@sub.domain.org" // Email with multiple subdomains
                );

                for (String email : invalidEmails) {
                        assertFalse(validator.validEmail(email));
                        compareErrors("Please enter a valid email address (e.g., user@example.com)",
                                        Arrays.asList("email"), errorToolManager);

                }

                for (String email : validEmails) {
                        assertTrue(validator.validEmail(email));
                }

        }

        @Test
        @DisplayName("Should detect duplicate emails correctly during user creation and update")
        void testEmailDuplication() {

                List<User> users = Arrays.asList(
                                new User(null, null, "user@example.com", BCrypt.hashpw("Password1!", BCrypt.gensalt())),
                                new User(null, null, "user.name@domain.com",
                                                BCrypt.hashpw("Password2!", BCrypt.gensalt())),
                                new User(null, null, "user_name@domain.com",
                                                BCrypt.hashpw("Password3!", BCrypt.gensalt())),
                                new User(null, null, "user123@domain.co",
                                                BCrypt.hashpw("Password4!", BCrypt.gensalt())),
                                new User(null, null, "user@subdomain.domain.com",
                                                BCrypt.hashpw("Password5!", BCrypt.gensalt())),
                                new User(null, null, "user+name@domain.com",
                                                BCrypt.hashpw("Password6!", BCrypt.gensalt())),
                                new User(null, null, "user.name123@domain.co.uk",
                                                BCrypt.hashpw("Password7!", BCrypt.gensalt())),
                                new User(null, null, "user1234@sub.domain.org",
                                                BCrypt.hashpw("Password8!", BCrypt.gensalt())));

                assertTrue(validator.nonDuplicateUserWithEmail(OperationType.CREATE, null, "user12@sub.domain.org",
                                users));
                assertFalse(validator.nonDuplicateUserWithEmail(OperationType.CREATE, null, "user1234@sub.domain.org",
                                users));
                compareErrors("Provided email is already used", Arrays.asList("email"), errorToolManager);

                assertTrue(validator.nonDuplicateUserWithEmail(OperationType.UPDATE, "user@example.com",
                                "user.test12@sub.domain.org", users));
                assertFalse(validator.nonDuplicateUserWithEmail(OperationType.UPDATE, "user@example.com",
                                "user1234@sub.domain.org", users));
                compareErrors("Provided email is already used", Arrays.asList("email"), errorToolManager);
        }
}

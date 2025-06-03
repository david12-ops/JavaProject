package com.example.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import com.example.utils.ErrorToolManager;
import com.example.utils.FileConvertor;
import com.example.utils.JsonStorageTool;
import com.example.utils.enums.AddTypeOperation;
import com.example.utils.enums.Environment;
import com.example.utils.enums.Form;
import com.example.utils.enums.Operation;
import com.example.utils.services.ValidationService;
import com.fasterxml.jackson.core.type.TypeReference;

import io.github.cdimascio.dotenv.Dotenv;

public class UserModel {

    static Dotenv dotenv = Dotenv.load();
    private Map<String, String> errorMap = new HashMap<>();
    private final ErrorToolManager errorToolManager = new ErrorToolManager(errorMap);
    private final ValidationService validationService = new ValidationService();
    private final ValidationService.UserModelValidations validator = validationService.new UserModelValidations(
            errorToolManager);
    private List<User> listOfUsers;
    private JsonStorageTool<User> storageTool;
    private Environment environment;

    public UserModel(Environment environment) {
        this.environment = environment;
        if (environment == Environment.PRODUCTION) {
            storageTool = new JsonStorageTool<User>(dotenv.get("FILE_PATH_USERS"), new TypeReference<List<User>>() {
            });
            this.listOfUsers = storageTool.getItems();
        } else if (environment == Environment.TEST) {
            this.listOfUsers = new ArrayList<>();
        }
    }

    // Support Methods
    private boolean validatePasswords(String email, String currentPassword, String password,
            String confirmationPassword, Form form) {

        clearError("validation");
        if (isFormSupported(form)) {
            return validator.validPassword(currentPassword, password, email, form)
                    && validator.confirmedPassword(password, confirmationPassword, form);
        } else {
            errorToolManager.logError(errorToolManager.createErrorBody("validation",
                    "Validation for form " + form + " is not supported"));
            return false;
        }
    }

    private boolean validateData(Operation operation, String currentEmail, String newEmail, String currentPassword,
            String password, String confirmationPassword, Form form) {

        clearError("validation");
        if (isFormSupported(form)) {
            return validator.validEmail(newEmail)
                    && validator.nonDuplicateUserWithEmail(operation, currentEmail, newEmail, listOfUsers)
                    && validatePasswords(newEmail, currentPassword, password, confirmationPassword, form);
        } else {
            errorToolManager.logError(errorToolManager.createErrorBody("validation",
                    "Validation for form " + form + " is not supported"));
            return false;
        }

    }

    private boolean applyUserUpdate(User user, User updatedUser, String confirmationPassword) {
        if (environment == Environment.PRODUCTION) {
            storageTool.updateItem(user, updatedUser);
            listOfUsers = storageTool.getItems();
        } else if (environment == Environment.TEST) {
            int index = listOfUsers.indexOf(user);
            if (index >= 0) {
                listOfUsers.set(index, updatedUser);
            } else {
                errorToolManager
                        .logError(errorToolManager.createErrorBody("updateUser", "User not found in test user list"));
            }
        }

        boolean updated = getUserByEmailAndPassword(updatedUser.getMailAccount(), confirmationPassword) != null;
        if (updated) {
            clearError("updateUser");
        }

        return updated;
    }

    private boolean applyUserAdding(User user, String email, String confirmationPassword) {
        if (environment == Environment.PRODUCTION) {
            storageTool.addItem(user);
            listOfUsers = storageTool.getItems();
        } else if (environment == Environment.TEST) {
            listOfUsers.add(user);
        }

        boolean added = getUserByEmailAndPassword(email, confirmationPassword) != null;
        if (added) {
            clearError("addAccount");
        }

        return added;
    }

    private User getUserByEmailAndPassword(String email, String password) {
        for (User user : listOfUsers) {
            if (user.getMailAccount().equals(email) && BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    private User getUserByToken(UserToken userToken) {
        for (User user : listOfUsers) {
            if (user.getUserId().equals(userToken.getUserId())
                    && user.getMailAccount().equals(userToken.getMailAccount())) {
                return user;
            }
        }
        return null;
    }

    private boolean isFormSupported(Form form) {
        return form == Form.ADDACCOUNT || form == Form.FORGOTCREDENTIALS || form == Form.REGISTER;
    }

    // Methods that implement the main logic
    public void setTestData(List<User> listOfUsers) {
        this.listOfUsers = listOfUsers;
    }

    public List<User> getTestData() {
        return listOfUsers;
    }

    public boolean addUser(String emailAccount, String password, String confirmationPassword, UserToken userToken,
            AddTypeOperation addTypeOperation, Form form) {

        if (!isFormSupported(form)) {
            errorToolManager
                    .logError(errorToolManager.createErrorBody("addAccount", "Form " + form + " is not supported"));
            return false;
        }

        if (addTypeOperation == AddTypeOperation.NEWACCOUNT) {
            if (validateData(Operation.CREATE, null, emailAccount, null, password, confirmationPassword, form)) {
                User newUser = new User(null, null, emailAccount, BCrypt.hashpw(password, BCrypt.gensalt()));
                return applyUserAdding(newUser, emailAccount, confirmationPassword);
            }
            return false;
        }

        if (addTypeOperation == AddTypeOperation.ANOTHERACCOUNT) {
            if (userToken == null) {
                errorToolManager.logError(errorToolManager.createErrorBody("addAccount", "User token is required"));
                return false;
            }

            User loggedUser = getUserByToken(userToken);
            if (loggedUser != null && validateData(Operation.CREATE, null, emailAccount, loggedUser.getPassword(),
                    password, confirmationPassword, form)) {
                User newUser = new User(null, loggedUser.getGroupId(), emailAccount,
                        BCrypt.hashpw(password, BCrypt.gensalt()));
                return applyUserAdding(newUser, newUser.getMailAccount(), confirmationPassword);
            } else {
                errorToolManager.logError(errorToolManager.createErrorBody("addAccount",
                        "Logged-in user not found or invalid credentials"));
                return false;
            }
        }

        errorToolManager.logError(
                errorToolManager.createErrorBody("addAccount", "Operation " + addTypeOperation + " is not supported"));
        return false;
    }

    public boolean removeUser(UserToken userToken, User user) {
        User loggedUser = getUserByToken(userToken);

        if (user == null) {
            errorToolManager.logError(errorToolManager.createErrorBody("removeAccount", "User to remove is required"));
            return false;
        }

        if (loggedUser != null && !loggedUser.getMailAccount().equals(user.getMailAccount())) {
            if (environment == Environment.PRODUCTION) {
                storageTool.removeItem(user);
                listOfUsers = storageTool.getItems();
            } else if (environment == Environment.TEST) {
                listOfUsers.remove(user);
            }

            boolean stillExists = listOfUsers.stream()
                    .anyMatch(item -> item.getMailAccount().equals(user.getMailAccount()));

            if (!stillExists) {
                clearError("removeAccount");
                return true;
            }
            return false;
        }

        errorToolManager.logError(errorToolManager.createErrorBody("removeAccount",
                "User is not logged in or is attempting to remove their own account"));
        return false;
    }

    public boolean updateUser(UserToken userToken, String password, String confirmationPassword, Form form) {
        User foundUser = getUserByToken(userToken);

        if (foundUser == null) {
            errorToolManager.logError(
                    errorToolManager.createErrorBody("updateUser", "Invalid or expired token — user not found"));
            return false;
        }

        if (!validatePasswords(foundUser.getMailAccount(), foundUser.getPassword(), password, confirmationPassword,
                form)) {
            errorToolManager.logError(errorToolManager.createErrorBody("updateUser", "Password validation failed"));
            return false;
        }

        User updatedUser = new User(foundUser.getUserId(), foundUser.getGroupId(), foundUser.getMailAccount(),
                BCrypt.hashpw(confirmationPassword, BCrypt.gensalt()));

        return applyUserUpdate(foundUser, updatedUser, confirmationPassword);
    }

    public boolean updateUser(User user, String password, String confirmationPassword, Form form) {

        if (user == null) {
            errorToolManager.logError(errorToolManager.createErrorBody("updateUser", "User is required"));
            return false;
        }

        if (!validatePasswords(user.getMailAccount(), user.getPassword(), password, confirmationPassword, form)) {
            errorToolManager.logError(errorToolManager.createErrorBody("updateUser", "Password validation failed"));
            return false;
        }

        User updatedUser = new User(user.getUserId(), user.getGroupId(), user.getMailAccount(),
                BCrypt.hashpw(confirmationPassword, BCrypt.gensalt()));

        return applyUserUpdate(user, updatedUser, confirmationPassword);
    }

    public void updateUser(UserToken userToken, File profileImage) {
        User user = getUserByToken(userToken);

        if (user == null) {
            errorToolManager
                    .logError(errorToolManager.createErrorBody("updateUser", "User not found for provided token"));
            return;
        }

        if (!validator.validProfileImage(profileImage)) {
            errorToolManager.logError(errorToolManager.createErrorBody("updateUser", "Invalid profile image"));
            return;
        }

        try {
            String base64 = profileImage != null ? FileConvertor.imageToBase64(profileImage) : null;
            user.setImage(base64);
            if (environment == Environment.PRODUCTION) {
                storageTool.updateItem(user, user);
                listOfUsers = storageTool.getItems();
            } else if (environment == Environment.TEST) {
                listOfUsers.set(listOfUsers.indexOf(user), user);
            }
        } catch (IOException e) {
            System.err.println("Error converting image: " + e.getMessage());
        }
    }

    public String getError(String errorName) {
        return errorToolManager.getError(errorName);
    }

    public void clearError(String errorName) {
        errorToolManager.removeError(errorName);
    }

    public User getUserByCredentials(String emailAccount, String password, UserToken userToken) {
        clearError("getAccount");

        if (listOfUsers == null || listOfUsers.isEmpty()) {
            errorToolManager.logError(errorToolManager.createErrorBody("getAccount", "No users available"));
            return null;
        }

        if (emailAccount != null && password != null && userToken == null) {
            return getUserByEmailAndPassword(emailAccount, password);
        }

        if (emailAccount == null && password == null && userToken != null) {
            return getUserByToken(userToken);
        }

        errorToolManager
                .logError(errorToolManager.createErrorBody("getAccount", "Invalid credentials, user not found"));
        return null;
    }

    public List<User> getAllUserAccounts(UserToken userToken) {
        if (userToken == null || getUserByToken(userToken) == null) {
            errorToolManager.logError(errorToolManager.createErrorBody("getAccounts",
                    "Invalid token or user not found by provided token"));
            return null;
        }

        return listOfUsers.stream().filter(user -> user.getGroupId().equals(userToken.getGroupId())).toList();
    }

}

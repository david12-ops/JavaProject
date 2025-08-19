package com.example.utils.services;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;
import com.example.utils.RepositoryFactory;
import com.example.utils.ValidationContext;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.FormType;
import com.example.utils.enums.OperationType;
import com.example.utils.enums.ValidationMode;
import com.example.utils.interfaces.AuthService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.UserValidator;

public class UserAuthService implements AuthService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.USER);
    private final UserRepository userRepository;

    private final ErrorHandler errorHandler;
    private final UserValidator userValidator;

    private final SessionService sessionService;
    private String currentSessionId;

    public UserAuthService(SessionService sessionService, EnvironmentType environmentType) {
        this.userRepository = RepositoryFactory.getUserRepository(environmentType);
        this.errorHandler = validationContext.getUserValidationBundle().getErrorManager();
        this.userValidator = validationContext.getUserValidationBundle().getValidator();
        this.sessionService = sessionService;

        if (environmentType == EnvironmentType.TEST)
            setTestUsersList(userRepository);
    }

    private void setTestUsersList(UserRepository userRepository) {
        List<User> users = new ArrayList<>();

        users.add(new User("1", "groupA", "alice@example.com", BCrypt.hashpw("hashedPassword1!", BCrypt.gensalt()),
                null));
        users.add(
                new User("2", "groupA", "bob@example.com", BCrypt.hashpw("hashedPassword2!", BCrypt.gensalt()), null));
        users.add(new User("3", "groupB", "charlie@example.com", BCrypt.hashpw("hashedPassword3!", BCrypt.gensalt()),
                null));
        users.add(
                new User("4", "groupB", "dave@example.com", BCrypt.hashpw("hashedPassword4!", BCrypt.gensalt()), null));
        users.add(
                new User("5", "groupC", "eve@example.com", BCrypt.hashpw("hashedPassword5!", BCrypt.gensalt()), null));

        userRepository.setTestData(users);
    }

    private record LabeledValue(String label, Object value) {
    }

    // Support Methods
    private boolean containsDataNull(String location, LabeledValue... labeledValues) {
        for (LabeledValue labeledValue : labeledValues) {
            if (labeledValue.value == null) {
                errorHandler.logError(errorHandler.createErrorBody(labeledValue.label(),
                        "Invalid " + labeledValue.label() + " argument in " + location + "."));
                return true;
            }
        }
        return false;
    }

    private void clearErrors(ErrorHandler errorHandler, String... errorkeys) {
        for (String key : errorkeys)
            errorHandler.removeError(key);
    }

    private boolean isFormSupported(FormType formType, EnumSet supportedTypes) {
        return supportedTypes.contains(formType);
    }

    private UserToken checkUserToken(UserToken userToken) {
        if (userToken == null || userToken.getMailAccount() == null || userToken.getUserId() == null) {
            return null;
        }

        return userToken;
    }

    private String resolveIdByEmail(String email) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userDTOs == null || userDTOs.size() == 0 || email == null)
            return null;

        for (UserDTO userDTO : userDTOs) {
            if (email.equals(userDTO.getMailAccount())) {
                return userDTO.getUserId();
            }
        }
        return null;
    }

    private UserDTO creaUserDTO(String userId, String groupId, String currentPassword, String mailAccount,
            String password, String confirmationPassword, String profileImage) {
        return new UserDTO(userId, groupId, mailAccount, currentPassword, confirmationPassword, password, profileImage);
    }

    private boolean validatePasswords(String email, String currentPassword, String password,
            String confirmationPassword, FormType formType) {
        return userValidator.validPassword(currentPassword, password, email, formType)
                && userValidator.confirmedPassword(password, confirmationPassword, formType);
    }

    private boolean validateData(OperationType operationType, String currentEmail, String newEmail,
            String currentPassword, String password, String confirmationPassword, FormType formType,
            List<UserDTO> userDTOs) {
        return userValidator.validEmail(newEmail)
                && userValidator.nonDuplicateUserWithEmail(operationType, currentEmail, newEmail, userDTOs)
                && validatePasswords(newEmail, currentPassword, password, confirmationPassword, formType);

    }

    private UserDTO getUserDTOByEmailAndPassword(String email, String password) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userDTOs == null || userDTOs.size() == 0 || email == null || password == null)
            return null;

        for (UserDTO userDTO : userDTOs) {
            String userMailAccountFromDTO = userDTO.getMailAccount();
            String userCurrentPasswordFromDTO = userDTO.getCurrentPassword();
            boolean dtoContainsData = userMailAccountFromDTO != null && userCurrentPasswordFromDTO != null;

            if (dtoContainsData && userMailAccountFromDTO.equals(email)
                    && BCrypt.checkpw(password, userCurrentPasswordFromDTO)) {
                return userDTO;
            }
        }
        return null;
    }

    private UserDTO getUserDTOByToken(UserToken userToken) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userDTOs == null || userDTOs.size() == 0)
            return null;

        for (UserDTO userDTO : userRepository.getAllUserDtos()) {
            String userIdFromDTO = userDTO.getUserId();
            String userMailAccountFromDTO = userDTO.getMailAccount();
            boolean dtoContainsData = userIdFromDTO != null && userMailAccountFromDTO != null;

            if (dtoContainsData && userIdFromDTO.equals(userToken.getUserId())
                    && userMailAccountFromDTO.equals(userToken.getMailAccount())) {
                return userDTO;
            }
        }
        return null;
    }

    @Override
    public boolean register(String emailAccount, String password, String confirmationPassword, FormType formType,
            AddOperationType addTypeOperation) {
        UserDTO userDTO;

        UserToken userToken = getLoggedUser();
        boolean isAddOperationTypeSupported = addTypeOperation != null
                && EnumSet.of(AddOperationType.ANOTHERACCOUNT, AddOperationType.NEWACCOUNT).contains(addTypeOperation);
        boolean isFormTypeSupported = isFormSupported(formType, EnumSet.of(FormType.REGISTER, FormType.ADDACCOUNT));

        if (!isFormTypeSupported) {
            errorHandler.logError(errorHandler.createErrorBody("formType", "Provided unsupported type of form."));
            return false;
        }

        if (!isAddOperationTypeSupported) {
            errorHandler.logError(errorHandler.createErrorBody("operationType",
                    "Operation with type " + addTypeOperation + " is not supported."));
            return false;
        }

        if (FormType.ADDACCOUNT == formType && AddOperationType.ANOTHERACCOUNT == addTypeOperation) {
            boolean isValid = !containsDataNull("register function",
                    new LabeledValue("token", checkUserToken(userToken)))
                    && validateData(OperationType.CREATE, null, emailAccount,
                            getUserDTOByToken(userToken).getCurrentPassword(), password, confirmationPassword, formType,
                            userRepository.getAllUserDtos());

            if (!isValid)
                return false;

            userDTO = creaUserDTO(null, userToken.getGroupId(), null, emailAccount, password, confirmationPassword,
                    null);
            userRepository.addUser(userDTO, addTypeOperation);

            if (getUserDTOByEmailAndPassword(userDTO.getMailAccount(), userDTO.getPassword()) != null) {
                clearErrors(errorHandler, "token", "formType", "operationType", "email", "password", "confirmPassword");
                return true;
            }

            return false;
        }

        if (FormType.REGISTER == formType && AddOperationType.NEWACCOUNT == addTypeOperation) {
            boolean isValid = validateData(OperationType.CREATE, null, emailAccount, null, password,
                    confirmationPassword, formType, userRepository.getAllUserDtos());

            if (!isValid)
                return false;

            userDTO = creaUserDTO(null, null, null, emailAccount, password, confirmationPassword, null);
            userRepository.addUser(userDTO, addTypeOperation);

            if (getUserDTOByEmailAndPassword(userDTO.getMailAccount(), userDTO.getPassword()) != null) {
                clearErrors(errorHandler, "token", "formType", "operationType", "email", "password", "confirmPassword");
                return true;
            }

            return false;
        }

        return false;
    }

    @Override
    public void login(String emailAccount, String password) {
        UserDTO userDTO = getUserDTOByEmailAndPassword(emailAccount, password);

        if (!containsDataNull("login function", new LabeledValue("dto", userDTO)) && userDTO.getUserId() != null
                && !sessionService.isUserLoggedIn(userDTO.getUserId())) {
            clearErrors(errorHandler, "dto");
            currentSessionId = sessionService.createSessionId(userDTO);
        }
    }

    @Override
    public boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationNewPassword, FormType formType) {
        UserDTO foundUserDTO = getUserDTOByEmailAndPassword(emailAccount, password);
        boolean isFormTypeSupported = isFormSupported(formType, EnumSet.of(FormType.FORGOTCREDENTIALS));

        if (!isFormTypeSupported) {
            errorHandler.logError(errorHandler.createErrorBody("formType", "Provided unsupported type of form."));
            return false;
        }

        boolean containsNull = containsDataNull("updateNotLoggedAccount function",
                new LabeledValue("dto", foundUserDTO));
        if (containsNull)
            return false;

        boolean validPasswords = validatePasswords(foundUserDTO.getMailAccount(), foundUserDTO.getCurrentPassword(),
                newPassword, confirmationNewPassword, formType);
        if (validPasswords) {
            UserDTO updatedUserDTO = creaUserDTO(foundUserDTO.getUserId(), foundUserDTO.getGroupId(),
                    foundUserDTO.getCurrentPassword(), foundUserDTO.getMailAccount(), newPassword,
                    confirmationNewPassword, foundUserDTO.getProfileImage());

            userRepository.updateUser(foundUserDTO, updatedUserDTO);

            if (getUserDTOByEmailAndPassword(updatedUserDTO.getMailAccount(), updatedUserDTO.getPassword()) != null) {
                clearErrors(errorHandler, "dto", "newPassword", "confirmNewPassword");
                return true;
            }

            return false;
        }

        return false;
    }

    @Override
    public boolean switchAccount(UserDTO switchToUserDTO) {
        UserToken userToken = getLoggedUser();

        boolean containsNull = containsDataNull("switchAccount function",
                new LabeledValue("token", checkUserToken(userToken)), new LabeledValue("dto", switchToUserDTO));

        if (containsNull)
            return false;

        String userId = resolveIdByEmail(switchToUserDTO.getMailAccount());
        if (userId != null && !userToken.getMailAccount().equals(switchToUserDTO.getMailAccount())
                && userToken.getGroupId().equals(switchToUserDTO.getGroupId())) {
            UserDTO switchToUserDTOwithId = creaUserDTO(userId, userToken.getGroupId(),
                    switchToUserDTO.getCurrentPassword(), switchToUserDTO.getMailAccount(),
                    switchToUserDTO.getPassword(), switchToUserDTO.getConfirmPassword(),
                    switchToUserDTO.getProfileImage());

            clearErrors(errorHandler, "dto", "token");
            logOut();
            currentSessionId = sessionService.createSessionId(switchToUserDTOwithId);
            return true;
        }

        return false;
    }

    @Override
    public boolean updateLoggedInAccount(String newPassword, String confirmationNewPassword, FormType formType) {
        UserDTO foundUserDTO;
        UserToken userToken = getLoggedUser();
        boolean isFormTypeSupported = isFormSupported(formType, EnumSet.of(FormType.FORGOTCREDENTIALS));

        if (!isFormTypeSupported) {
            errorHandler.logError(errorHandler.createErrorBody("formType", "Provided unsupported type of form."));
            return false;
        }

        boolean containsNullUserToken = containsDataNull("updateLoggedInAccount function",
                new LabeledValue("token", checkUserToken(userToken)));

        if (containsNullUserToken)
            return false;

        boolean containsNullDTO = containsDataNull("updateLoggedInAccount function",
                new LabeledValue("dto", getUserDTOByToken(userToken)));

        if (containsNullDTO)
            return false;
        else
            foundUserDTO = getUserDTOByToken(userToken);

        boolean validPasswords = validatePasswords(foundUserDTO.getMailAccount(), foundUserDTO.getCurrentPassword(),
                newPassword, confirmationNewPassword, formType);
        if (validPasswords) {
            UserDTO updatedUserDTO = creaUserDTO(foundUserDTO.getUserId(), foundUserDTO.getGroupId(),
                    foundUserDTO.getCurrentPassword(), foundUserDTO.getMailAccount(), newPassword,
                    confirmationNewPassword, foundUserDTO.getProfileImage());

            userRepository.updateUser(foundUserDTO, updatedUserDTO);

            if (getUserDTOByEmailAndPassword(updatedUserDTO.getMailAccount(), updatedUserDTO.getPassword()) != null) {
                clearErrors(errorHandler, "token", "dto", "newPassword", "confirmNewPassword");
                return true;
            }

            return false;
        }

        return false;
    }

    @Override
    public void logOut() {
        sessionService.removeSession(currentSessionId);
        currentSessionId = null;
    }

    @Override
    public UserToken getLoggedUser() {
        return sessionService.getUserTokenBySessionId(currentSessionId);
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}

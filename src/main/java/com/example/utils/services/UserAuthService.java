package com.example.utils.services;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

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
import com.example.utils.enums.TokenField;
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
            if (labeledValue.value instanceof Optional<?> opt) {
                if (opt.isEmpty()) {
                    errorHandler.logError(errorHandler.createErrorBody(labeledValue.label(),
                            "Invalid " + labeledValue.label() + " argument in " + location + "."));
                    return true;
                }
            } else {
                if (labeledValue.value == null) {
                    errorHandler.logError(errorHandler.createErrorBody(labeledValue.label(),
                            "Invalid " + labeledValue.label() + " argument in " + location + "."));
                    return true;
                }
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

    private UserToken checkUserToken(UserToken userToken, EnumSet<TokenField> tokenFieldsToCheck) {
        boolean validEmailField = false;
        boolean validUserIdField = false;
        boolean validGroupIdField = false;

        if (userToken == null)
            return null;

        for (TokenField tokenField : tokenFieldsToCheck) {
            if (TokenField.EMAILACCOUNT == tokenField)
                validEmailField = userToken.getMailAccount() != null ? true : false;

            if (TokenField.USERID == tokenField)
                validUserIdField = userToken.getUserId() != null ? true : false;

            if (!validGroupIdField && TokenField.GROUPID == tokenField)
                validGroupIdField = userToken.getGroupId() != null ? true : false;
        }

        return validEmailField && validGroupIdField && validUserIdField ? userToken : null;
    }

    private Optional<String> resolveIdByEmail(String emailAccount) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userDTOs == null || userDTOs.isEmpty() || emailAccount == null)
            return null;

        Optional<UserDTO> optionalFoundUserDTO = userDTOs.stream()
                .filter(userDTO -> userDTO.getMailAccount() != null && userDTO.getMailAccount().equals(emailAccount))
                .findFirst();

        return optionalFoundUserDTO.flatMap(userDTO -> Optional.ofNullable(userDTO.getUserId()));
    }

    private Optional<String> resolveGroupIdByUserIdAndEmail(String userId, String emailAccount) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userDTOs == null || userDTOs.isEmpty() || userId == null || emailAccount == null)
            return null;

        Optional<UserDTO> optionalFoundUserDTO = userDTOs.stream()
                .filter(userDTO -> userDTO.getUserId() != null && userDTO.getMailAccount() != null
                        && userDTO.getUserId().equals(userId) && userDTO.getMailAccount().equals(emailAccount))
                .findFirst();

        return optionalFoundUserDTO.flatMap(userDTO -> Optional.ofNullable(userDTO.getGroupId()));
    }

    private Optional<UserDTO> getUserDTOByEmailAndPassword(String emailAccount, String password) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userDTOs == null || userDTOs.isEmpty() || emailAccount == null || password == null)
            return null;

        Optional<UserDTO> optionalFoundUserDTO = userDTOs.stream()
                .filter(userDTO -> userDTO.getMailAccount() != null && userDTO.getCurrentPassword() != null
                        && userDTO.getMailAccount().equals(emailAccount)
                        && BCrypt.checkpw(password, userDTO.getCurrentPassword()))
                .findFirst();

        return optionalFoundUserDTO;
    }

    private Optional<UserDTO> getUserDTOByToken(UserToken userToken) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userDTOs == null || userDTOs.isEmpty())
            return null;

        Optional<UserDTO> optionalFoundUserDTO = userDTOs.stream()
                .filter(userDTO -> userDTO.getUserId() != null && userDTO.getMailAccount() != null
                        && userDTO.getGroupId() != null && userDTO.getUserId().equals(userToken.getUserId())
                        && userDTO.getMailAccount().equals(userToken.getMailAccount())
                        && userDTO.getGroupId().equals(userToken.getGroupId()))
                .findFirst();

        return optionalFoundUserDTO;
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

    @Override
    public boolean register(String emailAccount, String password, String confirmationPassword, FormType formType,
            AddOperationType addTypeOperation) {
        UserDTO userDTO;
        final UserToken userToken = getLoggedUser();
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
            boolean containsNull = containsDataNull("register function",
                    new LabeledValue("token", checkUserToken(userToken,
                            EnumSet.of(TokenField.EMAILACCOUNT, TokenField.GROUPID, TokenField.USERID))));
            if (containsNull)
                return false;

            Optional<UserDTO> foundUserDTO = getUserDTOByToken(userToken);
            boolean isValid = validateData(OperationType.CREATE, null, emailAccount,
                    foundUserDTO.isPresent() ? foundUserDTO.get().getCurrentPassword() : null, password,
                    confirmationPassword, formType, userRepository.getAllUserDtos());
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
        final Optional<UserDTO> userDTO = getUserDTOByEmailAndPassword(emailAccount, password);

        if (containsDataNull("login function", new LabeledValue("dto", userDTO)))
            return;

        if (userDTO.isPresent() && userDTO.get().getUserId() != null
                && !sessionService.isUserLoggedIn(userDTO.get().getUserId())) {
            clearErrors(errorHandler, "dto");
            currentSessionId = sessionService.createSessionId(userDTO.get());
        }
    }

    @Override
    public boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationNewPassword, FormType formType) {
        final Optional<UserDTO> foundUserDTO = getUserDTOByEmailAndPassword(emailAccount, password);
        boolean isFormTypeSupported = isFormSupported(formType, EnumSet.of(FormType.FORGOTCREDENTIALS));

        if (!isFormTypeSupported) {
            errorHandler.logError(errorHandler.createErrorBody("formType", "Provided unsupported type of form."));
            return false;
        }

        boolean containsNull = containsDataNull("updateNotLoggedAccount function",
                new LabeledValue("dto", foundUserDTO));
        if (containsNull)
            return false;

        boolean validPasswords = validatePasswords(foundUserDTO.get().getMailAccount(),
                foundUserDTO.get().getCurrentPassword(), newPassword, confirmationNewPassword, formType);
        if (validPasswords) {
            UserDTO updatedUserDTO = creaUserDTO(foundUserDTO.get().getUserId(), foundUserDTO.get().getGroupId(),
                    foundUserDTO.get().getCurrentPassword(), foundUserDTO.get().getMailAccount(), newPassword,
                    confirmationNewPassword, foundUserDTO.get().getProfileImage());

            userRepository.updateUser(foundUserDTO.get(), updatedUserDTO);

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
        final UserToken userToken = getLoggedUser();
        final Optional<String> resolvedGroupId;
        final Optional<String> resolvedUserId;

        boolean containsNull = containsDataNull("switchAccount function",
                new LabeledValue("token",
                        checkUserToken(userToken,
                                EnumSet.of(TokenField.EMAILACCOUNT, TokenField.GROUPID, TokenField.USERID))),
                new LabeledValue("dto", switchToUserDTO));

        if (containsNull)
            return false;

        resolvedUserId = resolveIdByEmail(switchToUserDTO.getMailAccount());

        if (resolvedUserId.isEmpty() || userToken.getMailAccount().equals(switchToUserDTO.getMailAccount())) {
            return false;
        }

        resolvedGroupId = resolveGroupIdByUserIdAndEmail(resolvedUserId.get(), switchToUserDTO.getMailAccount());

        if (resolvedGroupId.filter(gid -> gid.equals(userToken.getGroupId())).isEmpty())
            return false;

        UserDTO switchToUserDTOwithId = creaUserDTO(resolvedUserId.get(), resolvedGroupId.get(),
                switchToUserDTO.getCurrentPassword(), switchToUserDTO.getMailAccount(), switchToUserDTO.getPassword(),
                switchToUserDTO.getConfirmPassword(), switchToUserDTO.getProfileImage());

        clearErrors(errorHandler, "dto", "token");
        logOut();
        currentSessionId = sessionService.createSessionId(switchToUserDTOwithId);
        return true;
    }

    @Override
    public boolean updateLoggedInAccount(String newPassword, String confirmationNewPassword, FormType formType) {
        final UserToken userToken = getLoggedUser();
        final Optional<UserDTO> foundUserDTO;
        boolean isFormTypeSupported = isFormSupported(formType, EnumSet.of(FormType.FORGOTCREDENTIALS));

        if (!isFormTypeSupported) {
            errorHandler.logError(errorHandler.createErrorBody("formType", "Provided unsupported type of form."));
            return false;
        }

        boolean containsNullUserToken = containsDataNull("updateLoggedInAccount function", new LabeledValue("token",
                checkUserToken(userToken, EnumSet.of(TokenField.EMAILACCOUNT, TokenField.USERID, TokenField.GROUPID))));

        if (containsNullUserToken)
            return false;

        foundUserDTO = getUserDTOByToken(userToken);
        boolean containsNullDTO = containsDataNull("updateLoggedInAccount function",
                new LabeledValue("dto", foundUserDTO));

        if (containsNullDTO)
            return false;

        boolean validPasswords = validatePasswords(foundUserDTO.get().getMailAccount(),
                foundUserDTO.get().getCurrentPassword(), newPassword, confirmationNewPassword, formType);
        if (validPasswords) {
            UserDTO updatedUserDTO = creaUserDTO(foundUserDTO.get().getUserId(), foundUserDTO.get().getGroupId(),
                    foundUserDTO.get().getCurrentPassword(), foundUserDTO.get().getMailAccount(), newPassword,
                    confirmationNewPassword, foundUserDTO.get().getProfileImage());

            userRepository.updateUser(foundUserDTO.get(), updatedUserDTO);

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

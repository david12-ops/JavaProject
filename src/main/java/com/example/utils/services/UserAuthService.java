package com.example.utils.services;

import java.util.EnumSet;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;
import com.example.utils.RepositoryFactory;
import com.example.utils.ValidationContext;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.FormType;
import com.example.utils.enums.OperationType;
import com.example.utils.enums.ValidationMode;
import com.example.utils.interfaces.AuthService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.UserValidator;

public class UserAuthService implements AuthService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.USER);
    private final UserRepository userRepository = RepositoryFactory.getUserRepository();

    private SessionService sessionService;
    private ErrorHandler errorHandler;
    private UserValidator userValidator;
    private String currentSessionId;

    public UserAuthService(SessionService sessionService) {
        this.errorHandler = validationContext.getUserValidationBundle().getErrorManager();
        this.userValidator = validationContext.getUserValidationBundle().getValidator();
        this.sessionService = sessionService;
    }

    private record LabeledValue(String label, Object value) {
    }

    // Support Methods
    private boolean containsDataNull(String errorKey, LabeledValue... labeledValues) {
        for (LabeledValue labeledValue : labeledValues) {
            if (labeledValue.value == null) {
                errorHandler.logError(errorHandler.createErrorBody(errorKey, "Invalid " + labeledValue.label()));
                return true;
            }
        }
        return false;
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
            String confirmationPassword, FormType form) {
        return userValidator.validPassword(currentPassword, password, email, form)
                && userValidator.confirmedPassword(password, confirmationPassword, form);
    }

    private boolean validateData(OperationType operation, String currentEmail, String newEmail, String currentPassword,
            String password, String confirmationPassword, FormType form, List<UserDTO> userDTOs) {
        return userValidator.validEmail(newEmail)
                && userValidator.nonDuplicateUserWithEmail(operation, currentEmail, newEmail, userDTOs)
                && validatePasswords(newEmail, currentPassword, password, confirmationPassword, form);

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
        boolean isFormTypeSupported;

        isFormTypeSupported = isFormSupported(formType, EnumSet.of(FormType.REGISTER, FormType.ADDACCOUNT));

        if (isFormTypeSupported) {
            UserToken userToken = getLoggedUser();

            if (addTypeOperation == AddOperationType.ANOTHERACCOUNT) {
                boolean isValid = !containsDataNull("register", new LabeledValue("token", checkUserToken(userToken)))
                        && validateData(OperationType.CREATE, null, emailAccount,
                                getUserDTOByToken(userToken).getCurrentPassword(), password, confirmationPassword,
                                formType, userRepository.getAllUserDtos());

                if (!isValid)
                    return false;

                userDTO = creaUserDTO(null, userToken.getGroupId(), null, emailAccount, password, confirmationPassword,
                        null);
                userRepository.addUser(userDTO, addTypeOperation);
                return getUserDTOByEmailAndPassword(userDTO.getMailAccount(), userDTO.getPassword()) != null ? true
                        : false;
            }

            if (addTypeOperation == AddOperationType.NEWACCOUNT && validateData(OperationType.CREATE, null,
                    emailAccount, null, password, confirmationPassword, formType, userRepository.getAllUserDtos())) {

                userDTO = creaUserDTO(null, null, null, emailAccount, password, confirmationPassword, null);
                userRepository.addUser(userDTO, addTypeOperation);
                return getUserDTOByEmailAndPassword(userDTO.getMailAccount(), userDTO.getPassword()) != null ? true
                        : false;
            }
        }

        errorHandler.logError(errorHandler.createErrorBody("register", "Form " + formType + " is not supported."));
        return false;
    }

    @Override
    public void login(String emailAccount, String password) {
        UserDTO userDTO = getUserDTOByEmailAndPassword(emailAccount, password);

        if (!containsDataNull("login", new LabeledValue("dto", userDTO)) && userDTO.getUserId() != null
                && !sessionService.isUserLoggedIn(userDTO.getUserId())) {
            currentSessionId = sessionService.createSessionId(userDTO);
        }
    }

    @Override
    public boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationNewPassword, FormType formType) {
        UserDTO foundUserDTO = getUserDTOByEmailAndPassword(emailAccount, password);

        boolean containsNull = containsDataNull("updateNotLoggedAccount", new LabeledValue("dto", foundUserDTO));
        if (containsNull)
            return false;

        if (validatePasswords(foundUserDTO.getMailAccount(), foundUserDTO.getCurrentPassword(), newPassword,
                confirmationNewPassword, formType)) {
            UserDTO updatedUserDTO = creaUserDTO(foundUserDTO.getUserId(), foundUserDTO.getGroupId(),
                    foundUserDTO.getCurrentPassword(), foundUserDTO.getMailAccount(), newPassword,
                    confirmationNewPassword, foundUserDTO.getProfileImage());

            userRepository.updateUser(foundUserDTO, updatedUserDTO);
            return getUserDTOByEmailAndPassword(updatedUserDTO.getMailAccount(),
                    updatedUserDTO.getConfirmPassword()) != null ? true : false;
        }

        return false;
    }

    @Override
    public boolean switchAccount(UserDTO switchToUserDTO) {
        UserToken userToken = getLoggedUser();

        boolean containsNull = containsDataNull("switchAccount", new LabeledValue("token", checkUserToken(userToken)),
                new LabeledValue("dto", switchToUserDTO));

        if (containsNull)
            return false;

        String userId = resolveIdByEmail(switchToUserDTO.getMailAccount());

        if (userId != null && !userToken.getMailAccount().equals(switchToUserDTO.getMailAccount())) {
            UserDTO switchToUserDTOwithId = creaUserDTO(userId, userToken.getGroupId(),
                    switchToUserDTO.getCurrentPassword(), switchToUserDTO.getMailAccount(),
                    switchToUserDTO.getPassword(), switchToUserDTO.getConfirmPassword(),
                    switchToUserDTO.getProfileImage());

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

        boolean containsNull = containsDataNull("switchAccount", new LabeledValue("token", checkUserToken(userToken)),
                new LabeledValue("dto", getUserDTOByToken(userToken)));

        if (containsNull)
            return false;
        else
            foundUserDTO = getUserDTOByToken(userToken);

        if (validatePasswords(foundUserDTO.getMailAccount(), foundUserDTO.getCurrentPassword(), newPassword,
                confirmationNewPassword, formType)) {
            UserDTO updatedUserDTO = creaUserDTO(foundUserDTO.getUserId(), foundUserDTO.getGroupId(),
                    foundUserDTO.getCurrentPassword(), foundUserDTO.getMailAccount(), newPassword,
                    confirmationNewPassword, foundUserDTO.getProfileImage());

            userRepository.updateUser(foundUserDTO, updatedUserDTO);
            return getUserDTOByEmailAndPassword(updatedUserDTO.getMailAccount(),
                    updatedUserDTO.getConfirmPassword()) != null ? true : false;
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

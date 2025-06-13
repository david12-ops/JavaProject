package com.example.utils.services;

import java.util.EnumSet;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dto.UserDTO;
import com.example.model.UserRepository;
import com.example.model.UserToken;
import com.example.utils.ValidationContext;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.FormType;
import com.example.utils.enums.OperationType;
import com.example.utils.enums.ValidationMode;
import com.example.utils.interfaces.AuthService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.UserValidator;

public class UserAuthService implements AuthService {
    private SessionService sessionService;
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.USER);
    private ErrorHandler errorHandler;
    private UserValidator userValidator;

    private String currentSessionId;

    public UserAuthService(SessionService sessionService) {
        this.errorHandler = validationContext.getUserValidationBundle().getErrorManager();
        this.userValidator = validationContext.getUserValidationBundle().getValidator();
        this.sessionService = sessionService;
    }

    private boolean isFormSupported(FormType formType, EnumSet supportedTypes) {
        return supportedTypes.contains(formType);
    }

    private UserDTO creaUserDTO(String userId, String groupId, String currentPassword, String mailAccount,
            String password, String confirmationPassword, String profileImage) {
        return new UserDTO(userId, groupId, mailAccount, currentPassword, confirmationPassword, password, profileImage);
    }

    // Support Methods
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

    private UserDTO getUserByEmailAndPassword(String email, String password, List<UserDTO> userDTOs) {
        for (UserDTO userDTO : userDTOs) {
            if (userDTO.getMailAccount().equals(email) && BCrypt.checkpw(password, userDTO.getCurrentPassword())) {
                return userDTO;
            }
        }
        return null;
    }

    private UserDTO getUserByToken(UserToken userToken, List<UserDTO> userDTOs) {
        for (UserDTO userDTO : userDTOs) {
            if (userDTO.getUserId().equals(userToken.getUserId())
                    && userDTO.getMailAccount().equals(userToken.getMailAccount())) {
                return userDTO;
            }
        }
        return null;
    }

    @Override
    public boolean register(String emailAccount, String password, String confirmationPassword, FormType formType,
            AddOperationType addTypeOperation, UserRepository userRepository) {

        UserDTO userDTO;
        boolean isFormTypeSupported;

        isFormTypeSupported = isFormSupported(formType, EnumSet.of(FormType.REGISTER, FormType.ADDACCOUNT));

        if (isFormTypeSupported) {
            UserToken userToken = getLoggedUser();
            if (addTypeOperation == AddOperationType.ANOTHERACCOUNT && userToken != null
                    && validateData(OperationType.CREATE, null, emailAccount,
                            getUserByToken(userToken, userRepository.getAllUserDtos()).getCurrentPassword(), password,
                            confirmationPassword, formType, userRepository.getAllUserDtos())) {

                userDTO = creaUserDTO(null, userToken.getGroupId(), null, emailAccount, password, confirmationPassword,
                        null);
                userRepository.addUser(userDTO, addTypeOperation);
                return getUserByEmailAndPassword(userDTO.getMailAccount(), userDTO.getPassword(),
                        userRepository.getAllUserDtos()) != null ? true : false;
            }

            if (addTypeOperation == AddOperationType.NEWACCOUNT && validateData(OperationType.CREATE, null,
                    emailAccount, null, password, confirmationPassword, formType, userRepository.getAllUserDtos())) {

                userDTO = creaUserDTO(null, null, null, emailAccount, password, confirmationPassword, null);
                userRepository.addUser(userDTO, addTypeOperation);
                return getUserByEmailAndPassword(userDTO.getMailAccount(), userDTO.getPassword(),
                        userRepository.getAllUserDtos()) != null ? true : false;
            }
        }

        errorHandler.logError(errorHandler.createErrorBody("register", "Form " + formType + " is not supported."));
        return false;
    }

    @Override
    public void login(String emailAccount, String password, UserRepository userRepository) {
        UserDTO userDTO = getUserByEmailAndPassword(emailAccount, password, userRepository.getAllUserDtos());

        if (userDTO != null && userDTO.getUserId() != null && !sessionService.isUserLoggedIn(userDTO.getUserId())) {
            currentSessionId = sessionService.createSessionId(userDTO);
        }
    }

    @Override
    public boolean updateNotLoggedAccount(String emailAccount, String password, String newPassword,
            String confirmationNewPassword, FormType formType, UserRepository userRepository) {
        UserDTO foundUserDTO = getUserByEmailAndPassword(emailAccount, password, userRepository.getAllUserDtos());

        if (foundUserDTO == null) {
            errorHandler.logError(
                    errorHandler.createErrorBody("updateNotLoggedAccount", "Invalid credentials — user not found."));
            return false;
        }

        if (validatePasswords(foundUserDTO.getMailAccount(), foundUserDTO.getCurrentPassword(), newPassword,
                confirmationNewPassword, formType)) {
            foundUserDTO.setPassword(newPassword);
            foundUserDTO.setConfirmPassword(confirmationNewPassword);

            userRepository.updateUser(foundUserDTO, FormType.FORGOTCREDENTIALS);
            return getUserByEmailAndPassword(foundUserDTO.getMailAccount(), foundUserDTO.getPassword(),
                    userRepository.getAllUserDtos()) != null ? true : false;
        }

        return false;
    }

    @Override
    public boolean switchAccount(UserDTO switchToUserDTO) {
        UserToken userToken = getLoggedUser();

        if (userToken != null && switchToUserDTO != null
                && !userToken.getMailAccount().equals(switchToUserDTO.getMailAccount())) {
            logOut();
            currentSessionId = sessionService.createSessionId(switchToUserDTO);
            return true;
        }

        return false;
    }

    @Override
    public boolean updateLoggedInAccount(String newPassword, String confirmationNewPassword, FormType formType,
            UserRepository userRepository) {
        UserDTO foundUserDTO = getUserByToken(getLoggedUser(), userRepository.getAllUserDtos());

        if (foundUserDTO == null) {
            errorHandler.logError(
                    errorHandler.createErrorBody("updateLoggedInAccount", "Invalid credentials — user not found"));
            return false;
        }

        if (validatePasswords(foundUserDTO.getMailAccount(), foundUserDTO.getCurrentPassword(), newPassword,
                confirmationNewPassword, formType)) {
            foundUserDTO.setPassword(newPassword);
            foundUserDTO.setConfirmPassword(confirmationNewPassword);

            userRepository.updateUser(foundUserDTO, FormType.FORGOTCREDENTIALS);
            return getUserByEmailAndPassword(foundUserDTO.getMailAccount(), foundUserDTO.getPassword(),
                    userRepository.getAllUserDtos()) != null ? true : false;
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

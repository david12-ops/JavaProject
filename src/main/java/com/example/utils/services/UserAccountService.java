package com.example.utils.services;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;
import com.example.utils.FileConvertor;
import com.example.utils.RepositoryFactory;
import com.example.utils.ValidationContext;
import com.example.utils.enums.ValidationMode;
import com.example.utils.enums.ViewLevel;
import com.example.utils.interfaces.AccountService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.UserValidator;

import javafx.scene.image.Image;

public class UserAccountService implements AccountService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.USER);
    private final UserRepository userRepository = RepositoryFactory.getUserRepository();
    private ErrorHandler errorHandler;
    private UserValidator userValidator;

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

    private UserToken checkUserToken(UserToken userToken) {
        if (userToken == null || userToken.getMailAccount() == null || userToken.getUserId() == null) {
            return null;
        }

        return userToken;
    }

    private UserDTO creaUserDTO(String userId, String groupId, String currentPassword, String mailAccount,
            String password, String confirmationPassword, String profileImage) {
        return new UserDTO(userId, groupId, mailAccount, currentPassword, confirmationPassword, password, profileImage);
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

    private UserDTO getUserDTOByToken(UserToken userToken) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        if (userDTOs == null || userDTOs.size() == 0)
            return null;

        for (UserDTO userDTO : userDTOs) {
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

    public UserAccountService() {
        this.errorHandler = validationContext.getUserValidationBundle().getErrorManager();
        this.userValidator = validationContext.getUserValidationBundle().getValidator();
    }

    @Override
    public boolean removeAccount(UserToken userToken, UserDTO userDTO) {
        boolean containsNull = containsDataNull("removeAccount", new LabeledValue("token", checkUserToken(userToken)),
                new LabeledValue("dto", userDTO));

        if (containsNull)
            return false;

        if (userToken.getUserId().equals(userDTO.getUserId())
                && userToken.getMailAccount().equals(userDTO.getMailAccount())) {
            return false;
        }

        String userId = resolveIdByEmail(userDTO.getMailAccount());
        UserDTO userDTOwithUserId = creaUserDTO(userId, userDTO.getGroupId(), userDTO.getMailAccount(),
                userDTO.getCurrentPassword(), userDTO.getPassword(), userDTO.getConfirmPassword(),
                userDTO.getProfileImage());

        userRepository.removeUser(userDTOwithUserId);
        return !userRepository.getAllUserDtos().contains(userDTOwithUserId);
    }

    @Override
    public void updateImageProfile(UserToken userToken, File file) {
        UserDTO founUserDTO = getUserDTOByToken(userToken);

        boolean containsNull = containsDataNull("updateImageProfile",
                new LabeledValue("token", checkUserToken(userToken)), new LabeledValue("dto", founUserDTO));

        if (containsNull)
            return;

        boolean isValid = userValidator.validProfileImage(file);

        if (isValid && file == null) {
            userRepository.updateUser(founUserDTO, (File) null);
        }

        if (isValid && file != null) {
            userRepository.updateUser(founUserDTO, file);
        }
    }

    @Override
    public List<UserDTO> getAllUserAccounts(UserToken userToken) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();
        boolean containsNull = containsDataNull("getAllUserAccounts",
                new LabeledValue("token", checkUserToken(userToken)), new LabeledValue("DTOs", userDTOs));

        if (containsNull)
            return null;

        userDTOs = userDTOs.stream().filter(userDTO -> !userDTO.getUserId().equals(userToken.getUserId())
                && userDTO.getGroupId().equals(userToken.getGroupId())).toList();
        userDTOs.forEach(userDTO -> userDTO.sanitize(ViewLevel.PUBLIC));

        return userDTOs;
    }

    @Override
    public Image getImageProfile(UserToken userToken) {
        boolean containsNull = containsDataNull("getImageProfile",
                new LabeledValue("token", checkUserToken(userToken)));

        if (containsNull)
            return null;

        Optional<UserDTO> foundUserDTO = userRepository.getAllUserDtos().stream()
                .filter(userDTO -> userDTO.getGroupId().equals(userToken.getGroupId())
                        && userDTO.getMailAccount().equals(userToken.getMailAccount()))
                .findAny();

        if (foundUserDTO.isPresent() && foundUserDTO.get().getProfileImage() != null) {
            return FileConvertor.base64ToImage(foundUserDTO.get().getProfileImage());
        }
        return null;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}

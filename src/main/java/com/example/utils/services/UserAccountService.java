package com.example.utils.services;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;
import com.example.utils.FileConvertor;
import com.example.utils.ValidationContext;
import com.example.utils.enums.ValidationMode;
import com.example.utils.enums.ViewLevel;
import com.example.utils.interfaces.AccountService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.UserValidator;

import javafx.scene.image.Image;

public class UserAccountService implements AccountService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.USER);
    private ErrorHandler errorHandler;
    private UserValidator userValidator;

    private record LabeledValue(String label, Object value) {
    }

    private boolean containsDataNull(String errorKey, LabeledValue... labeledValues) {
        for (LabeledValue labeledValue : labeledValues) {
            if (labeledValue.value == null) {
                errorHandler.logError(errorHandler.createErrorBody(errorKey, "Invalid " + labeledValue.label()));
                return true;
            }
        }
        return false;
    }

    private UserDTO getUserDTOByToken(UserToken userToken, List<UserDTO> userDTOs) {
        for (UserDTO userDTO : userDTOs) {
            if (userDTO.getUserId().equals(userToken.getUserId())
                    && userDTO.getMailAccount().equals(userToken.getMailAccount())) {
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
    public boolean removeAccount(UserToken userToken, UserDTO userDTO, UserRepository userRepository) {
        boolean containsNull = containsDataNull("removeAccount", new LabeledValue("token", userToken),
                new LabeledValue("dto", userDTO));
        if (containsNull)
            return false;

        if (userToken.getUserId().equals(userDTO.getUserId())
                && userToken.getMailAccount().equals(userDTO.getMailAccount())) {
            return false;
        }
        userRepository.removeUser(userDTO);
        return !userRepository.getAllUserDtos().contains(userDTO);
    }

    @Override
    public void updateImageProfile(UserToken userToken, File file, UserRepository userRepository) {
        UserDTO userDTO;
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();

        boolean containsNull = containsDataNull("updateImageProfile", new LabeledValue("token", userToken),
                new LabeledValue("dto", getUserDTOByToken(userToken, userDTOs)));
        if (containsNull)
            return;
        else
            userDTO = getUserDTOByToken(userToken, userDTOs);

        boolean isValid = userValidator.validProfileImage(file);

        if (isValid && file == null) {
            userRepository.updateUser(userDTO, (File) null);
        }

        if (isValid && file != null) {
            userRepository.updateUser(userDTO, file);
        }
    }

    @Override
    public List<UserDTO> getAllUserAccounts(UserToken userToken, UserRepository userRepository) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();
        boolean containsNull = containsDataNull("getAllUserAccounts", new LabeledValue("token", userToken),
                new LabeledValue("DTOs", userDTOs));

        if (containsNull)
            return null;

        userDTOs = userDTOs.stream().filter(userDTO -> !userDTO.getUserId().equals(userToken.getUserId())
                && userDTO.getGroupId().equals(userToken.getGroupId())).toList();
        userDTOs.forEach(userDTO -> userDTO.sanitize(ViewLevel.PUBLIC));

        return userDTOs;
    }

    @Override
    public Image getImageProfile(UserToken userToken, UserRepository userRepository) {
        boolean containsNull = containsDataNull("getImageProfile", new LabeledValue("token", userToken));
        if (containsNull)
            return null;

        Optional<UserDTO> foundUserDTO = userRepository.getAllUserDtos().stream()
                .filter(userDTO -> userDTO.getGroupId().equals(userToken.getGroupId())
                        && userDTO.getMailAccount().equals(userToken.getMailAccount()))
                .findAny();

        if (foundUserDTO.isPresent() && foundUserDTO.get().getProfileImage() != null) {
            return FileConvertor.Base64ToImage(foundUserDTO.get().getProfileImage());
        }
        return null;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}

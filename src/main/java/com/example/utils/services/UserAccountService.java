package com.example.utils.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;
import com.example.utils.FileConvertor;
import com.example.utils.RepositoryFactory;
import com.example.utils.ValidationContext;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.ValidationMode;
import com.example.utils.enums.ViewLevel;
import com.example.utils.interfaces.AccountService;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.UserValidator;

import javafx.scene.image.Image;

public class UserAccountService implements AccountService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.USER);
    private final UserRepository userRepository;

    private final ErrorHandler errorHandler;
    private final UserValidator userValidator;

    public UserAccountService(EnvironmentType environmentType) {
        this.userRepository = RepositoryFactory.getUserRepository(environmentType);
        this.errorHandler = validationContext.getUserValidationBundle().getErrorManager();
        this.userValidator = validationContext.getUserValidationBundle().getValidator();

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

    @Override
    public boolean removeAccount(UserToken userToken, UserDTO userDTO) {
        boolean containsNull = containsDataNull("removeAccount function",
                new LabeledValue("token", checkUserToken(userToken)), new LabeledValue("dto", userDTO));

        if (containsNull)
            return false;

        String userId = resolveIdByEmail(userDTO.getMailAccount());
        if (userId == null || userToken.getUserId().equals(userDTO.getUserId())
                && userToken.getMailAccount().equals(userDTO.getMailAccount())) {
            return false;
        }

        UserDTO userDTOwithUserId = creaUserDTO(userId, userDTO.getGroupId(), userDTO.getMailAccount(),
                userDTO.getCurrentPassword(), userDTO.getPassword(), userDTO.getConfirmPassword(),
                userDTO.getProfileImage());

        clearErrors(errorHandler, "dto", "token");
        userRepository.removeUser(userDTOwithUserId);
        return !userRepository.getAllUserDtos().contains(userDTOwithUserId);
    }

    @Override
    public void updateImageProfile(UserToken userToken, File file) {
        UserDTO founUserDTO = getUserDTOByToken(userToken);

        boolean containsNull = containsDataNull("updateImageProfile function",
                new LabeledValue("token", checkUserToken(userToken)), new LabeledValue("dto", founUserDTO));

        if (containsNull)
            return;

        boolean isValid = userValidator.validProfileImage(file);

        if (isValid && file == null) {
            clearErrors(errorHandler, "dto", "token", "file");
            userRepository.updateUser(founUserDTO, (File) null);
        }

        if (isValid && file != null) {
            clearErrors(errorHandler, "dto", "token", "file");
            userRepository.updateUser(founUserDTO, file);
        }
    }

    @Override
    public List<UserDTO> getAllUserAccounts(UserToken userToken) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();
        boolean containsNull = containsDataNull("getAllUserAccounts function",
                new LabeledValue("token", checkUserToken(userToken)), new LabeledValue("DTOs", userDTOs));

        if (containsNull)
            return null;

        clearErrors(errorHandler, "DTOs", "token");
        userDTOs = userDTOs.stream().filter(userDTO -> !userDTO.getUserId().equals(userToken.getUserId())
                && userDTO.getGroupId().equals(userToken.getGroupId())).toList();
        userDTOs.forEach(userDTO -> userDTO.sanitize(ViewLevel.PUBLIC));

        return userDTOs;
    }

    @Override
    public Image getImageProfile(UserToken userToken) {
        boolean containsNull = containsDataNull("getImageProfile function",
                new LabeledValue("token", checkUserToken(userToken)));

        if (containsNull)
            return null;

        Optional<UserDTO> foundUserDTO = userRepository.getAllUserDtos().stream()
                .filter(userDTO -> userDTO.getGroupId().equals(userToken.getGroupId())
                        && userDTO.getMailAccount().equals(userToken.getMailAccount()))
                .findAny();

        if (foundUserDTO.isPresent() && foundUserDTO.get().getProfileImage() != null) {
            clearErrors(errorHandler, "token");
            return FileConvertor.base64ToImage(foundUserDTO.get().getProfileImage());
        }
        return null;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}

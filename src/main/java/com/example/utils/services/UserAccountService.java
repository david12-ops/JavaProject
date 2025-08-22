package com.example.utils.services;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
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
import com.example.utils.enums.TokenField;
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
        this.userRepository = RepositoryFactory.getInstance(environmentType).getUserRepository();
        this.errorHandler = validationContext.getUserValidationBundle().getErrorManager();
        this.userValidator = validationContext.getUserValidationBundle().getValidator();

        if (environmentType == EnvironmentType.TEST)
            setTestUsersList();
    }

    private void setTestUsersList() {
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

    @Override
    public boolean removeAccount(UserToken userToken, UserDTO userDTO) {
        final Optional<String> resolvedUserId;
        final Optional<String> resolvedGroupId;
        boolean containsNull = containsDataNull("removeAccount function",
                new LabeledValue("token",
                        checkUserToken(userToken,
                                EnumSet.of(TokenField.EMAILACCOUNT, TokenField.USERID, TokenField.GROUPID))),
                new LabeledValue("dto", userDTO));

        if (containsNull)
            return false;

        resolvedUserId = resolveIdByEmail(userDTO.getMailAccount());

        if (resolvedUserId.isEmpty())
            return false;

        resolvedGroupId = resolveGroupIdByUserIdAndEmail(resolvedUserId.get(), userDTO.getMailAccount());

        if (resolvedGroupId.filter(gId -> gId.equals(userToken.getGroupId())).isEmpty()
                || userToken.getMailAccount().equals(userDTO.getMailAccount()))
            return false;

        UserDTO userDTOwithUserId = creaUserDTO(resolvedUserId.get(), resolvedGroupId.get(), userDTO.getMailAccount(),
                userDTO.getCurrentPassword(), userDTO.getPassword(), userDTO.getConfirmPassword(),
                userDTO.getProfileImage());

        clearErrors(errorHandler, "dto", "token");
        userRepository.removeUser(userDTOwithUserId);
        return !userRepository.getAllUserDtos().contains(userDTOwithUserId);
    }

    @Override
    public void updateImageProfile(UserToken userToken, File file) {
        final Optional<UserDTO> foundUserDTO;
        boolean containsNull = containsDataNull("updateImageProfile function", new LabeledValue("token",
                checkUserToken(userToken, EnumSet.of(TokenField.EMAILACCOUNT, TokenField.USERID, TokenField.GROUPID))));

        if (containsNull)
            return;

        boolean isValid = userValidator.validProfileImage(file);
        foundUserDTO = isValid ? getUserDTOByToken(userToken) : null;

        if (containsDataNull("updateImageProfile function", new LabeledValue("dto", foundUserDTO)))
            return;

        if (foundUserDTO.isPresent() && file == null) {
            clearErrors(errorHandler, "dto", "token", "file");
            userRepository.updateUser(foundUserDTO.get(), (File) null);
        }

        if (foundUserDTO.isPresent() && file != null) {
            clearErrors(errorHandler, "dto", "token", "file");
            userRepository.updateUser(foundUserDTO.get(), file);
        }
    }

    @Override
    public List<UserDTO> getAllUserAccounts(UserToken userToken) {
        List<UserDTO> userDTOs = userRepository.getAllUserDtos();
        boolean containsNull = containsDataNull("getAllUserAccounts function",
                new LabeledValue("token",
                        checkUserToken(userToken,
                                EnumSet.of(TokenField.EMAILACCOUNT, TokenField.USERID, TokenField.GROUPID))),
                new LabeledValue("DTOs", userDTOs));

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
        final Optional<UserDTO> foundUserDTO;
        boolean containsNull = containsDataNull("getImageProfile function", new LabeledValue("token",
                checkUserToken(userToken, EnumSet.of(TokenField.EMAILACCOUNT, TokenField.USERID, TokenField.GROUPID))));

        if (containsNull)
            return null;

        foundUserDTO = getUserDTOByToken(userToken);

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

package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.model.UserToken;
import com.example.model.repository.UserRepository;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.FormType;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        this.userRepository = new UserRepository(EnvironmentType.TEST);
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

    private List<User> createUsersWithNullProfileImages() {
        List<User> users = new ArrayList<>();

        users.add(new User("1", "groupA", "alice@example.com", "hashedPassword1!", null));
        users.add(new User("2", "groupA", "bob@example.com", "hashedPassword2!", null));
        users.add(new User("3", "groupB", "charlie@example.com", "hashedPassword3!", null));
        users.add(new User("4", "groupB", "dave@example.com", "hashedPassword4!", null));
        users.add(new User("5", "groupC", "eve@example.com", "hashedPassword5!", null));

        return users;
    }

    private void prepareData(List<UserToken> userTokens) {
        userRepository.setTestData(createUsersWithNullProfileImages());
        List<UserDTO> userDTOs = new ArrayList<>(userRepository.getAllUserDtos());

        if (userTokens != null) {
            for (UserDTO userDTO : userDTOs) {
                userDTO.setCurrentPassword(BCrypt.hashpw(userDTO.getCurrentPassword(), BCrypt.gensalt()));
                userTokens.add(new UserToken(userDTO.getUserId(), userDTO.getGroupId(), userDTO.getMailAccount()));
            }
        } else {
            for (UserDTO userDTO : userDTOs) {
                userDTO.setCurrentPassword(BCrypt.hashpw(userDTO.getCurrentPassword(), BCrypt.gensalt()));
            }
        }

        List<User> data = new ArrayList<>();

        userDTOs.forEach(user -> data.add(new User(user.getUserId(), user.getGroupId(), user.getMailAccount(),
                user.getCurrentPassword(), user.getProfileImage())));

        userRepository.setTestData(data);
    }

    @Test
    @DisplayName("Should remove all users except the logged-in one")
    void testRemoveUser() {
        userRepository.setTestData(createUsersWithNullProfileImages());

        for (UserDTO userDTO : userRepository.getAllUserDtos()) {
            userRepository.removeUser(userDTO);
        }

        assertEquals(0, userRepository.getAllUserDtos().size());
    }

    @Test
    @DisplayName("Should update and clear user's profile image")
    void testUpdateUserProfileImage() {
        userRepository.setTestData(createUsersWithNullProfileImages());
        UserDTO alice = userRepository.getAllUserDtos().get(0);
        alice.setProfileImage("fK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQ");

        assertEquals(alice.getProfileImage(), "fK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQ");

        alice.setProfileImage(null);
        assertNull(alice.getProfileImage());
    }

    @Test
    @DisplayName("Should add another account")
    void testAddingAnotherAccount() {
        prepareData(null);

        userRepository.addUser(
                new UserDTO(null, null, "test.addanother@gmail.com", null, "Example@123", "Example@123", null),
                AddOperationType.ANOTHERACCOUNT);

        assertTrue(userRepository.getAllUserDtos().stream()
                .anyMatch(userDTO -> userDTO.getMailAccount().equals("test.addanother@gmail.com")));

        userRepository.addUser(
                new UserDTO(null, null, "test.addanother2@gmail.com", null, "Example@123", "Example@123", null),
                AddOperationType.ANOTHERACCOUNT);

        assertTrue(userRepository.getAllUserDtos().size() == 7);
    }

    @Test
    @DisplayName("Should add another account")
    void testRegisterNewAccount() {
        prepareData(null);

        userRepository.addUser(
                new UserDTO(null, null, "test.addanother@gmail.com", null, "Example@123", "Example@123", null),
                AddOperationType.NEWACCOUNT);

        assertTrue(userRepository.getAllUserDtos().stream()
                .anyMatch(userDTO -> userDTO.getMailAccount().equals("test.addanother@gmail.com")));

        userRepository.addUser(
                new UserDTO(null, null, "test.addanother2@gmail.com", null, "Example@123", "Example@123", null),
                AddOperationType.NEWACCOUNT);

        assertTrue(userRepository.getAllUserDtos().size() == 7);
    }

    @Test
    @DisplayName("Should update account of not logged user")
    void testUpdateNotLoggedAccount() {
        prepareData(null);

        UserDTO foundUserDTO = getUserByEmailAndPassword("alice@example.com", "hashedPassword1!",
                userRepository.getAllUserDtos());
        assertNotNull(foundUserDTO);

        foundUserDTO.setPassword("Example@123456");
        foundUserDTO.setConfirmPassword("Example@123456");
        userRepository.updateUser(foundUserDTO, FormType.FORGOTCREDENTIALS);
        assertNotNull(
                getUserByEmailAndPassword("alice@example.com", "Example@123456", userRepository.getAllUserDtos()));

        UserDTO foundUserDTO2 = getUserByEmailAndPassword("bob@example.com", "hashedPassword2!",
                userRepository.getAllUserDtos());
        assertNotNull(foundUserDTO2);

        foundUserDTO2.setPassword("Example@123456789");
        foundUserDTO2.setConfirmPassword("Example@123456789");
        userRepository.updateUser(foundUserDTO2, FormType.FORGOTCREDENTIALS);
        assertNull(getUserByEmailAndPassword("bob@example.com", "Example@123456", userRepository.getAllUserDtos()));
    }

    @Test
    @DisplayName("Should update account logged user")
    void testUpdateLoggedAccount() {
        List<UserToken> tokens = new ArrayList<>();
        prepareData(tokens);

        UserDTO foundUserDTO = getUserByToken(tokens.get(0), userRepository.getAllUserDtos());
        assertNotNull(foundUserDTO);

        foundUserDTO.setPassword("Example@123456");
        foundUserDTO.setConfirmPassword("Example@123456");
        userRepository.updateUser(foundUserDTO, FormType.FORGOTCREDENTIALS);
        assertNotNull(getUserByEmailAndPassword(foundUserDTO.getMailAccount(), "Example@123456",
                userRepository.getAllUserDtos()));

        UserDTO foundUserDTO2 = getUserByToken(tokens.get(1), userRepository.getAllUserDtos());
        assertNotNull(foundUserDTO2);

        foundUserDTO2.setPassword("Example@123456789");
        foundUserDTO2.setConfirmPassword("Example@123456789");
        userRepository.updateUser(foundUserDTO2, FormType.FORGOTCREDENTIALS);
        assertNull(getUserByEmailAndPassword(foundUserDTO2.getMailAccount(), "Example@123456",
                userRepository.getAllUserDtos()));
    }
}

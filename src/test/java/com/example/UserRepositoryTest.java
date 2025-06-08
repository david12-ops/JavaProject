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

import com.example.model.User;
import com.example.model.UserRepository;
import com.example.model.UserToken;
import com.example.utils.enums.AddOperationType;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.FormType;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    private UserRepository userModel;
    private List<User> testDataUsers;

    @BeforeEach
    void setup() {
        this.userModel = new UserRepository(EnvironmentType.TEST);
        this.testDataUsers = createUsersWithNullProfileImages();
    }

    public List<User> createUsersWithNullProfileImages() {
        List<User> users = new ArrayList<>();

        users.add(new User("1", "groupA", "alice@example.com", "hashedPassword1!", null));
        users.add(new User("2", "groupA", "bob@example.com", "hashedPassword2!", null));
        users.add(new User("3", "groupB", "charlie@example.com", "hashedPassword3!", null));
        users.add(new User("4", "groupB", "dave@example.com", "hashedPassword4!", null));
        users.add(new User("5", "groupC", "eve@example.com", "hashedPassword5!", null));

        return users;
    }

    @Test
    @DisplayName("Should remove all users except the logged-in one")
    void testRemoveUser() {
        userModel.setTestData(testDataUsers);
        UserToken userToken = new UserToken("1", "groupA", "alice@example.com");

        for (User user : new ArrayList<>(testDataUsers)) {
            userModel.removeUser(userToken, user);
        }

        List<User> remainingUsers = userModel.getTestData();
        assertEquals(1, remainingUsers.size());
        assertEquals("alice@example.com", remainingUsers.get(0).getMailAccount());
    }

    @Test
    @DisplayName("Should update and clear user's profile image")
    void testUpdateUserProfileImage() {
        userModel.setTestData(testDataUsers);
        User alice = testDataUsers.get(0);
        alice.setImage("fK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQ");

        assertEquals(alice.getProfileImage(), "fK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQfK7aVp9LzQ");

        alice.setImage(null);
        assertNull(alice.getProfileImage());
    }

    @Test
    @DisplayName("Should return user by valid credentials and null for invalid ones")
    void testGetUserByCredentials() {
        userModel.setTestData(testDataUsers);
        List<User> data = new ArrayList<>(testDataUsers);

        for (User user : data) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        userModel.setTestData(data);

        User user1 = userModel.getUserByCredentials("alice@example.com", "hashedPassword1!", null);
        User user2 = userModel.getUserByCredentials("charlie@example.com", "hashedPassword1", null);
        User user3 = userModel.getUserByCredentials("eve@example.com", "hashedPassword3!", null);
        User user4 = userModel.getUserByCredentials(null, "hashedPassword3!", null);

        assertEquals(user1.getMailAccount(), "alice@example.com");
        assertNull(user2);
        assertNull(user3);
        assertNull(user4);
    }

    @Test
    @DisplayName("Should return user by valid token and null for mismatched tokens")
    void testGetUserByToken() {
        userModel.setTestData(testDataUsers);
        User user1 = userModel.getUserByCredentials(null, null, new UserToken("1", "groupA", "alice@example.com"));
        User user2 = userModel.getUserByCredentials(null, null, new UserToken("3", "groupA", "eve@example.com"));
        User user3 = userModel.getUserByCredentials(null, null, new UserToken("5", "groupA", "charlie@example.com"));
        User user4 = userModel.getUserByCredentials(null, null, null);

        assertEquals(user1.getMailAccount(), "alice@example.com");
        assertNull(user2);
        assertNull(user3);
        assertNull(user4);
    }

    @Test
    @DisplayName("Should return all users belonging to the same group as the token")
    void testGetAllUserAccounts() {
        userModel.setTestData(testDataUsers);
        UserToken groupAToken = new UserToken("1", "groupA", "alice@example.com");
        UserToken groupBToken = new UserToken("3", "groupB", "charlie@example.com");

        List<User> groupA = userModel.getAllUserAccounts(groupAToken);
        List<User> groupB = userModel.getAllUserAccounts(groupBToken);
        List<User> groupC = userModel.getAllUserAccounts(null);

        assertNotNull(groupA);
        assertNotNull(groupB);
        assertNull(groupC);
        assertEquals(2, groupA.size());
        assertEquals(2, groupB.size());

    }

    @Test
    @DisplayName("Should add another account")
    void testAddingAnotherAccount() {
        userModel.setTestData(testDataUsers);
        List<User> data = new ArrayList<>(testDataUsers);

        for (User user : data) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        userModel.setTestData(data);

        UserToken userToken = new UserToken("1", "groupA", "alice@example.com");

        userModel.addUser("test.addanother@gmail.com", "Example@123", "Example@123", userToken,
                AddOperationType.ANOTHERACCOUNT, FormType.ADDACCOUNT);

        assertTrue(data.stream().anyMatch(user -> user.getMailAccount().equals("test.addanother@gmail.com")));

        userModel.addUser("test.addanother@gmail.com", "Example@123", "Example@123", userToken,
                AddOperationType.ANOTHERACCOUNT, FormType.ADDACCOUNT);

        userModel.addUser("alice@example.com", "Example@123", "Example@123", userToken, AddOperationType.ANOTHERACCOUNT,
                FormType.ADDACCOUNT);

        assertTrue(data.size() == 6);
    }

    @Test
    @DisplayName("Should add another account")
    void testRegisterNewAccount() {
        userModel.setTestData(testDataUsers);
        List<User> data = new ArrayList<>(testDataUsers);

        for (User user : data) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        userModel.setTestData(data);

        UserToken userToken = new UserToken("1", "groupA", "alice@example.com");

        userModel.addUser("test.addanother@gmail.com", "Example@123", "Example@123", userToken,
                AddOperationType.NEWACCOUNT, FormType.REGISTER);

        assertTrue(data.stream().anyMatch(user -> user.getMailAccount().equals("test.addanother@gmail.com")));

        userModel.addUser("test.addanother@gmail.com", "Example@123", "Example@123", userToken,
                AddOperationType.NEWACCOUNT, FormType.REGISTER);

        userModel.addUser("alice@example.com", "Example@123", "Example@123", userToken, AddOperationType.NEWACCOUNT,
                FormType.REGISTER);

        assertTrue(data.size() == 6);
    }

    @Test
    @DisplayName("Should update account of not logged user")
    void testUpdateNotLoggedAccount() {
        userModel.setTestData(testDataUsers);
        List<User> data = new ArrayList<>(testDataUsers);

        for (User user : data) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }

        userModel.setTestData(data);

        User foundUser = userModel.getUserByCredentials("alice@example.com", "hashedPassword1!", null);
        assertNotNull(foundUser);

        userModel.updateUser(foundUser, "Example@123456", "Example@123456", FormType.FORGOTCREDENTIALS);

        assertNotNull(userModel.getUserByCredentials("alice@example.com", "Example@123456", null));

        User foundUser2 = userModel.getUserByCredentials("bob@example.com", "hashedPassword2!", null);
        assertNotNull(foundUser2);

        userModel.updateUser(foundUser2, "Example@123456789", "Example@123456789", FormType.FORGOTCREDENTIALS);

        assertNull(userModel.getUserByCredentials("bob@example.com", "Example@123456", null));
    }

    @Test
    @DisplayName("Should update account logged user")
    void testUpdateLoggedAccount() {
        userModel.setTestData(testDataUsers);
        List<User> data = new ArrayList<>(testDataUsers);
        List<UserToken> tokens = new ArrayList<>();

        for (User user : data) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            tokens.add(new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount()));
        }

        userModel.setTestData(data);

        User foundUser = userModel.getUserByCredentials(null, null, tokens.get(0));
        assertNotNull(foundUser);

        userModel.updateUser(foundUser, "Example@123456", "Example@123456", FormType.FORGOTCREDENTIALS);

        assertNotNull(userModel.getUserByCredentials(foundUser.getMailAccount(), "Example@123456", null));

        User foundUser2 = userModel.getUserByCredentials(null, null, tokens.get(1));
        assertNotNull(foundUser2);

        userModel.updateUser(foundUser2, "Example@123456789", "Example@123456789", FormType.FORGOTCREDENTIALS);

        assertNull(userModel.getUserByCredentials(foundUser2.getMailAccount(), "Example@123456", null));
    }
}

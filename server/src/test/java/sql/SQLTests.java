package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataaccess.*;
import model.*;

public class SQLTests {
    private SQLUserDAO user;
    public SQLTests() throws DataAccessException {
        user = new SQLUserDAO();
    }
    
    @BeforeEach
    public void each() {
        user.clear();
    }

    @Test
    public void testUserInsertUserPositive() {
        assertDoesNotThrow(() ->
            user.insertUser(new UserData("fred", "fake pass", "email@me.net")));
    }

    @Test
    public void testUserInsertUserNegative() {
        assertDoesNotThrow(() -> user.insertUser(
            new UserData("fred", "fake pass", "email@me.net")));
        assertThrows(DataAccessException.class, () -> user.insertUser(
            new UserData("fred", "fake pass", "email@me.net")));
        assertThrows(DataAccessException.class, () -> user.insertUser(
            new UserData("empty fields", null, null)));
    }

    @Test
    public void testUserGetUserPositive() {
        var testUser = new UserData("fred", "another", "more emails");
        assertDoesNotThrow(() -> user.insertUser(testUser));
        var otherUser = user.getUser(testUser.username());
        assertTrue(testUser.equals(otherUser), testUser + " != " + otherUser);
    }

    @Test
    public void testUserGetUserNegative() {
        var testUser = new UserData("fred", "another", "more emails");
        assertDoesNotThrow(() -> user.insertUser(testUser));
        var otherUser = user.getUser("nada ver");
        assertTrue(otherUser == null);
    }

    @Test
    public void testUserClear() {
        var oneUser = new UserData("jameis", "fake", "...");
        var twoUser = new UserData("fred", "again", "!");
        assertDoesNotThrow(() -> user.insertUser(oneUser));
        assertDoesNotThrow(() ->
            user.insertUser(new UserData("another", "", "i")));
        assertDoesNotThrow(() -> user.insertUser(new UserData("fake", "", "")));
        assertDoesNotThrow(() -> user.insertUser(twoUser));
        user.clear();
        assertTrue(user.getUser(oneUser.username()) == null);
        assertTrue(user.getUser(twoUser.username()) == null);
        assertDoesNotThrow(() -> user.insertUser(oneUser));
        assertDoesNotThrow(() ->
            user.insertUser(new UserData("another", "", "i")));
        assertDoesNotThrow(() -> user.insertUser(new UserData("fake", "", "")));
        assertDoesNotThrow(() -> user.insertUser(twoUser));
    }
}

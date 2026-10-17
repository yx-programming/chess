package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataaccess.*;
import model.*;

public class SQLTests {
    private SQLUserDAO user;
    private SQLAuthDAO auth;
    public SQLTests() throws DataAccessException {
        user = new SQLUserDAO();
        auth = new SQLAuthDAO();
    }
    
    @BeforeEach
    public void each() throws DataAccessException {
        user.clear();
        auth.clear();
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
    public void testUserGetUserPositive() throws DataAccessException {
        var testUser = new UserData("fred", "another", "more emails");
        assertDoesNotThrow(() -> user.insertUser(testUser));
        var otherUser = user.getUser(testUser.username());
        assertTrue(testUser.equals(otherUser), testUser + " != " + otherUser);
    }

    @Test
    public void testUserGetUserNegative() throws DataAccessException {
        var testUser = new UserData("fred", "another", "more emails");
        assertDoesNotThrow(() -> user.insertUser(testUser));
        var otherUser = user.getUser("nada ver");
        assertTrue(otherUser == null);
    }

    @Test
    public void testUserClear() throws DataAccessException {
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

    @Test
    public void testAuthNewAuthPositive() throws DataAccessException {
        assertDoesNotThrow(() -> auth.newAuth("fakeuser"));
    }

    @Test
    public void testAuthNewAuthNegative() throws DataAccessException {
        assertDoesNotThrow(() -> auth.newAuth("fakeuser"));
        assertThrows(DataAccessException.class, () -> auth.newAuth("fakeuser"));
    }

    @Test
    public void testAuthGetAuthPositive() throws DataAccessException {
        assertDoesNotThrow(() -> auth.newAuth("fakeuser"));
        assertDoesNotThrow(() -> auth.newAuth("anotherfake"));
        assertFalse(() -> {
            try {
                return auth.getAuth("fakeuser")
                    .equals(auth.getAuth("anotherfake"));
            } catch (Exception e) { return false; }
        });
    }

    @Test
    public void testAuthGetAuthNegative() throws DataAccessException {
        assertDoesNotThrow(() -> auth.newAuth("fakeuser"));
        assertTrue(() -> {
            try {
                return auth.getAuth("another fake user") == null;
            } catch (Exception e) { return false; }
        });
    }
}

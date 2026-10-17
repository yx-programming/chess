package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataaccess.*;
import model.*;

public class MemTests {
    private UserDAO user;
    private AuthDAO auth;
    private GameDAO game;

    public MemTests() throws DataAccessException {
        this.user = new MemUserDAO();
        this.auth = new MemAuthDAO();
        this.game = new MemGameDAO();
    }
    
    @BeforeEach
    public void each() throws DataAccessException {
        user.clear();
        auth.clear();
        game.clear();
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
        assertTrue(otherUser != null, testUser + " != " + otherUser);
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

    @Test
    public void testAuthRemoveAuthPositive() throws DataAccessException {
        AuthData authInfo = auth.newAuth("fakeuser");
        assertDoesNotThrow(() -> auth.removeAuth(authInfo));
        assertTrue(() -> {
            try {
                return auth.getAuth(authInfo.authToken()) == null;
            } catch (Exception e) { return false; }
        });
    }

    @Test
    public void testAuthClearPositive() throws DataAccessException {
        AuthData authInfo = auth.newAuth("fakeuser");
        auth.clear();
        assertTrue(() -> {
            try {
                return auth.getAuth(authInfo.authToken()) == null;
            } catch (Exception e) { return false; }
        });
    }

    @Test
    public void testGameNewGamePositive() {
        assertDoesNotThrow(() -> game.newGame("test"));        
    }

    @Test
    public void testGameListGamesPositive() throws DataAccessException {
        int id1 = game.newGame("test1");
        int id2 = game.newGame("test2");
        int id3 = game.newGame("test3");

        var games = game.getGames();
        assertTrue(games.size() == 3);
    }

    @Test public void testGameJoinGamePositive() throws DataAccessException {
        int id1 = game.newGame("test1");

        assertDoesNotThrow(() -> game.joinGame(id1, ChessGame.TeamColor.BLACK,
            "testuser1"));        
        assertDoesNotThrow(() -> game.joinGame(id1, ChessGame.TeamColor.WHITE,
            "testuser2"));        
    }

    @Test public void testGameJoinGameNegative() throws DataAccessException {
        int id1 = game.newGame("test1");

        assertThrows(DataAccessException.class,
            () -> game.joinGame(10, ChessGame.TeamColor.WHITE, "testuser2"));        
        game.joinGame(id1, ChessGame.TeamColor.WHITE, "testuser2");
        game.joinGame(id1, ChessGame.TeamColor.BLACK, "testuser3");
        assertThrows(DataAccessException.class,
            () -> game.joinGame(id1, ChessGame.TeamColor.BLACK, "testuser2"));
        assertThrows(DataAccessException.class,
            () -> game.joinGame(id1, ChessGame.TeamColor.WHITE, "testuser3"));
    }
}

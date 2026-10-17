package dataaccess.sql;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataaccess.*;
import model.*;

public class SQLTests {
    private UserDAO user;
    private AuthDAO auth;
    private GameDAO game;
    private UserDAO userMem;
    private AuthDAO authMem;
    private GameDAO gameMem;

    public SQLTests() throws DataAccessException {
        this.user = new SQLUserDAO();
        this.auth = new SQLAuthDAO();
        this.game = new SQLGameDAO();
        this.userMem = new MemUserDAO();
        this.authMem = new MemAuthDAO();
        this.gameMem = new MemGameDAO();
    }
    
    @BeforeEach
    public void each() throws DataAccessException {
        user.clear();
        auth.clear();
        game.clear();
        userMem.clear();
        authMem.clear();
        gameMem.clear();
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

    @Test
    public void testUserInsertUserPositiveMem() {
        assertDoesNotThrow(() ->
            userMem.insertUser(new UserData("fred", "fake pass", "email@me.net")));
    }

    @Test
    public void testUserInsertUserNegativeMem() {
        assertDoesNotThrow(() -> userMem.insertUser(
            new UserData("fred", "fake pass", "email@me.net")));
        assertThrows(DataAccessException.class, () -> userMem.insertUser(
            new UserData("fred", "fake pass", "email@me.net")));
        assertThrows(DataAccessException.class, () -> userMem.insertUser(
            new UserData("empty fields", null, null)));
    }

    @Test
    public void testUserGetUserPositiveMem() throws DataAccessException {
        var testUser = new UserData("fred", "another", "more emails");
        assertDoesNotThrow(() -> userMem.insertUser(testUser));
        var otherUser = userMem.getUser(testUser.username());
        assertTrue(otherUser != null, testUser + " != " + otherUser);
    }

    @Test
    public void testUserGetUserNegativeMem() throws DataAccessException {
        var testUser = new UserData("fred", "another", "more emails");
        assertDoesNotThrow(() -> userMem.insertUser(testUser));
        var otherUser = userMem.getUser("nada ver");
        assertTrue(otherUser == null);
    }

    @Test
    public void testUserClearMem() throws DataAccessException {
        var oneUser = new UserData("jameis", "fake", "...");
        var twoUser = new UserData("fred", "again", "!");
        assertDoesNotThrow(() -> userMem.insertUser(oneUser));
        assertDoesNotThrow(() ->
            userMem.insertUser(new UserData("another", "", "i")));
        assertDoesNotThrow(() -> userMem.insertUser(new UserData("fake", "", "")));
        assertDoesNotThrow(() -> userMem.insertUser(twoUser));
        userMem.clear();
        assertTrue(userMem.getUser(oneUser.username()) == null);
        assertTrue(userMem.getUser(twoUser.username()) == null);
        assertDoesNotThrow(() -> userMem.insertUser(oneUser));
        assertDoesNotThrow(() ->
            userMem.insertUser(new UserData("another", "", "i")));
        assertDoesNotThrow(() -> userMem.insertUser(new UserData("fake", "", "")));
        assertDoesNotThrow(() -> userMem.insertUser(twoUser));
    }

    @Test
    public void testAuthNewAuthPositiveMem() throws DataAccessException {
        assertDoesNotThrow(() -> authMem.newAuth("fakeuser"));
    }

    @Test
    public void testAuthGetAuthPositiveMem() throws DataAccessException {
        assertDoesNotThrow(() -> authMem.newAuth("fakeuser"));
        assertDoesNotThrow(() -> authMem.newAuth("anotherfake"));
        assertFalse(() -> {
            try {
                return authMem.getAuth("fakeuser")
                    .equals(authMem.getAuth("anotherfake"));
            } catch (Exception e) { return false; }
        });
    }

    @Test
    public void testAuthGetAuthNegativeMem() throws DataAccessException {
        assertDoesNotThrow(() -> authMem.newAuth("fakeuser"));
        assertTrue(() -> {
            try {
                return authMem.getAuth("another fake user") == null;
            } catch (Exception e) { return false; }
        });
    }

    @Test
    public void testAuthRemoveAuthPositiveMem() throws DataAccessException {
        AuthData authInfo = authMem.newAuth("fakeuser");
        assertDoesNotThrow(() -> authMem.removeAuth(authInfo));
        assertTrue(() -> {
            try {
                return authMem.getAuth(authInfo.authToken()) == null;
            } catch (Exception e) { return false; }
        });
    }

    @Test
    public void testAuthClearPositiveMem() throws DataAccessException {
        AuthData authInfo = authMem.newAuth("fakeuser");
        authMem.clear();
        assertTrue(() -> {
            try {
                return authMem.getAuth(authInfo.authToken()) == null;
            } catch (Exception e) { return false; }
        });
    }

    @Test
    public void testGameNewGamePositiveMem() {
        assertDoesNotThrow(() -> gameMem.newGame("test"));        
    }

    @Test
    public void testGameListGamesPositiveMem() throws DataAccessException {
        int id1 = gameMem.newGame("test1");
        int id2 = gameMem.newGame("test2");
        int id3 = gameMem.newGame("test3");

        var games = gameMem.getGames();
        assertTrue(games.size() == 3);
    }

    @Test public void testGameJoinGameNegativeMem() throws DataAccessException {
        int id1 = gameMem.newGame("test1");

        assertThrows(DataAccessException.class,
            () -> gameMem.joinGame(10, ChessGame.TeamColor.WHITE, "testuser2"));        
        gameMem.joinGame(id1, ChessGame.TeamColor.WHITE, "testuser2");
        gameMem.joinGame(id1, ChessGame.TeamColor.BLACK, "testuser3");
        assertThrows(DataAccessException.class,
            () -> gameMem.joinGame(id1, ChessGame.TeamColor.BLACK, "testuser2"));
        assertThrows(DataAccessException.class,
            () -> gameMem.joinGame(id1, ChessGame.TeamColor.WHITE, "testuser3"));
    }
}

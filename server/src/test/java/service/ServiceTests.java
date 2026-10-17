package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataaccess.*;
import model.*;

public class ServiceTests {
    private UserService user;
    private AuthService auth;
    private GameService game;

    public ServiceTests() throws DataAccessException {
        user = new UserService();
        auth = new AuthService();
        game = new GameService();
    }

    @BeforeEach
    public void each() throws DataAccessException {
        user.clear();
        auth.clear();
        game.clear();
    }

    @Test
    public void newUserTestPositive() throws DataAccessException {
        var data = new UserData("test", "password", "email@email.net");
        user.newUser(data);
        assertDoesNotThrow(() ->
            assertTrue(data.equals(user.getUserData().getUser("test"))));
    }

    @Test
    public void newUserTestNegative() {
        var data = new UserData("test", "password", "email@email.net");
        assertDoesNotThrow(() -> {
            user.newUser(data);
            assertThrows(Exception.class, () -> user.newUser(data));
        });
    }

    @Test
    public void validateUserTestPositive() {
        assertDoesNotThrow(() -> {
            var data = new UserData("test", "password", "email@email.net");
            user.newUser(data);
            assertDoesNotThrow(() -> user.validateUser(data));
        });
    }

    @Test
    public void validateUserTestNegative() {
        assertDoesNotThrow(() -> {
            var data = new UserData("test", "password", "email@email.net");
            user.newUser(data);
            var badUser = new UserData("wrong", "password", "email@n");
            assertThrows(Exception.class, () -> user.validateUser(badUser));
            var badPass = new UserData("test", "wrong", "email@n");
            assertThrows(Exception.class, () -> user.validateUser(badPass));
        });
    }

    @Test
    public void newAuthTestPositive() {
        assertDoesNotThrow(() -> {
            auth.newAuth("test");
        });
    }

    @Test
    public void getAuthFromTokenTestPositive() {
        assertDoesNotThrow(() -> {
            var token = auth.newAuth("test");
            assertTrue(auth.getAuthFromToken(token.authToken()).equals(token));
        });
    }

    @Test
    public void getUAuthFromTokenTestNegative() {
        assertThrows(Exception.class, () -> {
            var token = auth.newAuth("test");        
            auth.getAuthFromToken("bad token");
        });
    }

    @Test
    public void removeAuthTestPositive() {
        assertDoesNotThrow(() -> {
            var token = auth.newAuth("uhhh");
            auth.removeAuth(token);
        });
    }

    @Test
    public void createGameTestPositive() {
        assertDoesNotThrow(() -> {
            assertTrue(game.newGame("first") > 0);        
            assertTrue(game.newGame("test") > 0);
            assertTrue(game.newGame("for") > 0);        
            assertTrue(game.newGame("GameService") > 0);        
        });
    }

    @Test
    public void getGamesTestPositive() {
        assertDoesNotThrow(() -> {
            game.newGame("next");
            game.newGame("test");
            game.newGame("for");
            game.newGame("GameService");

            assertTrue(game.getGames().size() == 4);
        });
    }

    @Test
    public void joinGameTestPositive() {
        assertDoesNotThrow(() -> {
            int id = game.newGame("testing");
            game.joinGame(id, ChessGame.TeamColor.WHITE, "whiteuser");
            game.joinGame(id, ChessGame.TeamColor.WHITE, "whiteuser");
        });
    }

    @Test
    public void joinGameTestNegative() {
        assertDoesNotThrow(() -> {
            int id = game.newGame("testing");
            game.joinGame(id, ChessGame.TeamColor.WHITE, "whiteuser");
            assertThrows(Exception.class, () -> game.joinGame(id,
                ChessGame.TeamColor.WHITE, "blackuser"));
        });
    }
}

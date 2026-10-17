package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataaccess.*;
import model.*;

public class ServiceTests {
    @Test
    public void newUserTestPositive() {
        var data = new UserData("test", "password", "email@email.net");
        assertDoesNotThrow(() -> {
            var service = new UserService();
            service.newUser(data);
            assertDoesNotThrow(() ->
                assertTrue(service.getUserData().getUser("test") == data));
        });
    }

    @Test
    public void newUserTestNegative() {
        var data = new UserData("test", "password", "email@email.net");
        assertDoesNotThrow(() -> {
            var service = new UserService();
            service.newUser(data);
            assertThrows(Exception.class, () -> service.newUser(data));
        });
    }

    @Test
    public void validateUserTestPositive() {
        assertDoesNotThrow(() -> {
            var service = new UserService();
            var data = new UserData("test", "password", "email@email.net");
            service.newUser(data);
            assertDoesNotThrow(() -> service.validateUser(data));
        });
    }

    @Test
    public void validateUserTestNegative() {
        assertDoesNotThrow(() -> {
            var data = new UserData("test", "password", "email@email.net");
            var service = new UserService();
            service.newUser(data);
            var badUser = new UserData("wrong", "password", "email@n");
            assertThrows(Exception.class, () -> service.validateUser(badUser));
            var badPass = new UserData("test", "wrong", "email@n");
            assertThrows(Exception.class, () -> service.validateUser(badPass));
        });
    }

    @Test
    public void newAuthTestPositive() {
        assertDoesNotThrow(() -> {
            var service = new AuthService();
            service.newAuth("test");
        });
    }

    @Test
    public void getAuthFromTokenTestPositive() {
        assertDoesNotThrow(() -> {
            var auth = new AuthService();
            var token = auth.newAuth("test");
            assertTrue(auth.getAuthFromToken(token.authToken()).equals(token));
        });
    }

    @Test
    public void getUAuthFromTokenTestNegative() {
        assertThrows(Exception.class, () -> {
            var auth = new AuthService();
            var token = auth.newAuth("test");        
            auth.getAuthFromToken("bad token");
        });
    }

    @Test
    public void removeAuthTestPositive() {
        assertDoesNotThrow(() -> {
            var auth = new AuthService();
            var token = auth.newAuth("uhhh");
            auth.removeAuth(token);
        });
    }

    @Test
    public void createGameTestPositive() {
        assertDoesNotThrow(() -> {
            var game = new GameService();

            assertTrue(game.newGame("first") > 0);        
            assertTrue(game.newGame("test") > 0);
            assertTrue(game.newGame("for") > 0);        
            assertTrue(game.newGame("GameService") > 0);        
        });
    }

    @Test
    public void getGamesTestPositive() {
        assertDoesNotThrow(() -> {
            var game = new GameService();

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
            var game = new GameService();
            int id = game.newGame("testing");
            game.joinGame(id, ChessGame.TeamColor.WHITE, "whiteuser");
            game.joinGame(id, ChessGame.TeamColor.WHITE, "whiteuser");
        });
    }

    @Test
    public void joinGameTestNegative() {
        assertDoesNotThrow(() -> {
            var game = new GameService();

            int id = game.newGame("testing");
            game.joinGame(id, ChessGame.TeamColor.WHITE, "whiteuser");
            assertThrows(Exception.class, () -> game.joinGame(id,
                ChessGame.TeamColor.WHITE, "blackuser"));
        });
    }
}

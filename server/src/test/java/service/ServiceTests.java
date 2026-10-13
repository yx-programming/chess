package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataaccess.*;
import model.*;

public class ServiceTests {
    @Test
    public void newUserTestPositive() {
        var service = new UserService();
        var data = new UserData("test", "password", "email@email.net");
        assertDoesNotThrow(() -> service.newUser(data));
        assertDoesNotThrow(() ->
            assertTrue(service.getUserData().getUser("test") == data));
    }

    @Test
    public void newUserTestNegative() {
        var service = new UserService();
        var data = new UserData("test", "password", "email@email.net");
        assertDoesNotThrow(() -> service.newUser(data));
        assertThrows(Exception.class, () -> service.newUser(data));
    }

    @Test
    public void validateUserTestPositive() {
        var service = new UserService();
        var data = new UserData("test", "password", "email@email.net");
        assertDoesNotThrow(() -> service.newUser(data));
        assertDoesNotThrow(() -> service.validateUser(data));
    }

    @Test
    public void validateUserTestNegative() {
        var service = new UserService();
        var data = new UserData("test", "password", "email@email.net");
        assertDoesNotThrow(() -> service.newUser(data));
        var badUser = new UserData("wrong", "password", "email@n");
        assertThrows(Exception.class, () -> service.validateUser(badUser));
        var badPass = new UserData("test", "wrong", "email@n");
        assertThrows(Exception.class, () -> service.validateUser(badPass));
    }

    @Test
    public void newAuthTestPositive() {
        var service = new AuthService();
        assertDoesNotThrow(() -> service.newAuth("test"));
    }

    @Test
    public void getAuthFromTokenTestPositive() {
        var auth = new AuthService();
        var token = auth.newAuth("test");
        assertDoesNotThrow(() -> assertTrue(
            auth.getAuthFromToken(token.authToken()).equals(token)));
    }

    @Test
    public void getUAuthFromTokenTestNegative() {
        var auth = new AuthService();
        var token = auth.newAuth("test");        
        assertThrows(Exception.class,
            () -> auth.getAuthFromToken("bad token"));
    }

    @Test
    public void removeAuthTestPositive() {
        var auth = new AuthService();
        var token = auth.newAuth("uhhh");
        assertDoesNotThrow(() -> auth.removeAuth(token));
    }

    @Test
    public void createGameTestPositive() {
        var game = new GameService();

        assertTrue(game.newGame("first") > 0);        
        assertTrue(game.newGame("test") > 0);
        assertTrue(game.newGame("for") > 0);        
        assertTrue(game.newGame("GameService") > 0);        
    }

    @Test
    public void getGamesTestPositive() {
        var game = new GameService();

        game.newGame("next");
        game.newGame("test");
        game.newGame("for");
        game.newGame("GameService");

        assertTrue(game.getGames().size() == 4);
    }

    @Test
    public void joinGameTestPositive() {
        var game = new GameService();

        int id = game.newGame("testing");

        assertDoesNotThrow(() -> game.joinGame(id, ChessGame.TeamColor.WHITE,
            "whiteuser"));
        assertDoesNotThrow(() -> game.joinGame(id, ChessGame.TeamColor.WHITE,
            "whiteuser"));
    }

    @Test
    public void joinGameTestNegative() {
        var game = new GameService();

        int id = game.newGame("testing");

        assertDoesNotThrow(() -> game.joinGame(id, ChessGame.TeamColor.WHITE,
            "whiteuser"));
        assertThrows(Exception.class, () -> game.joinGame(id,
            ChessGame.TeamColor.WHITE, "blackuser"));
    }
}

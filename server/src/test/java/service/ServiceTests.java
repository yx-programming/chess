package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
}

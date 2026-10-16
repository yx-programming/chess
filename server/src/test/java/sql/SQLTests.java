package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataaccess.*;
import model.*;

public class SQLTests {
    @Test
    public void testUserInsertUserPositive() {
        try {
            var user = new SQLUserDAO();
            user.insertUser(new UserData("fred", "fake pass", "email@me.net"));
        } catch (Exception e) {
            assertTrue(false, e.getMessage());
        }
    }

    @Test
    public void testUserInsertUserNegative() {
        try {
            var user = new SQLUserDAO();
            user.insertUser(new UserData("fred", "fake pass", "email@me.net"));
            assertThrows(DataAccessException.class, () -> user.insertUser(
                new UserData("empty fields", null, null)));
        } catch (Exception e) {
            assertTrue(false, e.getMessage());
        }
    }
}

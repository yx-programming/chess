package dataaccess;

import java.util.*;

import model.*;

public class MemUserDAO implements UserDAO {
    private Map<String, UserData> users;

    public MemUserDAO() {
        users = new HashMap<String, UserData>();
    }

    public void insertUser(UserData user) throws DataAccessException {
        if (getUser(user.username()) != null) {
            throw new DataAccessException("user already in database");
        }
        if (user.password() == null || user.email() == null) {
            throw new DataAccessException("bad request");
        }
        users.put(user.username(), user);
    }

    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    public void clear() throws DataAccessException {
        users.clear();
    }
}

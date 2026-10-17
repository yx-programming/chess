package service;

import dataaccess.*;
import model.*;

public class UserService {
    private UserDAO userData;
    
    public UserService() throws DataAccessException {
        userData = new SQLUserDAO();
    }

    public void newUser(UserData user) throws DataAccessException {
        try {
            userData.insertUser(user);
        } catch (DataAccessException dae) {
            throw new DataAccessException("already taken");
        }
    }

    public void validateUser(UserData user) throws Exception {
        UserData loginInfo = userData.getUser(user.username());
        if (loginInfo == null ||
                !loginInfo.password().equals(user.password())) {
            throw new Exception("unauthorized");
        }
    }

    protected UserDAO getUserData() { return userData; }

    public void clear() throws DataAccessException {
        userData.clear();
    }
}

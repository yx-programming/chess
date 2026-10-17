package service;

import dataaccess.*;
import model.*;

import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private UserDAO userData;
    
    public UserService() throws DataAccessException {
        userData = new SQLUserDAO();
    }

    public void newUser(UserData user) throws DataAccessException {
        try {
            userData.insertUser(user);
        } catch (DataAccessException dae) {
            if (dae.getMessage().contains("Duplicate entry")) {
                throw new DataAccessException("already taken");
            } else {
                throw dae;
            }
        }
    }

    public void validateUser(UserData user) throws Exception {
        UserData loginInfo = userData.getUser(user.username());
        if (loginInfo == null ||
                !BCrypt.checkpw(user.password(), loginInfo.password())) {
            throw new Exception("unauthorized");
        }
    }

    protected UserDAO getUserData() { return userData; }

    public void clear() throws DataAccessException {
        userData.clear();
    }
}

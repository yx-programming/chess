package service;

import dataaccess.*;
import model.*;

public class UserService {
    private UserDAO userData;
    
    public UserService() {
        userData = new MemUserDAO();
    }

    public void newUser(UserData user) throws DataAccessException {
        userData.insertUser(user);
    }

    protected UserDAO getUserData() { return userData; }
}

package service;

import dataaccess.*;
import model.*;

public class AuthService {
    private AuthDAO auth;

    public AuthService() {
        auth = new MemAuthDAO();
    }

    public AuthData newAuth(String username) {
        return auth.newAuth(username);
    }
}

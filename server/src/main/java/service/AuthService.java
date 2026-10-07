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

    public String getUsernameFromAuthToken(String authToken) throws Exception {
        AuthData auth = this.auth.getAuth(authToken);
        if (auth == null) {
            throw new Exception("unauthorized");
        }

        return auth.username();
    }
}

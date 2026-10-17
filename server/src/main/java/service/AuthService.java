package service;

import dataaccess.*;
import model.*;

public class AuthService {
    private AuthDAO auth;

    public AuthService() throws DataAccessException {
        auth = new MemAuthDAO();
    }

    public AuthData newAuth(String username) throws DataAccessException {
        return auth.newAuth(username);
    }

    public AuthData getAuthFromToken(String authToken) throws Exception {
        AuthData auth = this.auth.getAuth(authToken);
        if (auth == null) {
            throw new Exception("unauthorized");
        }

        return auth;
    }

    public void removeAuth(AuthData authData) throws DataAccessException {
        auth.removeAuth(authData);
    }

    public void clear() throws DataAccessException {
        auth.clear();
    }
}

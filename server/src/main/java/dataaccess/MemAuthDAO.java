package dataaccess;

import java.util.*;

import model.*;

public class MemAuthDAO implements AuthDAO {
    private Map<String, AuthData> authMap;

    public AuthData newAuth(String username) {
        String token = UUID.randomUUID().toString();
        return new AuthData(token, username);
    }

    public AuthData getAuth(String auth) {
        return authMap.get(auth);
    }

    public void removeAuth(AuthData auth) {
        authMap.remove(auth.authToken());
    }

    public boolean hasAuth(AuthData auth) {
        return getAuth(auth.authToken()) != null;
    }

    public void clear() {
        authMap.clear();
    }
}

package dataaccess;

import java.util.*;

import model.*;

public class MemAuthDAO implements AuthDAO {
    private Map<String, AuthData> authMap;

    public MemAuthDAO() {
        authMap = new HashMap<String, AuthData>();
    }

    public AuthData newAuth(String username) {
        String token = UUID.randomUUID().toString();
        var authData = new AuthData(token, username);
        authMap.put(authData.authToken(), authData);
        return authData;
    }

    public AuthData getAuth(String authToken) {
        return authMap.get(authToken);
    }

    public void removeAuth(AuthData auth) {
        authMap.remove(auth.authToken());
    }

    public void clear() {
        authMap.clear();
    }
}

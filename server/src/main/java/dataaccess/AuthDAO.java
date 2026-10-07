package dataaccess;

import model.*;

public interface AuthDAO {
    public AuthData newAuth(String username);
    public AuthData getAuth(String authToken);
    public void removeAuth(AuthData auth);
    public void clear();
}

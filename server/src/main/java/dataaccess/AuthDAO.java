package dataaccess;

import model.*;

public interface AuthDAO {
    public AuthData newAuth(String username);
    public AuthData getAuth(String auth);
    public void removeAuth(AuthData auth);
    public boolean hasAuth(AuthData auth);
    public void clear();
}

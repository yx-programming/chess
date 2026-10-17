package dataaccess;

import model.*;

public interface AuthDAO {
    public AuthData newAuth(String username) throws DataAccessException;
    public AuthData getAuth(String authToken) throws DataAccessException;
    public void removeAuth(AuthData auth) throws DataAccessException;
    public void clear() throws DataAccessException;
}

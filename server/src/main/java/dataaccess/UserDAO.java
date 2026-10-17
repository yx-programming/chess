package dataaccess;

import model.*;

public interface UserDAO {
    public void insertUser(UserData user) throws DataAccessException;
    public UserData getUser(String username) throws DataAccessException;
    public void clear() throws DataAccessException;
}

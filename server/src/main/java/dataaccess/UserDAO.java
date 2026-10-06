package dataaccess;

import model.*;

public interface UserDAO {
    public void insertUser(UserData user) throws DataAccessException;
    public void deleteUser(UserData user);
    public UserData getUser(String username);
    public void clear();
}

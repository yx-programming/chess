package dataaccess;

import java.util.*;

import model.*;

public class SQLAuthDAO implements AuthDAO {
    public SQLAuthDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        var statementString = """
            create table if not exists authtable (
                id int not null auto_increment,
                username varchar(255) unique not null,
                token char(36) unique not null,
                primary key (id),
                index (token)
        )""";
        SQLCommand.execute(statementString,
            statement -> statement.executeUpdate());
    }

    public AuthData newAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();   
        var authData = new AuthData(token, username);
        var statementString = """
            insert into authtable (username, token) values (?, ?);""";
        SQLCommand.execute(statementString, statement -> {
            statement.setString(1, username);
            statement.setString(2, token);
            return statement.executeUpdate();
        });
        return authData;
    }
    
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statementString = """
            select username, token from authtable where token=?;""";
        return SQLCommand.execute(statementString, statement -> {
            statement.setString(1, authToken);
            try (var rs = statement.executeQuery()) {
                if (rs.next()) {
                    var username = rs.getString(1);
                    return new AuthData(username, authToken);
                } else {
                    return null;
                }
            }
        });
    }
    
    public void removeAuth(AuthData auth) throws DataAccessException {

    }
    
    public void clear() throws DataAccessException {
        var statementString = "truncate table authtable;";
        SQLCommand.execute(statementString,
            statement -> statement.executeUpdate());
    }
}

package dataaccess;

import model.*;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        var statementString = """
            create table if not exists users (
                id int not null auto_increment,
                username varchar(255) unique not null,
                password varchar(255) not null,
                email varchar(255) unique not null,
                primary key (id)
            )""";
        SQLCommand.execute(statementString,
            statement -> statement.executeUpdate());
    }

    public void insertUser(UserData user) throws DataAccessException {
        var statementRaw = """
            insert into users (username, password, email) values (?, ?, ?);
            """;
        var hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        SQLCommand.execute(statementRaw, statement -> {
            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.email());
            return statement.executeUpdate();
        });
    }

    private UserData queryUsersTableForUsername(PreparedStatement statement,
            String username) throws Exception {
        statement.setString(1, username);
        try (var rs = statement.executeQuery()) {
            if (rs.next()) {
                var password = rs.getString(1);
                var email = rs.getString(2);
                return new UserData(username, password, email);
            } else {
                return null;
            }
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        try {
            var statementString = """
                select password, email from users where username=?;""";
            return SQLCommand.execute(statementString,
                statement -> queryUsersTableForUsername(statement, username));
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void clear() throws DataAccessException {
        try {
            SQLCommand.execute("truncate table users;",
                statement -> statement.executeUpdate());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }
};

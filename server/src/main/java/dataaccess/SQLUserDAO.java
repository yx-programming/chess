package dataaccess;

import model.*;

import java.sql.*;

public class SQLUserDAO implements UserDAO {

    private <R> R runCommand(String statement, SQLCommand<R> toRun)
            throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                return toRun.apply(preparedStatement);
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        var statementString = """
            create table if not exists users (
                id int not null auto_increment,
                username varchar(255) not null,
                password varchar(255) not null,
                email varchar(255) not null,
                primary key (id)
            )""";
        runCommand(statementString, statement -> statement.executeUpdate());
    }

    public void insertUser(UserData user) throws DataAccessException {
        var statementRaw = """
            insert into users (username, password, email) values (?, ?, ?);
            """;
        runCommand(statementRaw, statement -> {
            statement.setString(1, user.username());
            // TODO: bCrypt the password
            statement.setString(2, user.password());
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

    public UserData getUser(String username) {
        try {
            var statementRaw = """
                select (password, email) from users where username=?;""";
            return runCommand(statementRaw,
                statement -> queryUsersTableForUsername(statement, username));
        } catch (Exception e) {
            return null;
        }
    }

    public void clear() {

    }
};

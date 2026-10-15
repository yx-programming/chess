package dataaccess;

import model.*;

public class SQLUserDAO implements UserDAO {
    public SQLUserDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                create table if not exists users (
                    id int not null auto_increment,
                    username varchar(255) not null,
                    password varchar(255) not null,
                    email varchar(255) not null,
                    primary key (id)
                )""";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void insertUser(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                insert into users values('%s', '%s', '%s');
            """;
            statement = statement.format(user.username(), user.password(),
                user.email());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public UserData getUser(String username) {
        return null;
    }

    public void clear() {

    }
};

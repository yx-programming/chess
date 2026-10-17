package dataaccess;

import java.sql.*;

// this is basically java.util.function.Function but it can throw exceptions
public interface SQLCommand<R> {
    public R apply(PreparedStatement conn) throws Exception;

    public static <R> R execute(String statement, SQLCommand<R> toRun)
            throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                return toRun.apply(preparedStatement);
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}

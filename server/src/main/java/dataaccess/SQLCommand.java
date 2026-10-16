package dataaccess;

import java.sql.*;

// this is basically java.util.function.Function but it can throw exceptions
public interface SQLCommand<R> {
    public R apply(PreparedStatement conn) throws Exception;
}

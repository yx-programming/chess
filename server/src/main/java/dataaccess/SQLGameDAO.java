package dataaccess;

import chess.*;
import model.*;

import java.util.*;

public class SQLGameDAO implements GameDAO {
    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        var statementString = """
            create table if not exists games (
                id int not null auto_increment,
                whiteUser varchar(255),
                blackUser varchar(255),
                gameName varchar(255) not null,
                game text,
                primary key (id)
        )""";
        SQLCommand.execute(statementString,
            statement -> statement.executeUpdate());
    }

    public List<GameInfo> getGames() throws DataAccessException {
        var statementString = "select * from games;";
        return SQLCommand.execute(statementString, statement -> {
            try (var rs = statement.executeQuery()) {
                var games = new ArrayList<GameInfo>();
                while (rs.next()) {
                    var id = rs.getInt(1);
                    var white = rs.getString(2);
                    var black = rs.getString(3);
                    var name = rs.getString(4);
                    var gameString = rs.getString(5);
                    var game = ChessGame.fromSerialized(gameString);
                    games.add(new GameInfo(id, white, black, name, game));
                }
                return games;
            }
        });
    }

    public int newGame(String name) throws DataAccessException {
        var statementString = """
            insert into games (whiteUser, blackUser, gameName, game)
            values (null, null, ?, ?);""";
        return SQLCommand.execute(statementString, statement -> {
            statement.setString(1, name);
            statement.setString(2, new ChessGame().serialize());
            statement.executeUpdate();
            var ids = statement.getGeneratedKeys();
            return (ids.next()) ? ids.getInt(1) : -1;
        });
    }

    public GameInfo getGame(int id) throws DataAccessException {
        var queryString = """
            select whiteUser, blackUser, gameName, game from games
            where id = ?;""";
        var game = SQLCommand.execute(queryString, statement -> {
            statement.setInt(1, id);
            try (var rs = statement.executeQuery()) {
                if (rs.next()) {
                    var white = rs.getString(1);
                    var black = rs.getString(2);
                    var name = rs.getString(3);
                    var gameString = rs.getString(4);
                    var chessGame = ChessGame.fromSerialized(gameString);
                    return new GameInfo(id, white, black, name, chessGame);
                } else {
                    throw new DataAccessException("game does not exist");
                }
            }
        });
        return game;
    }

    public void updateGame(GameInfo game) throws DataAccessException {
        var statementString = """
            update games set whiteUser=?, blackUser=?, game=? where id=?""";
        SQLCommand.execute(statementString, statement -> {
            statement.setString(1, game.getWhiteUser());
            statement.setString(2, game.getBlackUser());
            statement.setString(3, game.getGame().serialize());
            statement.setInt(4, game.getID());
            return statement.executeUpdate();
        });
    }

    public void deleteGame(GameInfo game) throws DataAccessException {
        var statementString = "delete from games where id=?";
        SQLCommand.execute(statementString, statement -> {
            statement.setInt(1, game.getID());
            return statement.executeUpdate();
        });
    }

    public GameInfo joinGame(int id, ChessGame.TeamColor color,
            String username) throws DataAccessException {
        var gameToJoin = getGame(id);
        if (color == ChessGame.TeamColor.WHITE) {
            if (gameToJoin.getWhiteUser() != null &&
                    !username.equals(gameToJoin.getWhiteUser())) {
                throw new DataAccessException("already taken");       
            }
            gameToJoin.setWhiteUser(username);
        } else {
            if (gameToJoin.getBlackUser() != null &&
                    !username.equals(gameToJoin.getBlackUser())) {
                throw new DataAccessException("already taken");       
            } 
            gameToJoin.setBlackUser(username);
        }
        updateGame(gameToJoin);
        return gameToJoin;
    }
    public void clear() throws DataAccessException {
        var statementString = "truncate table games;";
        SQLCommand.execute(statementString,
            statement -> statement.executeUpdate());
    }
};

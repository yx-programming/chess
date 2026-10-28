package dataaccess;

import java.util.*;

import chess.*;
import model.*;

public interface GameDAO {
    public List<GameInfo> getGames() throws DataAccessException;
    public int newGame(String name) throws DataAccessException;
    public GameInfo getGame(int id) throws DataAccessException;
    public void updateGame(GameInfo game) throws DataAccessException;
    public GameInfo joinGame(int id, ChessGame.TeamColor color,
        String username) throws DataAccessException;
    public void clear() throws DataAccessException;
}

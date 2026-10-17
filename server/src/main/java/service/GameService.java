package service;

import chess.*;
import dataaccess.*;

import java.util.*;

public class GameService {
    private GameDAO game;

    public GameService() throws DataAccessException {
        game = new MemGameDAO();
    }

    public List<GameInfo> getGames() throws DataAccessException {
        return game.getGames();
    }

    public int newGame(String name) throws DataAccessException {
        return game.newGame(name);
    }

    public void joinGame(int id, ChessGame.TeamColor color, String name)
            throws DataAccessException {
        game.joinGame(id, color, name);
    }

    public void clear() throws DataAccessException {
        game.clear();
    }
}

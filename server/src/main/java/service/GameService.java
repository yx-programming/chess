package service;

import chess.*;
import dataaccess.*;

import java.util.*;

public class GameService {
    private GameDAO game;

    public GameService() throws DataAccessException {
        game = new SQLGameDAO();
    }

    public List<GameInfo> getGames() throws DataAccessException {
        return game.getGames();
    }

    public int newGame(String name) throws DataAccessException {
        return game.newGame(name);
    }

    public ChessGame joinGame(int id, ChessGame.TeamColor color, String name)
            throws DataAccessException {
        var info = game.joinGame(id, color, name);
        return info.getGame();
    }

    public void clear() throws DataAccessException {
        game.clear();
    }
}

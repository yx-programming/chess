package service;

import chess.*;
import dataaccess.*;

import java.util.*;

public class GameService {
    private GameDAO game;

    public GameService() {
        game = new MemGameDAO();
    }

    public List<GameInfo> getGames() {
        return game.getGames();
    }

    public int newGame(String name) {
        return game.newGame(name);
    }

    public void joinGame(int id, ChessGame.TeamColor color, String name)
            throws Exception {
        game.joinGame(id, color, name);
    }

    public void clear() {
        game.clear();
    }
}

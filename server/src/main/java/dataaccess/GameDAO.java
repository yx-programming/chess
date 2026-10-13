package dataaccess;

import java.util.*;

import chess.*;
import model.*;

public interface GameDAO {
    public List<GameInfo> getGames();
    public int newGame(String name);
    public void joinGame(int id, ChessGame.TeamColor color,
        String username) throws Exception;
}
